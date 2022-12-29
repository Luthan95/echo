/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.echo.config

import com.netflix.spinnaker.echo.jackson.EchoObjectMapper
import com.netflix.spinnaker.retrofit.RetrofitResponseInterceptor
import com.netflix.spinnaker.retrofit.Slf4jRetrofitLogger
import lombok.Setter
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.echo.twilio.TwilioService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty('twilio.enabled')
@Slf4j
@CompileStatic
class TwilioConfig {

    @Setter
    @Value('${twilio.base-url:https://api.twilio.com/}')
    private String twilioBaseUrl

    @Bean
    TwilioService twilioService(
      @Value('${twilio.account}') String username,
      @Value('${twilio.token}') String password,
      OkHttpClient retrofitClient,
      HttpLoggingInterceptor.Level retrofitLogLevel) {

        log.info('twilio service loaded')

        retrofitClient = retrofitClient.newBuilder()
          .addInterceptor({ chain ->
            String auth = "Basic " + Base64.encodeBase64String("${username}:${password}".getBytes())
            Request.Builder builder = chain.request().newBuilder();
            builder.addHeader("Authorization", auth);
            return chain.proceed(builder.build());
          })
          .addInterceptor(new RetrofitResponseInterceptor())
          .addInterceptor(new HttpLoggingInterceptor(new Slf4jRetrofitLogger(TwilioService.class)).setLevel(retrofitLogLevel))
          .build();

  //        RequestInterceptor authInterceptor = new RequestInterceptor() {
  //            @Override
  //            public void intercept(RequestInterceptor.RequestFacade request) {
  //                String auth = "Basic " + Base64.encodeBase64String("${username}:${password}".getBytes())
  //                request.addHeader("Authorization", auth)
  //            }
  //        }

         new Retrofit.Builder()
          .baseUrl(twilioBaseUrl)
          .client(retrofitClient)
          .addConverterFactory(JacksonConverterFactory.create(EchoObjectMapper.getInstance()))
          .build()
          .create(TwilioService.class);

//        JacksonConverter converter = new JacksonConverter(EchoObjectMapper.getInstance())
//
//        new RestAdapter.Builder()
//                .setEndpoint(twilioEndpoint)
//                .setRequestInterceptor(authInterceptor)
//                .setClient(retrofitClient)
//                .setLogLevel(retrofitLogLevel)
//                .setLog(new Slf4jRetrofitLogger(TwilioService.class))
//                .setConverter(converter)
//                .build()
//                .create(TwilioService.class)
    }

}
