package com.amadeus.ghostingutils.request;

public class GenerateJSONResponseRequest {

  private Object preRequest;
  private Object actualRequest;
  private Object response;
  private Object preUriPattern;
  private Object uriPattern;

  public Object getPreUriPattern() {
    return preUriPattern;
  }

  public Object getUriPattern() {
    return uriPattern;
  }

  public Object getPreRequest() {
    return preRequest;
  }

  public Object getActualRequest() {
    return actualRequest;
  }

  public Object getResponse() {
    return response;
  }

  @Override
  public String toString() {
    return "GenerateJSONResponseRequest [preRequest=" + preRequest + ", actualRequest=" + actualRequest + ", response="
        + response + ", preUriPattern=" + preUriPattern + ", uriPattern=" + uriPattern + "]";
  }

}
