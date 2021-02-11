package com.amadeus.ghostingutils.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.amadeus.ghostingutils.datamodel.CollateralLogRefineDataModel;
import com.amadeus.ghostingutils.datamodel.Variable;
import com.amadeus.ghostingutils.datamodel.Variables;
import com.amadeus.ghostingutils.utils.CommonConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MoxMatcherWriter {

  static MoxMatcherWriter moxMatcherWriter;

  private static final List<CollateralLogRefineDataModel> logs = new ArrayList<>();

  private MoxMatcherWriter() {
  }

  public static MoxMatcherWriter getInstance() {
    if (moxMatcherWriter == null) {
      return new MoxMatcherWriter();
    }
    return moxMatcherWriter;
  }

  public void addLogs(CollateralLogRefineDataModel log) {
    logs.add(log);
  }

  public void createMatcherFile(String ghostingDirectory, String ghostedWriteDirectoryV1) {
    String templateMatcherFile = ghostingDirectory + CommonConstants.TEMPLATE_DIR
        + CommonConstants.MATCHER_JSON_FILE_NAME;
    String matcherFileName = ghostedWriteDirectoryV1 + "_mox_matcher.json";
    String matcherFileContent;
    StringBuilder newMatcherFileContent = new StringBuilder();

    try {

      for (CollateralLogRefineDataModel log : logs) {
        if (log.isRequest()) {
          matcherFileContent = FileUtils.readFileToString(new File(templateMatcherFile), (String)null);
          matcherFileContent = StringUtils.replace(matcherFileContent, "{{id}}",
              log.getTrxnb() + "_" + log.getRequestService());
          matcherFileContent = StringUtils.replace(matcherFileContent, "{{service}}",
              log.getRequestService());
          matcherFileContent = StringUtils.replace(matcherFileContent, "{{trxnb}}", log.getTrxnb());
          if (log.getTids() != null && !log.getTids().isEmpty()) {
            matcherFileContent = StringUtils.replace(matcherFileContent, "{{variables}}",
                createVariableContent(log.getTids()));
          } else {
            matcherFileContent = StringUtils.replace(matcherFileContent, "{{variables}},",
                CommonConstants.EMPTY_STRING);
          }
          matcherFileContent = StringUtils.replace(matcherFileContent, "{{requestFileName}}",
              log.getRequestFileName());
          matcherFileContent = StringUtils.replace(matcherFileContent, "{{responseFileName}}",
              log.getResponseFileName());
          newMatcherFileContent.append(matcherFileContent).append(CommonConstants.COMMA_STRING);
        }

      }
      if (newMatcherFileContent.lastIndexOf(CommonConstants.COMMA_STRING) > 0) {
        newMatcherFileContent.deleteCharAt(newMatcherFileContent.lastIndexOf(CommonConstants.COMMA_STRING));
      }

      FileUtils.writeStringToFile(new File(matcherFileName), newMatcherFileContent.toString(), (String)null);

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private String createVariableContent(List<Variable> variableList) {
    Variables variables = new Variables(variableList);
    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    String json = gson.toJson(variables);
    if (json != null) {
      json = json.substring(1, json.length() - 1);
    }
    return json;
  }
}
