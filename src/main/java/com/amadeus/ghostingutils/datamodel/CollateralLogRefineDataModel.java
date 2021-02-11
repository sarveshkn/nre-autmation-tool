package com.amadeus.ghostingutils.datamodel;

import java.util.List;

public class CollateralLogRefineDataModel {

  private String trxnb;

  private String payload;

  private String responseService;

  private String requestService;

  private List<Variable> variables;

  private boolean isRequest;

  public CollateralLogRefineDataModel(String trxnb, String payload, String responseService, String requestService,
      String requestFileName, String responseFileName, List<Variable> tids, boolean isRequest) {
    super();
    this.trxnb = trxnb;
    this.payload = payload;
    this.responseService = responseService;
    this.requestFileName = requestFileName;
    this.responseFileName = responseFileName;
    this.requestService = requestService;
    this.variables = tids;
    this.isRequest = isRequest;
  }

  public boolean isRequest() {
    return isRequest;
  }

  public void setRequest(boolean isRequest) {
    this.isRequest = isRequest;
  }

  public String getResponseService() {
    return responseService;
  }

  public void setResponseService(String responseService) {
    this.responseService = responseService;
  }

  public String getRequestService() {
    return requestService;
  }

  public void setRequestService(String requestService) {
    this.requestService = requestService;
  }

  public String getTrxnb() {
    return trxnb;
  }

  public void setTrxnb(String trxnb) {
    this.trxnb = trxnb;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public String getResponseFileName() {
    return responseFileName;
  }

  public void setResponseFileName(String responseFileName) {
    this.responseFileName = responseFileName;
  }

  public List<Variable> getTids() {
    return variables;
  }

  public void setTids(List<Variable> tids) {
    this.variables = tids;
  }

  private String responseFileName;

  private String requestFileName;

  public String getRequestFileName() {
    return requestFileName;
  }

  public void setRequestFileName(String requestFileName) {
    this.requestFileName = requestFileName;
  }

  @Override
  public String toString() {
    return "collateralLogRefineDataModel [trxnb=" + trxnb + ", payload=" + payload + ", responseService="
        + responseService + ", requestService=" + requestService + ", variables=" + variables
        + ", responseFileName=" + responseFileName + ", requestFileName=" + requestFileName + "]";
  }

}
