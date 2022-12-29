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
import com.netflix.spinnaker.echo.services.IgorService;
import com.netflix.spinnaker.okhttp.SpinnakerRequestInterceptor;
import com.netflix.spinnaker.retrofit.RetrofitResponseInterceptor;
import com.netflix.spinnaker.retrofit.Slf4jRetrofitLogger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
@ConditionalOnProperty("igor.enabled")
@Slf4j
public class IgorConfig {
  //  @Bean
  //  public Endpoint igorEndpoint(@Value("${igor.base-url}") String igorBaseUrl) {
  //    return Endpoints.newFixedEndpoint(igorBaseUrl);
  //  }

  @Setter
  @Value("${igor.base-url}")
  private String igorBaseUrl;

  @Bean
  public IgorService igorService(
      OkHttpClientProvider clientProvider,
      HttpLoggingInterceptor.Level retrofitLogLevel,
      SpinnakerRequestInterceptor spinnakerRequestInterceptor) {
    log.info("igor service loaded");

    return new Retrofit.Builder()
        .baseUrl(igorBaseUrl)
        .addConverterFactory(JacksonConverterFactory.create())
        .client(
            clientProvider
                .getClient(new DefaultServiceEndpoint("igor", igorBaseUrl))
                .newBuilder()
                .addInterceptor(spinnakerRequestInterceptor)
                .addInterceptor(new RetrofitResponseInterceptor())
                .addInterceptor(
                    new HttpLoggingInterceptor(new Slf4jRetrofitLogger(IgorService.class))
                        .setLevel(retrofitLogLevel))
                .build())
        .build()
        .create(IgorService.class);

    //    return new Builder()
    //        .setEndpoint(igorEndpoint)
    //        .setConverter(new JacksonConverter())
    //        .setClient(
    //            new Ok3Client(
    //                clientProvider.getClient(
    //                    new DefaultServiceEndpoint("igor", igorEndpoint.getUrl()))))
    //        .setRequestInterceptor(spinnakerRequestInterceptor)
    //        .setLogLevel(retrofitLogLevel)
    //        .setLog(new Slf4jRetrofitLogger(IgorService.class))
    //        .build()
    //        .create(IgorService.class);
  }
}
