package com.amadeus.ghostingutils.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.amadeus.ghostingutils.datamodel.CollateralLogRefineDataModel;
import com.amadeus.ghostingutils.datamodel.MessageKey;
import com.amadeus.ghostingutils.datamodel.RestRequestDatamodel;
import com.amadeus.ghostingutils.datamodel.Variable;
import com.amadeus.ghostingutils.datamodel.Variables;
import com.amadeus.ghostingutils.utils.CommonConstants;
import com.amadeus.ghostingutils.utils.CommonUtils;

public class CollateralLogParser {

	private String ghostingReadDirectory;
	private String ghostingWriteDirectoryV2;

	private static final String REQUESTS = "requests/";
	private static final String RESPONSES = "responses/";
	private int verbIndex = 101;
	private String collateralTemplate;
	private String collateralTemplateV2;

	protected static final String CLOSING_XML = "\"";
	private static final String OPENING_XML = "<";
	private static final String NAME_XML = "name>";
	private static final String ID_XML = "id=\"";
	private static final String VERSION_XML = "version=\"";
	
	private static final String xmlTag = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

	private static Map<String, RestRequestDatamodel> restIds = new HashMap<>();

	public void parsecollateralLog(String collateralTemplateInput, String collateralTemplateInputV2, String testId,
			String officeId, boolean isUsr, Map<String, List<MessageKey>> trxnbToFileMap) {
		collateralTemplate = collateralTemplateInput;
		collateralTemplateV2 = collateralTemplateInputV2;
		parseColLog(testId, officeId, isUsr, trxnbToFileMap);
		verbIndex = 101;
	}

	public void parseColLog(String testId, String officeId, boolean isUsr,
			Map<String, List<MessageKey>> trxnbToFileMap) {
		List<String> allProjects = CommonUtils.loadAllProjectList(isUsr);
		for (String sproject : allProjects) {
			File ghostedDirectory = new File(ghostingReadDirectory + testId + "/collateralLogs/" + sproject);
			if (ghostedDirectory.exists()) {
				File[] files = ghostedDirectory.listFiles();
				for (File file : files) {
					processCollateralLogFiles(testId, officeId, trxnbToFileMap, file);
				}
			}
		}
	}

