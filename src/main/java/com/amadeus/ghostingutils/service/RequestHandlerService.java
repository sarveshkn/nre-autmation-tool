package com.amadeus.ghostingutils.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.amadeus.ghostingutils.request.GenerateJSONResponseRequest;
import com.amadeus.ghostingutils.utils.CommonConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RequestHandlerService {

  private String ghostingReadDirectory;

  public void generateJSONRequestFiles(String testId, GenerateJSONResponseRequest requestBody,
      String stepName) {
    Object beforeString = requestBody.getPreRequest();
    Object afterString = requestBody.getActualRequest();
    Object preUriPattern = requestBody.getPreUriPattern();
    Object uriPattern = requestBody.getUriPattern();
    Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    Set<Map<String, String>> listSet = new HashSet<>();
    String preUriString = (null != preUriPattern) ? preUriPattern.toString() : "";
    String uriString = (null != uriPattern) ? uriPattern.toString() : "";
    String[] preUriStrings = preUriString.substring(preUriString.indexOf("v2")).split("/");
    String[] uriStrings = uriString.substring(uriString.indexOf("v2")).split("/");
    for (int i = 0; i < uriStrings.length; i++) {
      if (!uriStrings[i].equals(preUriStrings[i])) {
        if (!uriStrings[i].contains(CommonConstants.QUESTION_STRING)) {
          Map<String, String> newEntryMap = new HashMap<>();
          newEntryMap.put(CommonConstants.KEY_STRING,
              com.amadeus.ghostingutils.utils.CommonUtils.cleanUpString(preUriStrings[i]));
          newEntryMap.put(CommonConstants.VALUE_STRING, uriStrings[i]);
          listSet.add(newEntryMap);
        } else {
          handleQueryParams(listSet, preUriStrings[i], uriStrings[i]);
        }
      }
    }
    if (null != beforeString && null != afterString) {
      handleComparisonStrings(beforeString, afterString, prettyGson, listSet);
    } else {
      beforeString = null;
      afterString = null;
    }
    HashMap<String, Object> masterMap = prepareFinalMap(beforeString, afterString, listSet);
    String dataToWrite = prettyGson.toJson(masterMap);
    writeToFile(testId, stepName, dataToWrite);
  }

  private void handleComparisonStrings(Object beforeString, Object afterString, Gson prettyGson,
      Set<Map<String, String>> listSet) {
    String preRequest = prettyGson.toJson(beforeString);
    String actualRequest = prettyGson.toJson(afterString);
    try {
      List<MapDifference<String, Object>> difference = findRequestDifferences(preRequest, actualRequest);
      prepareEntryMap(difference, listSet);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void writeToFile(String testId, String stepName, String dataToWrite) {
    try {
      FileUtils.writeStringToFile(
          new File(ghostingReadDirectory + testId + "/jsonRequest/" + stepName + ".json"),
          dataToWrite, (String)null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleQueryParams(Set<Map<String, String>> listSet, String preUriString1, String uriString1) {
    String[] preSplitByQM = preUriString1.split("\\?");
    String[] splitByQM = uriString1.split("\\?");
    if (!splitByQM[0].equals(preSplitByQM[0])) {
      Map<String, String> newEntryMap = new HashMap<>();
      newEntryMap.put(CommonConstants.KEY_STRING,
          com.amadeus.ghostingutils.utils.CommonUtils.cleanUpString(preSplitByQM[0]));
      newEntryMap.put(CommonConstants.VALUE_STRING, splitByQM[0]);
      listSet.add(newEntryMap);
    }
    String[] preUriStringsChild = preUriString1.substring(preUriString1.indexOf(CommonConstants.QUESTION_STRING))
        .split(
            CommonConstants.EQUALS_STRING);
    String[] uriStringsChild = uriString1.substring(uriString1.indexOf(CommonConstants.QUESTION_STRING))
        .split(CommonConstants.EQUALS_STRING);
    for (int k = 0; k < uriStringsChild.length; k++) {
      if (!uriStringsChild[k].equals(preUriStringsChild[k])) {
        if (!uriStringsChild[k].contains(CommonConstants.AMPERSAND_STRING)) {
          if (uriStringsChild[k].contains(CommonConstants.COMMA_STRING)) {
            String[] preUriStringsChild3 = preUriStringsChild[k].split(CommonConstants.COMMA_STRING);
            String[] uriStringsChild3 = uriStringsChild[k].split(CommonConstants.COMMA_STRING);
            extractKeyValueFromChildList(listSet, preUriStringsChild3, uriStringsChild3);
          } else {
            Map<String, String> newEntryMap = new HashMap<>();
            newEntryMap.put(CommonConstants.KEY_STRING,
                com.amadeus.ghostingutils.utils.CommonUtils.cleanUpString(preUriStringsChild[k]));
            newEntryMap.put(CommonConstants.VALUE_STRING, uriStringsChild[k]);
            listSet.add(newEntryMap);
          }
        } else {
          String[] preUriStringsChild2 = preUriStringsChild[k].split(CommonConstants.AMPERSAND_STRING);
          String[] uriStringsChild2 = uriStringsChild[k].split(CommonConstants.AMPERSAND_STRING);
          extractKeyValueFromChildList(listSet, preUriStringsChild2, uriStringsChild2);
        }
      }
    }
  }

  private void extractKeyValueFromChildList(Set<Map<String, String>> listSet, String[] preUriStringsChild3,
      String[] uriStringsChild3) {
    for (int j = 0; j < uriStringsChild3.length; j++) {
      if (!uriStringsChild3[j].equals(preUriStringsChild3[j])) {
        Map<String, String> newEntryMap = new HashMap<>();
        newEntryMap.put(CommonConstants.KEY_STRING,
            com.amadeus.ghostingutils.utils.CommonUtils.cleanUpString(preUriStringsChild3[j]));
        newEntryMap.put(CommonConstants.VALUE_STRING, uriStringsChild3[j]);
        listSet.add(newEntryMap);
      }
    }
  }

  private HashMap<String, Object> prepareFinalMap(Object beforeString, Object afterString,
      Set<Map<String, String>> newEntryMapList) {
    HashMap<String, Object> masterMap = new HashMap<>();
    masterMap.put("entryVariables", newEntryMapList);
    masterMap.put("preRequest", null == beforeString ? "" : beforeString);
    masterMap.put("actualRequest", null == afterString ? "" : afterString);
    return masterMap;
  }

  private List<MapDifference<String, Object>> findRequestDifferences(String preRequest, String actualRequest) {
    List<MapDifference<String, Object>> finalDifferences = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<HashMap<String, Object>> type = new TypeReference<HashMap<String, Object>>() {
    };
    Map<String, Object> leftMap;
    Map<String, Object> rightMap;
    try {
      leftMap = mapper.readValue(preRequest, type);
      rightMap = mapper.readValue(actualRequest, type);
      Map<String, Object> leftFlatMap = com.amadeus.ghostingutils.utils.FlatMapUtil.flatten(leftMap);
      Map<String, Object> rightFlatMap = com.amadeus.ghostingutils.utils.FlatMapUtil.flatten(rightMap);
      MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);
      finalDifferences.add(difference);
    } catch (JsonProcessingException e) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      try {
        List<HashMap<String, Object>> leftMapList = mapper.readValue(preRequest,
            mapper.getTypeFactory().constructCollectionType(List.class, HashMap.class));
        List<HashMap<String, Object>> rightMapList = mapper.readValue(actualRequest,
            mapper.getTypeFactory().constructCollectionType(List.class, HashMap.class));
        for (int i = 0; i < leftMapList.size(); i++) {
          Map<String, Object> leftFlatMap = com.amadeus.ghostingutils.utils.FlatMapUtil.flatten(leftMapList.get(i));
          Map<String, Object> rightFlatMap = com.amadeus.ghostingutils.utils.FlatMapUtil.flatten(rightMapList.get(i));
          MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);
          finalDifferences.add(difference);
        }
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
    return finalDifferences;
  }

  private void prepareEntryMap(List<MapDifference<String, Object>> difference,
      Set<Map<String, String>> listSet) {
    for (MapDifference<String, Object> singleDifference : difference) {
      singleDifference.entriesDiffering().forEach((key, value) -> {
        String finalValue = String.valueOf(value);
        String[] arrOfStr = finalValue.split(CommonConstants.COMMA_STRING, 2);
        Map<String, String> newEntryMap = new HashMap<>();
        newEntryMap.put(CommonConstants.KEY_STRING,
            com.amadeus.ghostingutils.utils.CommonUtils.cleanUpString(arrOfStr[0]));
        newEntryMap.put(CommonConstants.VALUE_STRING,
            com.amadeus.ghostingutils.utils.CommonUtils.cleanUpString(arrOfStr[1]));
        listSet.add(newEntryMap);
      });
    }
  }

  public void setGhostingReadDirectory(String ghostingReadDirectory) {
    this.ghostingReadDirectory = ghostingReadDirectory;
  }
}
