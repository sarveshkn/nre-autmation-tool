package com.amadeus.ghostingutils.migrate;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MigrateV2ToV3 {

	private String migrating;
	private String ghostingReadDirectory;

	private static String outputV3Path = "C:/DAPI/Ghosting-Workshop/migration/v3";
	private static String inputV2Path = "C:/DAPI/Ghosting-Workshop/migration/v2";
	private static final String soapOpeningBody = "<soap:Body>";
	private static final String soapClosingBody = "</soap:Body>";
	private static final String HTTP_TYPE = "HTTP";
	private static final String XML_TYPE = "XML";
	private static final String REQUEST_TAG = "requestPath: ";
	private static final String RESPONSE_TAG = "responsePath: ";
	protected static final String CLOSING_XML = "\"";
	private static final String OPENING_XML = "<";
	private static final String SERVICE_XML = "Service";
	private static final String SERVICE_CLOSE_XML = "Service>";
	private static final String NAME_XML = "name>";
	private static final String ID_XML = "id=\"";
	private static final String VERSION_XML = "version=\"";

	private static final String SERVICE_ID = "{{serviceId}}";
	private static final String SERVICE_VERSION = "{{serviceVersion}}";
	private static final String SERVICE_NAME = "{{serviceName}}";

	public void migrateGhostFiles() { 
		if (migrating.equalsIgnoreCase("true")) {
			File v2Directory = new File(inputV2Path);
			File v3Directory = new File(outputV3Path);
			v3Directory.delete();
			try {
				if (!inputV2Path.equals(outputV3Path)) {
					FileUtils.copyDirectory(v2Directory, v3Directory);
				}
			} catch (IOException e) {
				log.error("Error while copy the directory %s to v3 directory %s", v2Directory, v3Directory);
			}
	
			for (File testDirectory : v3Directory.listFiles()) {
				if (log.isInfoEnabled()) {
					log.info("Test Id folder is " + testDirectory.getName());
				}
				migrateMessagesFiles(testDirectory.getName());
			}
		}
	}

	private void migrateMessagesFiles(String testId) {
		String messagesFilePath = outputV3Path + "/" + testId + "/messages";
		String httpMessageTemplatePath = ghostingReadDirectory + "template/http_message.yml";
		String httpMessageTemplateContent = "";
		try {
			httpMessageTemplateContent = FileUtils.readFileToString(new File(httpMessageTemplatePath));
		} catch (IOException e1) {
			log.error("Error reading the template file " + httpMessageTemplatePath);
		}
		File messagesDirectory = new File(messagesFilePath);
		if (messagesDirectory.exists()) {
			for (File message : messagesDirectory.listFiles()) {
				log.info("Message file name is " + message.getName());
				try {
					String messageFileContent = FileUtils.readFileToString(message);
					if (StringUtils.contains(messageFileContent, XML_TYPE)) {
						migrateSoapFiles(messageFileContent, message, testId);
					} else if (StringUtils.contains(messageFileContent, HTTP_TYPE)) {
						migrateHttpFiles(messageFileContent, message, httpMessageTemplateContent);
					}
				} catch (IOException e) {
					log.error("Error during writing of request file");
				}
			}
		}
	}
	
	private void migrateSoapFiles(String messageFileContent, File messageFile, String testId) {
		updateMessageFileContent(messageFileContent, messageFile, testId);
		migrateSoapRequestFile(messageFileContent);
		migrateSoapResponseFile(messageFileContent);
		if (messageFile.getName().contains("SEI_1AXML")) {
			deleteFile(messageFile);
			return;
		}
	}
	
	private void migrateSoapRequestFile(String messageFileContent) {
		String requestPath = StringUtils.substringBetween(messageFileContent, REQUEST_TAG, ".mu");
		if (requestPath != null) {
			File requestFile = new File(outputV3Path + "/" + requestPath + ".mu");
			if (requestFile.getName().contains("SEI_1AXML")) {
				deleteFile(requestFile);
				return;
			}
			updateSoapFileContent(requestFile);
		}
	}
	
	private void migrateSoapResponseFile(String messageFileContent) {
		String responsePath = StringUtils.substringBetween(messageFileContent, RESPONSE_TAG, ".mu"); 
		File responseFile = new File(outputV3Path + "/" + responsePath + ".mu");
		if (responseFile.getName().contains("SEI_1AXML")) {
			deleteFile(responseFile);
			return;
		}
		updateSoapFileContent(responseFile);
	}
	
	private void updateSoapFileContent(File file) {
		log.info("File is " + file.getName());
		try {
			String fileContent = FileUtils.readFileToString(file);
			String soapBody = StringUtils.substringBetween(fileContent, soapOpeningBody, soapClosingBody);
			if (StringUtils.isNotEmpty(soapBody)) {
				FileUtils.writeStringToFile(file, soapBody);
			}
		} catch (IOException e) {
			log.error("Error during updateSoapFileContent " + file.getName());
		}
	}

	private void updateMessageFileContent(String messageFileContent, File messageFile, String testId) {

		String serviceTemplatePath = ghostingReadDirectory + "template/response_service.yml";
		log.info("Service template path " + serviceTemplatePath);
		String templateContent = "";
		try {
			templateContent = FileUtils.readFileToString(new File(serviceTemplatePath));
		} catch (IOException e) {
			log.error("Error reading the Service template file" + serviceTemplatePath);
		}

		String responsePath = StringUtils.substringAfter(messageFileContent, RESPONSE_TAG);
		File responseFile = new File(outputV3Path + "/" + responsePath);
		
		templateContent = StringUtils.replace(templateContent, "{{responsePath}}", responsePath);
		templateContent = updateServiceTag(responseFile, templateContent);

		/*if (messageFileContent.contains("variables:")) {
			String variable = StringUtils.substringBetween(messageFileContent, "delay: 0", "requestPath:");
			variable = "delay: 0" + "\n" + variable;
			templateContent = StringUtils.replace(templateContent, "delay: 0", variable);
		}*/

		if (StringUtils.isNotEmpty(templateContent)) {
			messageFileContent = StringUtils.replace(messageFileContent, "delay: 0", templateContent);
		}
		int indexOfResponsePath = messageFileContent.indexOf("responsePath");
		int lastIndexofMu = messageFileContent.lastIndexOf(".mu");
		try {
			String responsePathContent = messageFileContent.substring(indexOfResponsePath, lastIndexofMu + 3); 
			if (StringUtils.isNotEmpty(responsePathContent)) {
				messageFileContent = StringUtils.replace(messageFileContent, responsePathContent, "");
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			log.error(messageFileContent);
		}
		
		messageFileContent = StringUtils.replace(messageFileContent, testId + "/" , "");
		if (log.isDebugEnabled()) {
			log.info("Message file content after replacement is " + messageFileContent);
		}
		try {
			FileUtils.writeStringToFile(messageFile, messageFileContent); 
		} catch (IOException e) {
			log.error("Error occured during updating the message file content " + messageFile.getAbsolutePath());
		}
	}

	private String updateServiceTag(File responseFile, String templateContent) {
		String fileContent = "";
		try {
			fileContent = FileUtils.readFileToString(responseFile);
		} catch (IOException e) {
			log.error("Error while creating service tag in responseFile" + responseFile);
			return fileContent;
		}
		String serviceString = StringUtils.substringBetween(fileContent, SERVICE_XML, SERVICE_CLOSE_XML);
		String serviceId = StringUtils.substringBetween(serviceString, ID_XML, CLOSING_XML);
		String serviceName = StringUtils.substringBetween(serviceString, NAME_XML, OPENING_XML);
		String serviceVersion = StringUtils.substringBetween(serviceString, VERSION_XML, CLOSING_XML);
		log.info("Service details are %s , %s , %s, %s", serviceId, serviceName, serviceString, serviceVersion);
		
		if (StringUtils.isNotEmpty(serviceId)) {
			templateContent = StringUtils.replace(templateContent, SERVICE_ID, serviceId);
			templateContent = StringUtils.replace(templateContent, SERVICE_NAME, serviceName);
			templateContent = StringUtils.replace(templateContent, SERVICE_VERSION, serviceVersion);
		} else {
			templateContent = StringUtils.replace(templateContent, SERVICE_ID, "11.3");
			templateContent = StringUtils.replace(templateContent, SERVICE_NAME, "TTR_DisplayTripRS");
			templateContent = StringUtils.replace(templateContent, SERVICE_VERSION, "WS");
		}
		if (log.isDebugEnabled()) {
			log.debug("Template content after replacement of service tag is " + templateContent);
		}
		return templateContent;
	}

	private void migrateHttpFiles(String messageFileContent, File messageFile, String httpMessageTemplateContent) {
		String trxnb = StringUtils.substringBetween(messageFileContent, "trxnb: ", "service:");
		if (StringUtils.isNoneEmpty(trxnb)) {
			trxnb = trxnb.trim();
		}
		String httpBody = StringUtils.substringBetween(messageFileContent, "{\"", "\"}");
		log.info("Http file name is " + messageFile.getName());
		log.info("Http trxnb is " + trxnb);
		log.info("Http body is " + httpBody);

		httpMessageTemplateContent = StringUtils.replace(httpMessageTemplateContent, "{{trxnb}}", trxnb);
		httpMessageTemplateContent = StringUtils.replace(httpMessageTemplateContent, "{{httpBody}}", httpBody);
		try {
			FileUtils.writeStringToFile(messageFile, httpMessageTemplateContent);
		} catch (IOException e) {
			log.error("Error writing http message file");
		}

		String requestPath = StringUtils.substringBetween(messageFileContent, REQUEST_TAG, ".mu");
		File requestFile = new File(outputV3Path + "/" + requestPath + ".mu");
		deleteFile(requestFile);

		String responsePath = StringUtils.substringAfter(messageFileContent, RESPONSE_TAG);
		File responseFile = new File(outputV3Path + "/" + responsePath);
		try {
			FileUtils.forceDelete(responseFile);
		} catch (IOException e) {
			log.error("Error deleting http response file " + responseFile.getName());
		}
	}

	private void deleteFile(File file) {
		log.info("Deleted file name is " + file.getName());
		try {
			FileUtils.forceDelete(file);
		} catch (IOException e) {
			log.error("Error deleting http request file " + file.getName());
		}
	}

	public void setMigrating(String migrating) {
		this.migrating = migrating;
	}
	
	public String getMigrating() {
		return migrating;
	}

	public void setV2Directory(String v2Directory) {
		this.inputV2Path = v2Directory;
	}

	public void setV3Directory(String v3Directory) {
		this.outputV3Path = v3Directory;
	}

	public void setGhostingReadDirectory(String ghostingReadDirectory) {
		this.ghostingReadDirectory = ghostingReadDirectory;
	}

}
