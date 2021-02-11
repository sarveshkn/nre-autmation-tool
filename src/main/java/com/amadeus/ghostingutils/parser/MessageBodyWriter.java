package com.amadeus.ghostingutils.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.amadeus.ghostingutils.datamodel.MessageKey;

public class MessageBodyWriter {

  private String ghostingWriteDirectoryV2;

  public void updateBodyInMessage(Map<String, List<MessageKey>> map) {

    for (Map.Entry<String, List<MessageKey>> entry : map.entrySet()) {
      if (entry.getValue() != null && entry.getValue().size() > 1) {
        for (MessageKey key : entry.getValue()) {
          String messageFileAbsolutePath = ghostingWriteDirectoryV2 + "messages/" + key.getMessageFileName();
          String requestFileAbsolutePath = ghostingWriteDirectoryV2 + "requests/" + key.getRequestFileName();

          String body;
          try {
            body = FileUtils.readFileToString(new File(requestFileAbsolutePath), (String)null);
            body = StringUtils.substringBetween(body, "<soap:Body>", "</soap:Body>");
            if (body == null) {
              body = StringUtils.substringBetween(body, "<Body>", "</Body>");
            }
            body = convertNamespacesToRegex(body);
            body = body.trim();
            body = StringEscapeUtils.escapeJava(body);
            File messageFile = new File(messageFileAbsolutePath);
            String message = FileUtils.readFileToString(messageFile, (String)null);
            message = message.replace(".*", body);
            FileUtils.writeStringToFile(messageFile, message, (String)null);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private static String convertNamespacesToRegex(String body) {
    String processedbody = body.replace(StringUtils.substringBetween(body, " ", ">"), ".*");
    String nameSpaceRegex = "[a-zA-Z0-9]+:";
    Pattern pattern = Pattern.compile(nameSpaceRegex);
    Matcher matcher = pattern.matcher(processedbody);
    if (matcher.find()) {
      processedbody = matcher.replaceAll(nameSpaceRegex);
    } else {
      return body;
    }
    return processedbody;
  }

  public void setGhostingWriteDirectoryV2(String ghostingWriteDirectoryV2) {
    this.ghostingWriteDirectoryV2 = ghostingWriteDirectoryV2;
  }
}
