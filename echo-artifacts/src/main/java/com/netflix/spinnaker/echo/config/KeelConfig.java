package com.netflix.spinnaker.echo.config;

import com.netflix.spinnaker.config.DefaultServiceEndpoint;
import com.netflix.spinnaker.config.okhttp3.OkHttpClientProvider;
import com.netflix.spinnaker.echo.services.KeelService;
import com.netflix.spinnaker.retrofit.RetrofitResponseInterceptor;
import com.netflix.spinnaker.retrofit.Slf4jRetrofitLogger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
@Slf4j
@ConditionalOnExpression("${keel.enabled:false}")
public class KeelConfig {
  @Bean
  public HttpLoggingInterceptor.Level retrofitLogLevel(
      @Value("${retrofit.log-level:BASIC}") String retrofitLogLevel) {
    return HttpLoggingInterceptor.Level.valueOf(retrofitLogLevel);
  }

  @Value("${keel.base-url}")
  @Setter
  private String keelBaseUrl;

  @Bean
  public KeelService keelService(
      String keelEndpoint,
      OkHttpClientProvider clientProvider,
      HttpLoggingInterceptor.Level retrofitLogLevel) {

    OkHttpClient.Builder clientBuilder =
        clientProvider.getClient(new DefaultServiceEndpoint("keel", keelBaseUrl)).newBuilder();
    clientBuilder.addInterceptor(new RetrofitResponseInterceptor());
    clientBuilder.addInterceptor(
        new HttpLoggingInterceptor(new Slf4jRetrofitLogger(KeelService.class))
            .setLevel(retrofitLogLevel));

    return new Retrofit.Builder()
        .baseUrl(new DefaultServiceEndpoint("keel", keelBaseUrl).getBaseUrl())
        .client(clientBuilder.build())
        .addConverterFactory(JacksonConverterFactory.create())
        .build()
        .create(KeelService.class);

    //    return new RestAdapter.Builder()
    //        .setEndpoint(keelEndpoint)
    //        .setConverter(new JacksonConverter())
    //        .setClient(
    //            new Ok3Client(
    //                clientProvider.getClient(
    //                    new DefaultServiceEndpoint("keel", keelEndpoint.getUrl()))))
    //        .setLogLevel(retrofitLogLevel)
    //        .setLog(new Slf4jRetrofitLogger(KeelService.class))
    //        .build()
    //        .create(KeelService.class);
  }
}
