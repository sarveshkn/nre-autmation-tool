package com.amadeus.ghostingutils.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amadeus.ghostingutils.migrate.MigrateV2ToV3;
import com.amadeus.ghostingutils.parser.CollateralLogParser;
import com.amadeus.ghostingutils.parser.DLogParser;
import com.amadeus.ghostingutils.parser.GenerateSetup;
import com.amadeus.ghostingutils.parser.JsonResponseParser;
import com.amadeus.ghostingutils.parser.MessageBodyWriter;
import com.amadeus.ghostingutils.parser.TestConfigGenerator;
import com.amadeus.ghostingutils.service.CleanUpService;
import com.amadeus.ghostingutils.service.GhostFilesService;
import com.amadeus.ghostingutils.service.GhostingUtilV2Service;
import com.amadeus.ghostingutils.service.RequestHandlerService;
import com.amadeus.ghostingutils.service.ResponseHandlerService;

@Configuration
public class GhostingUtilConfiguration {

  /**
   * All templates and ghosting data will be read from this directory, edit it in properties file
   */
  @Value("${ghosting.directory.read}")
  private String ghostingReadDirectory;

  /**
   * All V2 ghosted files will be generated in this directory, edit it in properties file
   */
  @Value("${ghosting.directory.write.V2}")
  private String ghostingWriteDirectoryV2;

  /**
   * Local Praxis folder from where logs will be fetched, edit it in properties file
   */
  @Value("${ghosting.praxis.directory}")
  private String praxisDirectory;
  
  /**
   * User GMC directory, edit it in properties file
   */
  @Value("${ghosting.usrGMCDirectory.directory}")
  private String usrGMCDirectory;

  /**
   * Is it v2 to V3 migration
   */
  @Value("${migrating}")
  private String migrating;
  
  /**
   * V3 directory in which new files will be generated
   */
  @Value("${ghosting.directory.V2}")
  private String v2Directory;
  
  /**
   * V3 directory in which new files will be generated
   */
  @Value("${ghosting.directory.V3}")
  private String v3Directory;

  @Bean
  GhostingUtilV2Service generateJSONResponseService() {
    GhostingUtilV2Service ghostingUtilV2Service = new GhostingUtilV2Service();
    ghostingUtilV2Service.setGhostingWriteDirectoryV2(ghostingWriteDirectoryV2);
    return ghostingUtilV2Service;
  }

  @Bean
  CleanUpService cleanUpService() {
    CleanUpService cleanUpService = new CleanUpService();
    cleanUpService.setGhostingReadDirectory(ghostingReadDirectory);
    cleanUpService.setPraxisDirectory(praxisDirectory);
    cleanUpService.setUsrGMCDirectory(usrGMCDirectory);
    return cleanUpService;
  }
  
  @Bean(initMethod="migrateGhostFiles")
  MigrateV2ToV3 migrateService() {
	  MigrateV2ToV3 migrateV2ToV3 = new MigrateV2ToV3();
	  migrateV2ToV3.setMigrating(migrating);
	  migrateV2ToV3.setV2Directory(v2Directory);
	  migrateV2ToV3.setV3Directory(v3Directory);
	  migrateV2ToV3.setGhostingReadDirectory(ghostingReadDirectory);
    return migrateV2ToV3;
  }

  @Bean
  DLogParser dLogParser() {
    DLogParser dLogParser = new DLogParser();
    dLogParser.setGhostingReadDirectory(ghostingReadDirectory);
    dLogParser.setGhostingWriteDirectoryV2(ghostingWriteDirectoryV2);
    return dLogParser;
  }

  @Bean
  CollateralLogParser collateralLogParser() {
    CollateralLogParser collateralLogParser = new CollateralLogParser();
    collateralLogParser.setGhostingReadDirectory(ghostingReadDirectory);
    collateralLogParser.setGhostingWriteDirectoryV2(ghostingWriteDirectoryV2);
    return collateralLogParser;
  }

  @Bean
  MessageBodyWriter messageBodyWriter() {
    MessageBodyWriter messageBodyWriter = new MessageBodyWriter();
    messageBodyWriter.setGhostingWriteDirectoryV2(ghostingWriteDirectoryV2);
    return messageBodyWriter;
  }

  @Bean
  JsonResponseParser jsonResponseParser() {
    JsonResponseParser jsonResponseParser = new JsonResponseParser();
    jsonResponseParser.setGhostingReadDirectory(ghostingReadDirectory);
    jsonResponseParser.setGhostingWriteDirectoryV2(ghostingWriteDirectoryV2);
    return jsonResponseParser;
  }

  @Bean
  GenerateSetup generateSetup() {
    GenerateSetup generateSetup = new GenerateSetup();
    generateSetup.setGhostingWriteDirectoryV2(ghostingWriteDirectoryV2);
    return generateSetup;
  }

  @Bean
  ResponseHandlerService responseHandlerService() {
    ResponseHandlerService responseHandlerService = new ResponseHandlerService();
    responseHandlerService.setGhostingReadDirectory(ghostingReadDirectory);
    return responseHandlerService;
  }

  @Bean
  RequestHandlerService requestHandlerService() {
    RequestHandlerService requestHandlerService = new RequestHandlerService();
    requestHandlerService.setGhostingReadDirectory(ghostingReadDirectory);
    return requestHandlerService;
  }

  @Bean
  GhostFilesService ghostFilesService() {
    GhostFilesService ghostFilesService = new GhostFilesService();
    ghostFilesService.setPraxisDirectory(praxisDirectory);
    ghostFilesService.setGhostingWriteDirectoryV2(ghostingWriteDirectoryV2);
    ghostFilesService.setGhostingReadDirectory(ghostingReadDirectory);
    return ghostFilesService;
  }

  @Bean
  TestConfigGenerator testConfigGenerator() {
    TestConfigGenerator testConfigGenerator = new TestConfigGenerator();
    testConfigGenerator.setGhostingReadDirectory(ghostingReadDirectory);
    testConfigGenerator.setGhostingWriteDirectoryV2(ghostingWriteDirectoryV2);
    return testConfigGenerator;
  }

}
