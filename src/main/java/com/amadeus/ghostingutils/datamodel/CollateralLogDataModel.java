package com.amadeus.ghostingutils.datamodel;

public class CollateralLogDataModel {

  private String id;

  private String headers;

  private String payload;

  public CollateralLogDataModel(String id, String headers, String payload) {
    super();
    this.id = id;
    this.headers = headers;
    this.payload = payload;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getHeaders() {
    return headers;
  }

  public void setHeaders(String headers) {
    this.headers = headers;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

}
