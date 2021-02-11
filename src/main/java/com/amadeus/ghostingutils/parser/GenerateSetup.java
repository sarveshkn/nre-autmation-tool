package com.amadeus.ghostingutils.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import com.amadeus.ghostingutils.service.GhostingUtilV2Service;

public class GenerateSetup {

  private String ghostingWriteDirectoryV2;

  private static final String JSON_EXTENSION = ".json";
  private static final String SETUP_TXT = "Setup.txt";
  private static final String CLOSING_BRACES = "    },\n";
  private static final String RTL_CRT = "\"RTL-CRT\"\n";
  private static final String COMPONENT = "        \"component\": ";
  private static final String REQUEST_NAME = "        \"requestName\": ";
  private static final String OPEN_BRACES = "    {\n";

  public void createSetup(String responseFolderPath) throws IOException {
    File file = new File(responseFolderPath);
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
      createSetupFile(ghostingWriteDirectoryV2, files);
    }
  }

  private static void createSetupFile(String ghostedWriteDirectoryV2, File[] files) throws IOException {
    List<String> requests = new ArrayList<>();
    for (File file : files) {
      requests.add(OPEN_BRACES);
      requests.add(REQUEST_NAME);
      requests.add("\"" + getFileName(file.getName()) + "\",\n");
      requests.add(COMPONENT);
      requests.add(RTL_CRT);
      requests.add(CLOSING_BRACES);
    }
    generateFiles(ghostedWriteDirectoryV2, requests);
  }

  private static String getFileName(String name) {
    return GhostingUtilV2Service.getStepNameToPostmanStepName()
        .get(name.substring(0, name.indexOf(JSON_EXTENSION)));
  }

  private static void generateFiles(String ghostedWriteDirectoryV2,
      List<String> requests) throws IOException {
    StringBuilder builder = new StringBuilder();
    String destinationFilePathV2 = ghostedWriteDirectoryV2 + SETUP_TXT;
    // create before
    for (String s : before()) {
      builder.append(s);
    }

    for (String s : requests) {
      builder.append(s);
    }

    // create after
    for (String s : after()) {
      builder.append(s);
    }
    Files.write(Paths.get(destinationFilePathV2), builder.toString().getBytes());
  }

  private static List<String> before() {
    List<String> before = new ArrayList<>();
    before.add("eval(globals.setupTestAtFirstStep);\n");
    before.add("setupTestAtFirstStep();\n");
    before.add("\n");
    before.add("// harcoded list of requests\n");
    before.add("var requests = [\n");
    return before;
  }

  private static List<String> after() {
    List<String> after = new ArrayList<>();
    after.add("];\n");
    after.add("\n");
    after.add("pm.environment.set(\"requests\", requests);");
    return after;
  }

  public void setGhostingWriteDirectoryV2(String ghostingWriteDirectoryV2) {
    this.ghostingWriteDirectoryV2 = ghostingWriteDirectoryV2;
  }
}
