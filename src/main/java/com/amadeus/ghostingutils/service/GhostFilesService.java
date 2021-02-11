package com.amadeus.ghostingutils.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.amadeus.ghostingutils.datamodel.MessageKey;
import com.amadeus.ghostingutils.migrate.MigrateV2ToV3;
import com.amadeus.ghostingutils.parser.CollateralLogParser;
import com.amadeus.ghostingutils.parser.DLogParser;
import com.amadeus.ghostingutils.parser.GenerateSetup;
import com.amadeus.ghostingutils.parser.JsonResponseParser;
import com.amadeus.ghostingutils.parser.MessageBodyWriter;
import com.amadeus.ghostingutils.parser.TestConfigGenerator;
import com.amadeus.ghostingutils.utils.CommonUtils;

/**
 * Service related to ghost file generation
 */
public class GhostFilesService {

  @Autowired
  DLogParser dLogParser;
  @Autowired
  CollateralLogParser collateralLogParser;
  @Autowired
  JsonResponseParser jsonResponseParser;
  @Autowired
  MessageBodyWriter messageBodyWriter;
  @Autowired
  TestConfigGenerator testConfigGenerator;
  
  @Autowired 
  MigrateV2ToV3 migrateV2ToV3;

  @Autowired
  GenerateSetup generateSetup;

  private String ghostingReadDirectory;
  private String praxisDirectory;
  private String ghostingWriteDirectoryV2;

  private List<String> allProjects = null;

  public void lastStepProcessing(String testId, String officeId, boolean isUsr) throws IOException {
    loadAllProjects(isUsr);
    if (isUsr) {
      processBuilder();
    }
    // Moving the generated Dlog files from praxis to ghosting folder
    copyDlogFiles(testId);
    // Copying the collateral log file to ghosting folder
    copycollateralLogs(testId);
    // Generating Ghosting files
    generateGhostingFiles(testId, officeId, isUsr);
    
    //migrateToV3(testId);
  }

  private void migrateToV3(String testId) {
	  migrateV2ToV3.setGhostingReadDirectory(ghostingReadDirectory);
	  migrateV2ToV3.setV2Directory(ghostingWriteDirectoryV2);
	  migrateV2ToV3.setV3Directory(ghostingWriteDirectoryV2);
	  migrateV2ToV3.setMigrating("true");
	  migrateV2ToV3.migrateGhostFiles();
  }

private void loadAllProjects(boolean isUsr) {
    allProjects = CommonUtils.loadAllProjectList(isUsr);
  }

  public void copyDlogFiles(String testId) {
    for (String sproject : allProjects) {
      File destDir = new File(ghostingReadDirectory + testId + "/dlogs/" + sproject);
      File rtlSerDirectory = new File(praxisDirectory + sproject);
      File ghostedDirectory = new File(praxisDirectory + sproject + "/dlogs");
      if (rtlSerDirectory.exists() && ghostedDirectory.exists()) {
        copyDlogs(destDir, ghostedDirectory);
      }
    }
  }

  private void copyDlogs(File destDir, File ghostedDirectory) {
    File[] files = ghostedDirectory.listFiles();
    if (null != files && files.length > 0) {
      for (File file : files) {
        try {
          FileUtils.copyFileToDirectory(file, destDir, true);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void copycollateralLogs(String testId) {
    for (String sproject : allProjects) {
      File destDir = new File(ghostingReadDirectory + testId + "/collateralLogs/" + sproject);
      File file = new File(praxisDirectory + sproject);
      try {
        if (file.exists()) {
          for (File collateralFile : file.listFiles()) {
            if (collateralFile.getName().startsWith("collateral-calls.log") && collateralFile.exists()) {
              FileUtils.copyFileToDirectory(collateralFile, destDir);
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void generateGhostingFiles(String testId, String officeId, boolean isUsr) throws IOException {
    String ghostedWriteDirectoryV2 = ghostingWriteDirectoryV2 + testId + "/";
    String collateralTemplate = null;
    String jsonGhostTemplate = null;
    String collateralTemplateV2 = null;
    String ghostedDirectoryTemplateName = ghostingReadDirectory + "template/" + officeId;
    try {
      collateralTemplate = FileUtils.readFileToString(new File(ghostedDirectoryTemplateName + "/collateral.mu"),
          (String)null);
      collateralTemplateV2 = FileUtils
          .readFileToString(new File(ghostedDirectoryTemplateName + "/collateral.yml"), (String)null);
      jsonGhostTemplate = FileUtils
          .readFileToString(new File(ghostedDirectoryTemplateName + "/postman_ghost.json"), (String)null);
      File ghostedDirectoryV2 = new File(ghostedWriteDirectoryV2 + "messages");
      ghostedDirectoryV2.mkdirs();
      FileUtils.cleanDirectory(new File(ghostedWriteDirectoryV2));
      Map<String, List<MessageKey>> trxnbToFileMap = new HashMap<>();

      updateWriteDirectoryWithTestId(ghostedWriteDirectoryV2);

      dLogParser.parseDLog(testId, officeId, isUsr, trxnbToFileMap);
      collateralLogParser.parsecollateralLog(collateralTemplate, collateralTemplateV2, testId, officeId, isUsr,
          trxnbToFileMap);
      jsonResponseParser.createJsonGhostFile(jsonGhostTemplate, testId);
      //messageBodyWriter.updateBodyInMessage(trxnbToFileMap);
      generateSetup.createSetup(ghostingReadDirectory + testId + "/jsonResponse");
      testConfigGenerator.generateTestConfig(testId);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void updateWriteDirectoryWithTestId(String ghostedWriteDirectoryV2) {
    dLogParser.setGhostingWriteDirectoryV2(ghostedWriteDirectoryV2);
    collateralLogParser.setGhostingWriteDirectoryV2(ghostedWriteDirectoryV2);
    jsonResponseParser.setGhostingWriteDirectoryV2(ghostedWriteDirectoryV2);
    messageBodyWriter.setGhostingWriteDirectoryV2(ghostedWriteDirectoryV2);
    generateSetup.setGhostingWriteDirectoryV2(ghostedWriteDirectoryV2);
    testConfigGenerator.setGhostingWriteDirectoryV2(ghostedWriteDirectoryV2);
  }

  private void processBuilder() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    String userName = StringUtils.substringBetween(ghostingReadDirectory, "users/", "/");
    processBuilder.command("/remote/users/" + userName + "/rtl-deployment-structure/applications-build/synclogs.sh");
    try {
      Process process = processBuilder.start();
      StringBuilder output = new StringBuilder();
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setGhostingReadDirectory(String ghostingReadDirectory) {
    this.ghostingReadDirectory = ghostingReadDirectory;
  }

  public void setPraxisDirectory(String praxisDirectory) {
    this.praxisDirectory = praxisDirectory;
  }

  public void setGhostingWriteDirectoryV2(String ghostingWriteDirectoryV2) {
    this.ghostingWriteDirectoryV2 = ghostingWriteDirectoryV2;
  }
}
