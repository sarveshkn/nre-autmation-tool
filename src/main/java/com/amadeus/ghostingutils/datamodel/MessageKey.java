package com.amadeus.ghostingutils.datamodel;

public class MessageKey {

  public MessageKey(String messageFileName, String requestFileName) {
    super();
    this.messageFileName = messageFileName;
    this.requestFileName = requestFileName;
  }

  public String getMessageFileName() {
    return messageFileName;
  }

  public void setMessageFileName(String messageFileName) {
    this.messageFileName = messageFileName;
  }

  public String getRequestFileName() {
    return requestFileName;
  }

  public void setRequestFileName(String requestFileName) {
    this.requestFileName = requestFileName;
  }

  private String messageFileName;

  private String requestFileName;

}
