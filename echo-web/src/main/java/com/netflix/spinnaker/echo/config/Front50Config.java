/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import com.netflix.spinnaker.config.DefaultServiceEndpoint;
import com.netflix.spinnaker.config.okhttp3.OkHttpClientProvider;
import com.netflix.spinnaker.echo.services.Front50Service;
import com.netflix.spinnaker.okhttp.SpinnakerRequestInterceptor;
import com.netflix.spinnaker.retrofit.RetrofitResponseInterceptor;
import com.netflix.spinnaker.retrofit.Slf4jRetrofitLogger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
@Slf4j
public class Front50Config {

  @Bean
  public HttpLoggingInterceptor.Level retrofitLogLevel() {
    return HttpLoggingInterceptor.Level.BASIC;
  }

  @Value("${front50.base-url}")
  @Setter
  private String front50BaseUrl;

  @Bean
  public Front50Service front50Service(
      OkHttpClientProvider clientProvider,
      HttpLoggingInterceptor.Level retrofitLogLevel,
      SpinnakerRequestInterceptor spinnakerRequestInterceptor) {
    log.info("front50 service loaded");

    OkHttpClient.Builder clientBuilder =
        clientProvider
            .getClient(new DefaultServiceEndpoint("front50", front50BaseUrl))
            .newBuilder();
    clientBuilder.addInterceptor(spinnakerRequestInterceptor);
    clientBuilder.addInterceptor(new RetrofitResponseInterceptor());
    clientBuilder.addInterceptor(
        new HttpLoggingInterceptor(new Slf4jRetrofitLogger(Front50Service.class))
            .setLevel(retrofitLogLevel));

    return new Retrofit.Builder()
        .baseUrl(front50BaseUrl)
        .client(clientBuilder.build())
        .addConverterFactory(JacksonConverterFactory.create())
        .build()
        .create(Front50Service.class);

    //     new Builder()
    //        .setEndpoint(front50Endpoint)
    //        .setConverter(new JacksonConverter())
    //        .setClient(
    //            new Ok3Client(
    //                clientProvider.getClient(
    //                    new DefaultServiceEndpoint("front50", front50Endpoint.getUrl()))))
    //        .setRequestInterceptor(spinnakerRequestInterceptor)
    //        .setLogLevel(retrofitLogLevel)
    //        .setLog(new Slf4jRetrofitLogger(Front50Service.class))
    //        .build()
    //        .create(Front50Service.class);
  }
}
