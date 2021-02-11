package com.amadeus.ghostingutils.datamodel;

public class JsonDataModel {

  private String trxnb;

  private String jsonResponse;

  private String stepId;

  public String getTrxnb() {
    return trxnb;
  }

  public void setTrxnb(String trxnb) {
    this.trxnb = trxnb;
  }

  public String getJsonResponse() {
    return jsonResponse;
  }

  public void setJsonResponse(String jsonResponse) {
    this.jsonResponse = jsonResponse;
  }

  public String getStepId() {
    return stepId;
  }

  public void setStepId(String stepId) {
    this.stepId = stepId;
  }

  public JsonDataModel(String trxnb, String jsonResponse, String stepId) {
    super();
    this.trxnb = trxnb;
    this.jsonResponse = jsonResponse;
    this.stepId = stepId;
  }

}
