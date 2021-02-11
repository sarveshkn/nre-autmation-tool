package com.amadeus.ghostingutils.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.amadeus.ghostingutils.utils.CommonUtils;

/**
 * Service handling all the clean up activities
 */
public class CleanUpService {

  private String ghostingReadDirectory;
  private String praxisDirectory;
  private String usrGMCDirectory;

  private List<String> allProjects = null;

  public void doCleanupsOnFirstStep(String testId, boolean isUsr) {
    // Setting all project list
    allProjects = CommonUtils.loadAllProjectList(isUsr);
    doGhostingDataFolderCleanup(testId);
    doCleanups(isUsr);
  }

  /**
   * Cleans the previously generated collection specific ghosting data
   * 
   * @param testId
   */
  private void doGhostingDataFolderCleanup(String testId) {
    try {
      String colLogDir = ghostingReadDirectory + "/" + testId;
      File dirToClean = new File(colLogDir);
      if (dirToClean.exists()) {
        FileUtils.forceDelete(dirToClean);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Cleans all log folders
   * 
   * @param isUSR
   */
  private void doCleanups(boolean isUSR) {
    for (String sproject : allProjects) {
      try {
        // Cleaning Collateral Logs
        cleanCollateralLogs(sproject, praxisDirectory);
        if (isUSR) {
          cleanCollateralLogs(sproject, usrGMCDirectory);
        }
        // Cleaning Praxis Dlogs folder
        cleanDlogs(sproject, praxisDirectory);
        if (isUSR) {
          // Cleaning Praxis Dlogs folder
          cleanDlogs(sproject, usrGMCDirectory);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void cleanDlogs(String sproject, String usrGMCDirectory) throws IOException {
		String usrDLogsDir = usrGMCDirectory + sproject + "/dlogs";
		File usrDirToClean = new File(usrDLogsDir);
		if (usrDirToClean.exists()) {
			FileUtils.cleanDirectory(usrDirToClean);
		}
  }

  private void cleanCollateralLogs(String sproject, String usrGMCDirectory) throws FileNotFoundException {
    String usrGMCDirectoryI = usrGMCDirectory + sproject;
    File mainFileToClean = new File(usrGMCDirectoryI);
    if (mainFileToClean.exists()) {
      for (File collateralFile : mainFileToClean.listFiles()) {
        if (collateralFile.getName().startsWith("collateral-calls.log") && collateralFile.exists()) {
          PrintWriter writer = new PrintWriter(collateralFile);
          writer.print("");
          writer.close();
        }
      }
    }
  }

  public void setGhostingReadDirectory(String ghostingReadDirectory) {
    this.ghostingReadDirectory = ghostingReadDirectory;
  }

  public void setPraxisDirectory(String praxisDirectory) {
    this.praxisDirectory = praxisDirectory;
  }

  public void setUsrGMCDirectory(String usrGMCDirectory) {
    this.usrGMCDirectory = usrGMCDirectory;
  }
}