	private void processCollateralLogFiles(String testId, String officeId, Map<String, List<MessageKey>> trxnbToFileMap,
			File file) {
		Set<String> ids = new HashSet<>();
		Map<String, String> responsePayloads = new LinkedHashMap<>();
		Map<String, String> requestPayloads = new LinkedHashMap<>();
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
			String line = bufferedReader.readLine();
			while (line != null) {
				String id = StringUtils.substringAfter(line, "ID: ");
				if (line.startsWith("ID:")) {
					prepareRestDataModel(ids, responsePayloads, requestPayloads, bufferedReader, id);
				}
				line = bufferedReader.readLine();
			}
			int verbNumber = verbIndex;
			handlePayloads(testId, officeId, trxnbToFileMap, file, responsePayloads, requestPayloads, verbNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
		responsePayloads.clear();
	}

	private void handlePayloads(String testId, String officeId, Map<String, List<MessageKey>> trxnbToFileMap, File file,
			Map<String, String> responsePayloads, Map<String, String> requestPayloads, int verbNumber)
			throws IOException {
		StringBuilder payloadNotParsed = new StringBuilder();
		// Handle request payloads
		Map<String, String> payloadsToProcess = requestPayloads;
		Map<String, String> payloadsToSupport = responsePayloads;
		explorePayloads(testId, payloadNotParsed, file, payloadsToProcess, payloadsToSupport, officeId, trxnbToFileMap,
				REQUESTS);
		verbIndex = verbNumber;
		// Handle Response Payloads
		payloadsToProcess = responsePayloads;
		payloadsToSupport = requestPayloads;
		explorePayloads(testId, payloadNotParsed, file, payloadsToProcess, payloadsToSupport, officeId, trxnbToFileMap,
				RESPONSES);
		FileUtils.writeStringToFile(new File(ghostingWriteDirectoryV2 + "notparsed/PayLoadNotParsed.txt"),
				payloadNotParsed.toString(), (String) null);
	}

	private void prepareRestDataModel(Set<String> ids, Map<String, String> responsePayloads,
			Map<String, String> requestPayloads, BufferedReader bufferedReader, String id) throws IOException {
		boolean restCall = false;
		RestRequestDatamodel restDataModel = new RestRequestDatamodel();
		if (ids.add(id)) {
			restCall = addToReqPayloads(requestPayloads, bufferedReader, id, restDataModel);
		} else {
			addToResPayloads(responsePayloads, bufferedReader, id, restDataModel);
		}
		if (restCall) {
			restIds.put(id, restDataModel);
		}
	}

	private boolean addToReqPayloads(Map<String, String> requestPayloads, BufferedReader bufferedReader, String id,
			RestRequestDatamodel restDataModel) throws IOException {
		String line;
		boolean restCall = false;
		line = bufferedReader.readLine();
		while (line != null) {
			if (line.startsWith("Content-Type: application/json")) {
				restCall = true;
			}
			if (line.startsWith("Headers: ")) {
				String trxnb = StringUtils.substringBetween(line, "TRXNB=\"", "\"");
				restDataModel.setTrxnb(trxnb);
			}
			if (line.startsWith("Payload")) {
				String payload = StringUtils.substringAfter(line, "Payload: ");
				requestPayloads.put(id, payload);
				restDataModel.setRequestPayload(payload);
				break;
			}
			line = bufferedReader.readLine();

		}
		return restCall;
	}

	private void addToResPayloads(Map<String, String> responsePayloads, BufferedReader bufferedReader, String id,
			RestRequestDatamodel restDataModel) throws IOException {
		String line;
		line = bufferedReader.readLine();
		while (line != null) {
			if (line.startsWith("Payload")) {
				String payload = StringUtils.substringAfter(line, "Payload: ");
				responsePayloads.put(id, payload);
				restDataModel.setResponsePayload(payload);
				break;
			}
			line = bufferedReader.readLine();
		}
	}

	private void explorePayloads(String testId, StringBuilder payloadNotParsed, File file, Map<String, String> payloads,
			Map<String, String> otherPayloads, String officeId, Map<String, List<MessageKey>> trxnbToFileMap,
			String payloadType) throws IOException {
		for (Map.Entry<String, String> payload : payloads.entrySet()) {
			String otherPayload = otherPayloads.get(payload.getKey());
			String otherServiceDetails = extractPayloadServiceName(otherPayload, payloadType);
			String serviceDetails = extractPayloadServiceName(payload.getValue(), payloadType);
			String body = StringUtils.substringBetween(payload.getValue(), "<soap:Body>", "</soap:Body>");
			String trxnb = StringUtils.substringBetween(payload.getValue(), "TRXNB=\"", "\"");
			if (null == trxnb) {
				trxnb = StringUtils.substringBetween(otherPayload, "TRXNB=\"", "\"");
			}
			String actualService = generateServiceName(serviceDetails);
			String otherService = generateServiceName(otherServiceDetails);
			String trxnbForFileName = trxnb;
			if ((actualService == null && body != null)) {
				payloadNotParsed.append(file.getName()).append("   ---    ").append(payload).append("\n");
				continue;
			} else if (actualService != null && actualService.contains("SEI_1AXML_TERMINATE")) {
				continue;
			}

			String newFileName = trxnbForFileName + CommonConstants.UNDERSCORE_STRING + verbIndex
					+ CommonConstants.UNDERSCORE_STRING + actualService + ".mu";
			String newOtherFileName = trxnbForFileName + CommonConstants.UNDERSCORE_STRING + verbIndex
					+ CommonConstants.UNDERSCORE_STRING + otherService + ".mu";
			String updatedFile = null;

			if (body == null) {
				body = StringUtils.substringBetween(payload.getValue(), "\"binaryData\":\"", "\"}");
				if (REQUESTS.equals(payloadType) && restIds.containsKey(payload.getKey())) {
					parseRestCall(otherPayload, payload.getValue(), restIds.get(payload.getKey()).getTrxnb(), testId);
					continue;
				} else if (RESPONSES.equals(payloadType) && restIds.containsKey(payload.getKey()) && trxnb == null) {
					continue;
				} else {
					updatedFile = StringUtils.replace(collateralTemplate, CommonConstants.TRXNB_VARIABLE, trxnb);
					updatedFile = StringUtils.replace(updatedFile, "{{binaryData}}", body);
				}
			} else {
				collateralTemplate = FileUtils.readFileToString(new File(ghostingReadDirectory
						+ CommonConstants.TEMPLATE_DIR + officeId + CommonConstants.collateral_AIR_OFFERS_MU_FILE_NAME),
						(String) null);
				updatedFile = StringUtils.replace(collateralTemplate, "{{soapBody}}", body);
			}

			String absoluteNewFileReponseNameV2 = ghostingWriteDirectoryV2 + payloadType + newFileName;
			FileUtils.writeStringToFile(new File(absoluteNewFileReponseNameV2), updatedFile, (String) null);
			List<Variable> tids = null;
			String yml = "";
			if (body != null && body.contains(" TID=")) {
				tids = MuFileTIdParser.handleAndGetTidListFromCollateralMuFile(absoluteNewFileReponseNameV2,
						payload.getValue());
				yml = getYMLString(tids);
				String fileContentAfterVariableUpdate = FileUtils.readFileToString(new File(absoluteNewFileReponseNameV2), (String) null);
				if (fileContentAfterVariableUpdate.startsWith(xmlTag)) {
					fileContentAfterVariableUpdate = StringUtils.replace(fileContentAfterVariableUpdate, xmlTag, "");
					FileUtils.writeStringToFile(new File(absoluteNewFileReponseNameV2), fileContentAfterVariableUpdate, (String) null);
				}
			}
			String newFileNameV2 = trxnb + CommonConstants.UNDERSCORE_STRING + verbIndex
					+ CommonConstants.UNDERSCORE_STRING + actualService + ".yml";
			if (REQUESTS.equals(payloadType)) {
				body = writeMessageFile(testId, officeId, trxnbToFileMap, payload, trxnb, actualService, newFileName,
						newOtherFileName, yml, newFileNameV2, otherServiceDetails);
			}
			verbIndex++;
			CollateralLogRefineDataModel collateralLogRefineDataModel = getcollateralLogRefineDataModel(payloadType,
					serviceDetails, body, trxnb, actualService, newFileName, newOtherFileName, tids);
			MoxMatcherWriter.getInstance().addLogs(collateralLogRefineDataModel);
		}
	}

	private CollateralLogRefineDataModel getcollateralLogRefineDataModel(String payloadType, String serviceDetails,
			String body, String trxnb, String actualService, String newFileName, String newOtherFileName,
			List<Variable> tids) {
		CollateralLogRefineDataModel collateralLogRefineDataModel = null;
		if (REQUESTS.equals(payloadType)) {
			collateralLogRefineDataModel = new CollateralLogRefineDataModel(trxnb, body, serviceDetails, actualService,
					newFileName, newOtherFileName, tids, true);
		} else {
			collateralLogRefineDataModel = new CollateralLogRefineDataModel(trxnb, body, serviceDetails, actualService,
					newOtherFileName, newFileName, tids, false);
		}
		return collateralLogRefineDataModel;
	}

	private String writeMessageFile(String testId, String officeId, Map<String, List<MessageKey>> trxnbToFileMap,
			Map.Entry<String, String> payload, String trxnb, String actualService, String newFileName,
			String newOtherFileName, String yml, String newFileNameV2, String responseService) throws IOException {
		String body;
		String updatedFileV2 = null;
		body = StringUtils.substringBetween(payload.getValue(), "<soap:Body>", "</soap:Body>");
		FileUtils.copyFile(new File(ghostingReadDirectory + "template/" + officeId + "/collateral.yml"),
				new File(ghostingWriteDirectoryV2 + CommonConstants.MESSAGES_DIR + newFileNameV2));
		updatedFileV2 = prepareFileContent(testId, body, trxnb, actualService, newFileName, newOtherFileName, yml, responseService);
		String absoluteNewFileNameV2 = ghostingWriteDirectoryV2 + CommonConstants.MESSAGES_DIR + newFileNameV2;
		maintainTrxnbToFileMap(trxnbToFileMap, trxnb, actualService, newFileName, newFileNameV2);
		FileUtils.writeStringToFile(new File(absoluteNewFileNameV2), updatedFileV2, (String) null);
		return body;
	}

	static void maintainTrxnbToFileMap(Map<String, List<MessageKey>> trxnbToFileMap, String trxnb, String actualService,
			String newFileName, String newFileNameV2) {
		String key = trxnb + actualService;
		if (trxnbToFileMap.containsKey(key)) {
			trxnbToFileMap.get(key).add(new MessageKey(newFileNameV2, newFileName));
		} else {
			List<MessageKey> filesNames = new ArrayList<>();
			filesNames.add(new MessageKey(newFileNameV2, newFileName));
			trxnbToFileMap.put(key, filesNames);
		}
	}

	private String prepareFileContent(String testId, String body, String trxnb, String actualService,
			String newFileName, String newOtherFileName, String yml, String responseService) {
		String updatedFileV2 = StringUtils.replace(collateralTemplateV2, CommonConstants.TRXNB_VARIABLE, trxnb);
		String type = getType(body);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{variables}}", yml);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{type}}", type);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{testid}}", testId);

		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{mureqfile}}", newFileName);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{muresfile}}", newOtherFileName);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{id}}",
				actualService + "-" + type + "-" + trxnb + CommonConstants.UNDERSCORE_STRING + verbIndex);
		String serviceId = StringUtils.substringBetween(responseService, ID_XML, CLOSING_XML);
		String serviceName = StringUtils.substringBetween(responseService, NAME_XML, OPENING_XML);
		String serviceVersion = StringUtils.substringBetween(responseService, VERSION_XML, CLOSING_XML);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{serviceId}}", serviceId);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{serviceName}}", serviceName);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{serviceVersion}}", serviceVersion);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{service}}", actualService);
		return updatedFileV2;
	}

	private void parseRestCall(String responsePayload, String requestPayload, String trxnb, String testId) {
		try {
			printRestTemplateYML(responsePayload, trxnb, testId);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void printRestTemplateYML(String responsePayload, String trxnb, String testId) throws IOException {
		String absoluteNewFileNameV2;
		String restMessageTemplate = FileUtils.readFileToString(
				new File(new StringBuilder().append(ghostingReadDirectory)
						.append(CommonConstants.TEMPLATE_DIR + "restTemplate/").append("rpwreq.yml").toString()),
				(String) null);
		String updatedFile = StringUtils.replace(restMessageTemplate, CommonConstants.TRXNB_VARIABLE, trxnb);
		updatedFile = StringUtils.replace(updatedFile, "{{body}}", responsePayload);
		absoluteNewFileNameV2 = ghostingWriteDirectoryV2 + CommonConstants.MESSAGES_DIR + trxnb + "-rpwreq.yml";
		FileUtils.writeStringToFile(new File(absoluteNewFileNameV2), updatedFile, (String) null);
	}

	private static String generateServiceName(String serviceDetails) {
		String regex = "ns[0-9]+:";
		// Compiling the regular expression
		Pattern pattern = Pattern.compile(regex);
		String actualService = StringUtils.substringBetween(serviceDetails, "<name>", "</name>");
		if (null == actualService && serviceDetails != null) {
			Matcher matcher = pattern.matcher(serviceDetails);
			String serviceDetailsEnhanced = matcher.replaceAll("");
			actualService = StringUtils.substringBetween(serviceDetailsEnhanced, "<name>", "</name>");
		}
		return actualService;
	}

	private static String extractPayloadServiceName(String payload, String payloadType) {
		String regex = "ns[0-9]+:";
		// Compiling the regular expression
		Pattern pattern = Pattern.compile(regex);
		String serviceDetails = null;
		String tag = (REQUESTS.equals(payloadType)) ? "</FromRef>" : "</ToRef>";
		serviceDetails = StringUtils.substringBetween(payload, tag, CommonConstants.EXTENDED_DATA);
		if (null == serviceDetails) {
			serviceDetails = StringUtils.substringBetween(payload, tag, CommonConstants.MESSAGE_HEADER);
		}
		if (null == serviceDetails) {
			Matcher matcher = pattern.matcher(payload);
			String payloadEnhanced = matcher.replaceAll("");
			serviceDetails = StringUtils.substringBetween(payloadEnhanced, tag, CommonConstants.EXTENDED_DATA);
			if (null == serviceDetails) {
				serviceDetails = StringUtils.substringBetween(payloadEnhanced, tag, CommonConstants.MESSAGE_HEADER);
			}
		}

		return serviceDetails;
	}

	private static String getYMLString(List<Variable> tids) {
		String yml;
		Variables variables = new Variables(tids);
		Representer representer = new Representer();
		representer.addClassTag(Variable.class, Tag.MAP);
		representer.addClassTag(Variables.class, Tag.MAP);
		representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		representer.setDefaultFlowStyle(FlowStyle.BLOCK);
		Yaml yaml = new Yaml(representer);
		yml = yaml.dump(variables);
		return yml;
	}

	private static String getType(String body) {
		return "XML";
	}

	public void setGhostingReadDirectory(String ghostingReadDirectory) {
		this.ghostingReadDirectory = ghostingReadDirectory;
	}

	public void setGhostingWriteDirectoryV2(String ghostingWriteDirectoryV2) {
		this.ghostingWriteDirectoryV2 = ghostingWriteDirectoryV2;
	}
}
