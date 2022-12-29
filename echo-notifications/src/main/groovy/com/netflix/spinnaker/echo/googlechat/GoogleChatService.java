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

package com.netflix.spinnaker.echo.googlechat;

import com.netflix.spinnaker.retrofit.RetrofitException;
import groovy.transform.Canonical;
import java.io.IOException;
import okhttp3.Response;

@Canonical
public class GoogleChatService {
  GoogleChatClient googleChatClient;

  public GoogleChatService(GoogleChatClient googleChatClient) {
    this.googleChatClient = googleChatClient;
  }

  Response sendMessage(String webhook, GoogleChatMessage message) {
    try {
      return googleChatClient.sendMessage(webhook, message).execute().body();
    } catch (IOException e) {
      throw RetrofitException.networkError("", e);
    }
  }
}
