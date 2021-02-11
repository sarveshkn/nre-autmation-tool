package com.amadeus.ghostingutils.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amadeus.ghostingutils.request.GenerateJSONResponseRequest;
import com.amadeus.ghostingutils.service.GhostingUtilV2Service;
import com.amadeus.ghostingutils.utils.CommonConstants;

@RestController
public class GenerateJSONReponseController {

  @Autowired
  GhostingUtilV2Service ghostingUtilV2Service;

  @PostMapping("/generateJSONResponse")
  public void generateJSONResponse(@RequestBody GenerateJSONResponseRequest requestBody,
      @RequestParam("testId") String testId, @RequestParam("stepName") String stepName,
      @RequestParam("lastStep") String lastStep, @RequestParam("postmanStepName") String postmanStepName,
      @RequestParam("officeId") String officeId, @RequestParam("isUSR") Boolean isUSR) {
      // As in MOX V3 all the templates are removed so overriding the office id with default office id
	  officeId = CommonConstants.AY_STRING;
    try {
      ghostingUtilV2Service.generateJSONResponse(testId, stepName, requestBody, lastStep, postmanStepName,
          officeId, isUSR);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @GetMapping("/getJsonGhostedData")
  public String getJsonGhostedData(@RequestParam("testId") String testId) {
    return ghostingUtilV2Service.getJsonGhostedData(testId);
  }

}
