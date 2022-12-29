package com.netflix.spinnaker.echo.services;

import com.netflix.spinnaker.echo.model.Pipeline;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface Front50Service {
  @GET("/pipelines?restricted=false")
  @Headers("Accept: application/json")
  Call<List<Map<String, Object>>>
      getPipelines(); // Return Map here so we don't throw away MPT attributes.

  @GET("/pipelines/{application}?refresh=false")
  @Headers("Accept: application/json")
  Call<List<Pipeline>> getPipelines(@Path("application") String application);

  @GET("/pipelines/{pipelineId}/get")
  Call<Map<String, Object>> getPipeline(@Path("pipelineId") String pipelineId);

  @POST("/graphql")
  @Headers("Accept: application/json")
  Call<GraphQLQueryResponse> query(@Body GraphQLQuery body);
}
