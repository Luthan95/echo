/*
 * Copyright 2018 xiaohongshu, Inc.
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

import com.netflix.spinnaker.echo.bearychat.BearychatService
import com.netflix.spinnaker.echo.github.GithubService
import com.netflix.spinnaker.retrofit.RetrofitResponseInterceptor
import com.netflix.spinnaker.retrofit.Slf4jRetrofitLogger
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty('bearychat.enabled')
@Slf4j
@CompileStatic
class BearychatConfig {

  final static String BEARYCHAT_BASE_URL = 'https://api.bearychat.com'
//  @Bean
//  Endpoint bearychatEndpoint() {
//    String endpoint = BEARYCHAT_BASE_URL
//    newFixedEndpoint(endpoint)
//  }

  @Bean
  BearychatService bearychatService(OkHttpClient retrofitClient, okhttp3.logging.HttpLoggingInterceptor.Level retrofitLogLevel) {
    log.info('bearchat service loaded')

    new Retrofit.Builder()
      .baseUrl(Objects.requireNonNull(HttpUrl.parse(BEARYCHAT_BASE_URL)))
      .client(retrofitClient.newBuilder()
        .addInterceptor(new RetrofitResponseInterceptor())
        .addInterceptor(new HttpLoggingInterceptor(new Slf4jRetrofitLogger(BearychatService.class)).setLevel(retrofitLogLevel))
        .build())
      .addConverterFactory(JacksonConverterFactory.create())
      .build().create(BearychatService.class);

//    new RestAdapter.Builder()
//      .setEndpoint(bearychatEndpoint)
//      .setConverter(new JacksonConverter())
//      .setClient(retrofitClient)
//      .setLogLevel(retrofitLogLevel)
//      .setLog(new Slf4jRetrofitLogger(BearychatService.class))
//      .build()
//      .create(BearychatService.class)
  }
}
