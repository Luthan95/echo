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

package com.netflix.spinnaker.echo.config

import com.netflix.spinnaker.echo.github.GithubService
import com.netflix.spinnaker.echo.googlechat.GoogleChatService
import com.netflix.spinnaker.echo.googlechat.GoogleChatClient
import com.netflix.spinnaker.retrofit.RetrofitResponseInterceptor
import com.netflix.spinnaker.retrofit.Slf4jRetrofitLogger
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory


@Configuration
@ConditionalOnProperty('googlechat.enabled')
@Slf4j
@CompileStatic
class GoogleChatConfig {

//  @Bean
//  Endpoint chatEndpoint() {
//    newFixedEndpoint("https://chat.googleapis.com")
//  }

  @Bean
  GoogleChatService chatService(OkHttpClient retrofitClient, HttpLoggingInterceptor.Level retrofitLogLevel) {

    log.info("Chat service loaded");

    def chatClient = new Retrofit.Builder()
          .baseUrl(Objects.requireNonNull(HttpUrl.parse("https://chat.googleapis.com")))
          .client(retrofitClient.newBuilder()
            .addInterceptor(new RetrofitResponseInterceptor())
            .addInterceptor(new HttpLoggingInterceptor(new Slf4jRetrofitLogger(GoogleChatClient.class)).setLevel(retrofitLogLevel))
            .build())
          .addConverterFactory(JacksonConverterFactory.create())
          .build().create(GoogleChatClient.class);

//    def chatClient = new RestAdapter.Builder()
//            .setConverter(new JacksonConverter())
//            .setClient(retrofitClient)
//            .setEndpoint(chatEndpoint)
//            .setLogLevel(retrofitLogLevel)
//            .setLog(new Slf4jRetrofitLogger(GoogleChatClient.class))
//            .build()
//            .create(GoogleChatClient.class)

    new GoogleChatService(chatClient)
  }

}
