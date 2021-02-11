package com.amadeus.ghostingutils.service;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ResponseHandlerService {

  private String ghostingReadDirectory;

  public void generateJSONResponseFiles(String testId, String stepName, Object response) {
    Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    String prettyResponse = prettyGson.toJson(response);
    try {
      FileUtils.writeStringToFile(new File(ghostingReadDirectory + testId + "/jsonResponse/" + stepName + ".json"),
          prettyResponse,
          (String)null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setGhostingReadDirectory(String ghostingReadDirectory) {
    this.ghostingReadDirectory = ghostingReadDirectory;
  }
}
