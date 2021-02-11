package com.amadeus.ghostingutils.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.lang3.StringUtils;

import com.amadeus.ghostingutils.request.RequestJSONFormat;
import com.amadeus.ghostingutils.service.GhostingUtilV2Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonResponseParser {

  private String ghostingReadDirectory;
  private String ghostingWriteDirectoryV2;

  public void createJsonGhostFile(String jsonTemplate, String testId) {
    File jsonFileDirectoryForReading = new File(ghostingReadDirectory + testId + "/jsonResponse");
    File[] jsonFiles = jsonFileDirectoryForReading.listFiles();
    Arrays.sort(jsonFiles, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
    Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    StringBuilder jsonGhostFinalContent = new StringBuilder();
    for (File jsonFile : jsonFiles) {
      String fileName = jsonFile.getName();
      File reqFile = new File(ghostingReadDirectory + testId + "/jsonRequest/" + fileName);
      RequestJSONFormat obj = null;
      try (BufferedReader bufferedReader = new BufferedReader(new FileReader(reqFile))) {
        String line = bufferedReader.readLine();
        StringBuilder jsonString = new StringBuilder();
        while (line != null) {
          jsonString.append(line);
          line = bufferedReader.readLine();
        }
        Gson g = new Gson();
        obj = g.fromJson(jsonString.toString(), RequestJSONFormat.class);
        fileName = fileName.substring(0, fileName.indexOf("."));
        String stepId = fileName;
        String trxnb = "";
        if (fileName.contains("_")) {
          trxnb = fileName.substring(0, fileName.indexOf("_"));
        } else {
          break;
        }
        String localJsonTemplate = prepareJsonTemplate(jsonTemplate, testId, prettyGson, jsonFile, obj, stepId, trxnb);
        jsonGhostFinalContent.append(localJsonTemplate)
            .append(",");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      writeToFile(jsonGhostFinalContent);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeToFile(StringBuilder jsonGhostFinalContent) throws IOException {
    String finalResponse = jsonGhostFinalContent.deleteCharAt(jsonGhostFinalContent.length() - 1)
        .append("]")
        .toString();
    finalResponse = "[" + finalResponse;
    FileUtils.writeStringToFile(new File(ghostingWriteDirectoryV2 + "_postman_ghost.json"),
        finalResponse, (String)null);
  }

  private String prepareJsonTemplate(String jsonTemplate, String testId, Gson prettyGson, File jsonFile,
      RequestJSONFormat obj, String stepId, String trxnb) throws IOException {
    String localJsonTemplate = jsonTemplate;
    String fileContent = FileUtils.readFileToString(jsonFile, (String)null);
    if (fileContent.length() > 1) {
      fileContent = fileContent.substring(1, fileContent.length() - 1);
    }
    localJsonTemplate = handleExpectedResponse(localJsonTemplate, fileContent);
    localJsonTemplate = StringUtils.replace(localJsonTemplate, "{{trxnb}}", trxnb);
    localJsonTemplate = handleEntryParameters(prettyGson, obj, localJsonTemplate);
    localJsonTemplate = StringUtils.replace(localJsonTemplate, "{{request.testId}}", testId);
    localJsonTemplate = StringUtils.replace(localJsonTemplate, "{{stepId}}",
        GhostingUtilV2Service.getStepNameToPostmanStepName().get(stepId));
    return localJsonTemplate;
  }

  private String handleExpectedResponse(String localJsonTemplate, String fileContent) {
    if (fileContent.equals("{}")) {
      localJsonTemplate = StringUtils.replace(localJsonTemplate, "{{expectedResponse}}", "");
    } else {
      localJsonTemplate = StringUtils.replace(localJsonTemplate, "{{expectedResponse}}", fileContent);
    }
    return localJsonTemplate;
  }

  private String handleEntryParameters(Gson prettyGson, RequestJSONFormat obj, String localJsonTemplate) {
    if (null != obj) {
      String toPrint = prettyGson.toJson(obj.getEntryVariables());
      localJsonTemplate = StringUtils.replace(localJsonTemplate, "{{entryTemporaryVariables}}",
          toPrint.substring(1, toPrint.length() - 1));
    } else {
      localJsonTemplate = StringUtils.replace(localJsonTemplate, "{{entryTemporaryVariables}}",
          "\"No Entry Temporary Variables\"");
    }
    return localJsonTemplate;
  }

  public void setGhostingReadDirectory(String ghostingReadDirectory) {
    this.ghostingReadDirectory = ghostingReadDirectory;
  }

  public void setGhostingWriteDirectoryV2(String ghostingWriteDirectoryV2) {
    this.ghostingWriteDirectoryV2 = ghostingWriteDirectoryV2;
  }
}