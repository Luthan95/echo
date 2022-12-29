/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.echo.config;

// import static retrofit.Endpoints.newFixedEndpoint;

// import com.jakewharton.retrofit.Ok3Client;
import com.netflix.spinnaker.config.DefaultServiceEndpoint;
import com.netflix.spinnaker.config.okhttp3.OkHttpClientProvider;
import com.netflix.spinnaker.echo.rest.RestService;
import com.netflix.spinnaker.retrofit.RetrofitResponseInterceptor;
import com.netflix.spinnaker.retrofit.Slf4jRetrofitLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import retrofit.RequestInterceptor;
// import retrofit.RestAdapter;
// import retrofit.converter.JacksonConverter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/** Rest endpoint configuration */
@Configuration
@ConditionalOnProperty("rest.enabled")
public class RestConfig {

  private static final Logger log = LoggerFactory.getLogger(RestConfig.class);

  @Bean
  public HttpLoggingInterceptor.Level retrofitLogLevel(
      @Value("${retrofit.log-level:BASIC}") String retrofitLogLevel) {
    return HttpLoggingInterceptor.Level.valueOf(retrofitLogLevel);
  }

  interface RequestInterceptorAttacher {
    void attach(OkHttpClient.Builder builder, Interceptor interceptor);
  }

  @Bean
  public RequestInterceptorAttacher requestInterceptorAttacher() {
    return OkHttpClient.Builder::addInterceptor;
  }

  interface HeadersFromFile {
    Map<String, String> headers(String path);
  }

  @Bean
  HeadersFromFile headersFromFile() {
    return path -> {
      Map<String, String> headers = new HashMap<>();
      try {
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
          String line;
          while ((line = br.readLine()) != null) {
            String[] pair = line.split(":");
            if (pair.length == 2) {
              headers.put(pair[0], pair[1].trim());
            } else {
              log.warn("Could not parse header from file={}", path);
            }
          }
        }
      } catch (Exception e) {
        log.error("Error parsing headers from file={}", path, e);
      }
      return headers;
    };
  }

  @Bean
  RestUrls restServices(
      RestProperties restProperties,
      HttpLoggingInterceptor.Level retrofitLogLevel,
      RequestInterceptorAttacher requestInterceptorAttacher,
      OkHttpClientProvider okHttpClientProvider,
      HeadersFromFile headersFromFile) {

    RestUrls restUrls = new RestUrls();

    for (RestProperties.RestEndpointConfiguration endpoint : restProperties.getEndpoints()) {

      OkHttpClient.Builder okhttpClient =
          endpoint.insecure
              ? okHttpClientProvider
                  .getClient(
                      new DefaultServiceEndpoint(endpoint.getEventName(), endpoint.getUrl(), false))
                  .newBuilder()
              : new OkHttpClient().newBuilder();
      okhttpClient.addInterceptor(new RetrofitResponseInterceptor());
      okhttpClient.addInterceptor(
          new HttpLoggingInterceptor(new Slf4jRetrofitLogger(RestService.class))
              .setLevel(retrofitLogLevel));

      //      RestAdapter.Builder restAdapterBuilder =
      //          new RestAdapter.Builder()
      //              .setEndpoint(newFixedEndpoint(endpoint.getUrl()))
      //              .setClient(
      //                  endpoint.insecure
      //                      ? new Ok3Client(
      //                          okHttpClientProvider.getClient(
      //                              new DefaultServiceEndpoint(
      //                                  endpoint.getEventName(), endpoint.getUrl(), false)))
      //                      : new Ok3Client(new OkHttpClient()))
      //              .setLogLevel(retrofitLogLevel)
      //              .setLog(new Slf4jRetrofitLogger(RestService.class))
      //              .setConverter(new JacksonConverter());

      Map<String, String> headers = new HashMap<>();

      if (endpoint.getUsername() != null && endpoint.getPassword() != null) {
        String basicAuthCreds = endpoint.getUsername() + ":" + endpoint.getPassword();
        String auth = "Basic " + Base64.encodeBase64String(basicAuthCreds.getBytes());
        headers.put("Authorization", auth);
      }

      if (endpoint.getHeaders() != null) {
        headers.putAll(endpoint.headers);
      }

      if (endpoint.getHeadersFile() != null) {
        headers.putAll(headersFromFile.headers(endpoint.getHeadersFile()));
      }

      if (!headers.isEmpty()) {
        Interceptor headerInterceptor =
            chain -> {
              Request.Builder request = chain.request().newBuilder();
              headers.forEach((k, v) -> request.addHeader(k, v));
              return chain.proceed(request.build());
            };
        requestInterceptorAttacher.attach(okhttpClient, headerInterceptor);
      }

      Retrofit.Builder restAdapterBuilder =
          new Retrofit.Builder()
              .baseUrl(endpoint.getUrl())
              .client(okhttpClient.build())
              .addConverterFactory(JacksonConverterFactory.create());

      RestUrls.Service service =
          RestUrls.Service.builder()
              .client(restAdapterBuilder.build().create(RestService.class))
              .config(endpoint)
              .build();

      restUrls.getServices().add(service);
    }

    return restUrls;
  }
}
