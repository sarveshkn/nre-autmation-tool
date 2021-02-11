package com.amadeus.ghostingutils.parser;

import com.amadeus.ghostingutils.utils.CommonConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Test Config Json File generator
 */
@SuppressWarnings("deprecation")
public class TestConfigGenerator {

  private String ghostingReadDirectory;
  private String ghostingWriteDirectoryV2;

  /**
   * Method to refer the test config template and generate a test config json file related to a collection ghosted
   *
   * @param testId String
   * @throws IOException FileNotFound could be thrown
   */
  public void generateTestConfig(String testId)
      throws IOException {
    String configTemplate = FileUtils
        .readFileToString(new File(
            ghostingReadDirectory + CommonConstants.TEMPLATE_DIR
                + CommonConstants.TEST_CONFIG_TEMPLATE_JSON_FILE_NAME));
    String updatedFile = StringUtils.replace(configTemplate, "{{request.testId}}", testId);
    FileUtils.write(new File(ghostingWriteDirectoryV2 + CommonConstants.TEST_CONFIG_JSON_FILE_NAME), updatedFile);
  }

  public void setGhostingReadDirectory(String ghostingReadDirectory) {
    this.ghostingReadDirectory = ghostingReadDirectory;
  }

  public void setGhostingWriteDirectoryV2(String ghostingWriteDirectoryV2) {
    this.ghostingWriteDirectoryV2 = ghostingWriteDirectoryV2;
  }
}
