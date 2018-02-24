package com.my.server.http.ex03.pyrmont;

import com.my.server.http.ex03.pyrmont.connector.http.HttpRequest;
import com.my.server.http.ex03.pyrmont.connector.http.HttpResponse;
import java.io.IOException;

public class StaticResourceProcessor {

  public void process(HttpRequest request, HttpResponse response) {
    try {
      response.sendStaticResource();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

}
