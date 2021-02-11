package com.amadeus.ghostingutils.service;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amadeus.ghostingutils.request.GenerateJSONResponseRequest;

/**
 * Main Service class for Ghosting Util V2, contains business logic for all required actions to be performed
 */
@Service
public class GhostingUtilV2Service {

  protected static final Map<String, String> stepNameToPostmanStepName = new LinkedHashMap<>();

  private String ghostingWriteDirectoryV2;

  @Autowired
  RequestHandlerService requestHandlerService;

  @Autowired
  ResponseHandlerService responseHandlerService;

  @Autowired
  GhostFilesService ghostFilesService;

  @Autowired
  CleanUpService cleanUpService;

  public void generateJSONResponse(String testId, String stepName, GenerateJSONResponseRequest requestBody,
      String lastStepName, String postmanStepName, String officeId, boolean isUsr) throws IOException {
    if (postmanStepName.equals("Setup")) {
      // Cleaning up while running the first step of collection (Setup Step)
      cleanUpService.doCleanupsOnFirstStep(testId, isUsr);
    } else {
      // Generating Request files for each step
      requestHandlerService.generateJSONRequestFiles(testId, requestBody, stepName);

      // Generating Response files for each step
      responseHandlerService.generateJSONResponseFiles(testId, stepName, requestBody.getResponse());

      stepNameToPostmanStepName.put(stepName, postmanStepName);

      if (isLastStep(lastStepName, postmanStepName)) {
        ghostFilesService.lastStepProcessing(testId, officeId, isUsr);
        stepNameToPostmanStepName.clear();
      }
    }
  }

  public String getJsonGhostedData(String testId) {
    String ghostedJsonFilePath = ghostingWriteDirectoryV2 + testId + "/_postman_ghost.json";
    String jsonGhostedData = "";
    try {
      jsonGhostedData = FileUtils.readFileToString(new File(ghostedJsonFilePath),(String) null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return jsonGhostedData;
  }

  private boolean isLastStep(String lastStepName, String postmanStepName) {
    return lastStepName.equals(postmanStepName);
  }

  public static Map<String, String> getStepNameToPostmanStepName() {
    return stepNameToPostmanStepName;
  }

  public void setGhostingWriteDirectoryV2(String ghostingWriteDirectoryV2) {
    this.ghostingWriteDirectoryV2 = ghostingWriteDirectoryV2;
  }

}
