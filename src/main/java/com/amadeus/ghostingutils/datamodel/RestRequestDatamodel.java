package com.amadeus.ghostingutils.datamodel;

public class RestRequestDatamodel {

  private String trxnb;

  private String requestPayload;

  private String responsePayload;

  public RestRequestDatamodel(String trxnb, String requestPayload, String responsePayload) {
    super();
    this.trxnb = trxnb;
    this.requestPayload = requestPayload;
    this.responsePayload = responsePayload;
  }

  public RestRequestDatamodel() {
  }

  public String getTrxnb() {
    return trxnb;
  }

  public void setTrxnb(String trxnb) {
    this.trxnb = trxnb;
  }

  public String getRequestPayload() {
    return requestPayload;
  }

  public void setRequestPayload(String requestPayload) {
    this.requestPayload = requestPayload;
  }

  public String getResponsePayload() {
    return responsePayload;
  }

  public void setResponsePayload(String responsePayload) {
    this.responsePayload = responsePayload;
  }

}
