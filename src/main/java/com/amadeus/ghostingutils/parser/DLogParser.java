package com.amadeus.ghostingutils.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.amadeus.ghostingutils.datamodel.CollateralLogRefineDataModel;
import com.amadeus.ghostingutils.datamodel.MessageKey;
import com.amadeus.ghostingutils.datamodel.Variable;
import com.amadeus.ghostingutils.datamodel.Variables;
import com.amadeus.ghostingutils.utils.CommonConstants;
import com.amadeus.ghostingutils.utils.CommonUtils;

public class DLogParser {

	private String ghostingReadDirectory;
	private String ghostingWriteDirectoryV2;

	protected static final String CLOSING_XML = "\"";
	private static final String OPENING_XML = "<";
	private static final String NAME_XML = "name>";
	private static final String ID_XML = "id=\"";
	private static final String VERSION_XML = "version=\"";

	private int verbIndex = 1;

	public void parseDLog(String testId, String officeId, boolean isUsr, Map<String, List<MessageKey>> trxnbToFileMap)
			throws IOException {
		String ghostedDirectoryTemplateName = ghostingReadDirectory + "template/" + officeId;
		String dlogsTemplate = FileUtils.readFileToString(new File(ghostedDirectoryTemplateName + "/dlogs.mu"),
				(String) null);
		String dlogsTemplateV2 = FileUtils.readFileToString(new File(ghostedDirectoryTemplateName + "/dlogs.yml"),
				(String) null);
		List<String> allProjects = CommonUtils.loadAllProjectList(isUsr);
		for (String sproject : allProjects) {
			File ghostedDirectory = new File(ghostingReadDirectory + testId + "/dlogs/" + sproject);
			if (ghostedDirectory.exists()) {
				File[] files = ghostedDirectory.listFiles();
				if (files != null && files.length != 0) {
					for (File file : files) {
						processFile(dlogsTemplate, dlogsTemplateV2, testId, officeId, trxnbToFileMap, file);
					}
				}
			}
		}
	}

	private void processFile(String dlogsTemplate, String dlogsTemplateV2, String testId, String officeId,
			Map<String, List<MessageKey>> trxnbToFileMap, File file) {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
			String line = bufferedReader.readLine();
			while (line != null) {

				String parsedVerbResponse = StringUtils.substringBetween(line,
						CommonConstants.VERB_TAG + verbIndex + ">", "</Verb_" + verbIndex + ">");

				while (parsedVerbResponse != null) {
					parsedVerbResponse = processVerbResponse(dlogsTemplate, dlogsTemplateV2, testId, officeId,
							trxnbToFileMap, file, line, parsedVerbResponse);

				}
				line = bufferedReader.readLine();
				verbIndex = 1;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String processVerbResponse(String dlogsTemplate, String dlogsTemplateV2, String testId, String officeId,
			Map<String, List<MessageKey>> trxnbToFileMap, File file, String line, String parsedVerbResponse)
			throws IOException {
		// Extracting Response params
		String parsedResponse = StringUtils.substringBetween(parsedVerbResponse, "<Response>", "</Response>");
		String responseService = StringUtils.substringBetween(parsedResponse, "</ToRef>", "<ExtendedData>");
		String responseServiceName = StringUtils.substringBetween(responseService, "<name>", "</name></Service>");
		String responseBody = StringUtils.substringBetween(parsedResponse, "<Body>", "</Body>");

		// Extracting Request params
		String parsedRequest = StringUtils.substringBetween(line, CommonConstants.VERB_TAG + verbIndex + "><Request>",
				"</Request>");
		String requestServiceName = StringUtils.substringBetween(parsedRequest, "<Service><Name>", "</Name>");
		String requestBody = StringUtils.substringBetween(parsedRequest, "<Body>", "</Body>");

		String trxnb = StringUtils.substringBetween(parsedResponse, "TRXNB=\"", "\"/");

		String newRequestFileName = trxnb + "_" + verbIndex + "_" + requestServiceName + ".mu";
		String newResponseFileName = trxnb + "_" + verbIndex + "_" + responseServiceName + ".mu";
		String newRequestFileNameV2 = trxnb + "_" + verbIndex + "_" + requestServiceName + ".mu";
		String newMessageFileNameV2 = trxnb + "_" + verbIndex + "_" + requestServiceName + ".yml";
		String newResponseFileNameV2 = trxnb + "_" + verbIndex + "_" + responseServiceName + ".mu";
		CollateralLogParser.maintainTrxnbToFileMap(trxnbToFileMap, trxnb, requestServiceName, newRequestFileNameV2,
				newMessageFileNameV2);

		doFileCopies(officeId, newRequestFileNameV2, newMessageFileNameV2, newResponseFileNameV2);

		String updatedResponseFile = dlogsTemplate;
		String updatedRequestFile = updatedResponseFile;
		updatedResponseFile = StringUtils.replace(updatedResponseFile, CommonConstants.SOAP_BODY_VARIABLE,
				responseBody);

		updatedRequestFile = StringUtils.replace(updatedRequestFile, CommonConstants.SOAP_BODY_VARIABLE, requestBody);

		String absoluteNewRQFileNameV2 = ghostingWriteDirectoryV2 + "requests/" + newRequestFileNameV2;
		String absoluteNewRSFileNameV2 = ghostingWriteDirectoryV2 + "responses/" + newResponseFileNameV2;

		List<Variable> tids = null;
		String yml = "";
		if (responseBody.contains(" TID=")) {
			tids = MuFileTIdParser.handleAndGetTidListFromMuFile(absoluteNewRSFileNameV2, file.getAbsolutePath());
			yml = getYMLString(tids);
		}
		FileUtils.writeStringToFile(new File(absoluteNewRQFileNameV2), updatedRequestFile, (String) null);
		FileUtils.writeStringToFile(new File(absoluteNewRSFileNameV2), updatedResponseFile, (String) null);
		generateV2YMLS(dlogsTemplateV2, requestServiceName, requestBody, trxnb, newRequestFileNameV2, yml,
				newResponseFileNameV2, testId, true, newMessageFileNameV2, responseService);
		verbIndex++;

		CollateralLogRefineDataModel collateralLogRefineDataModel = new CollateralLogRefineDataModel(trxnb,
				responseBody, responseServiceName, requestServiceName, newRequestFileName, newResponseFileName, tids,
				true);

		MoxMatcherWriter.getInstance().addLogs(collateralLogRefineDataModel);

		parsedVerbResponse = StringUtils.substringBetween(line, CommonConstants.VERB_TAG + verbIndex + ">",
				"</Verb_" + verbIndex + ">");
		return parsedVerbResponse;
	}

	private void doFileCopies(String officeId, String newRequestFileNameV2, String newMessageFileNameV2,
			String newResponseFileNameV2) throws IOException {
		FileUtils.copyFile(
				new File(ghostingReadDirectory + CommonConstants.TEMPLATE_DIR + officeId
						+ CommonConstants.DLOGS_MU_FILE_NAME),
				new File(ghostingWriteDirectoryV2 + "requests/" + newRequestFileNameV2));
		FileUtils.copyFile(
				new File(ghostingReadDirectory + CommonConstants.TEMPLATE_DIR + officeId
						+ CommonConstants.DLOGS_MU_FILE_NAME),
				new File(ghostingWriteDirectoryV2 + "responses/" + newResponseFileNameV2));
		FileUtils.copyFile(new File(ghostingReadDirectory + CommonConstants.TEMPLATE_DIR + officeId + "/dlogs.yml"),
				new File(ghostingWriteDirectoryV2 + "messages/" + newMessageFileNameV2));
	}

	private void generateV2YMLS(String dlogsTemplateV2, String parsedRequestService, String body, String trxnb,
			String newFileNameV2, String yml, String newFileName, String testId, boolean isRequest,
			String newMessageFileNameV2, String responseService) throws IOException {
		String updatedFileV2 = StringUtils.replace(dlogsTemplateV2, "{{trxnb}}", trxnb);
		updatedFileV2 = StringUtils.replace(updatedFileV2, CommonConstants.SOAP_BODY_VARIABLE, body);
		String type = getType(body);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{type}}", type);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{id}}",
				trxnb + "_" + verbIndex + "_" + parsedRequestService + "-" + type);
		updatedFileV2 = StringUtils.replace(updatedFileV2, CommonConstants.SERVICE_VARIABLES, parsedRequestService);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{variables}}", yml);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{mureqfile}}", newFileNameV2);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{muresfile}}", newFileName);
		String serviceId = StringUtils.substringBetween(responseService, ID_XML, CLOSING_XML);
		String serviceName = StringUtils.substringBetween(responseService, NAME_XML, OPENING_XML);
		String serviceVersion = StringUtils.substringBetween(responseService, VERSION_XML, CLOSING_XML);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{serviceId}}", serviceId);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{serviceName}}", serviceName);
		updatedFileV2 = StringUtils.replace(updatedFileV2, "{{serviceVersion}}", serviceVersion);
		
		if (isRequest) {
			String absoluteNewFileNameV2 = ghostingWriteDirectoryV2 + "messages/" + newMessageFileNameV2;
			FileUtils.writeStringToFile(new File(absoluteNewFileNameV2), updatedFileV2, (String) null);
		}
	}

	private String getYMLString(List<Variable> tids) {
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

	private String getType(String body) {
		if (body.contains("</")) {
			return "XML";
		}
		return "";
	}

	public void setGhostingReadDirectory(String ghostingReadDirectory) {
		this.ghostingReadDirectory = ghostingReadDirectory;
	}

	public void setGhostingWriteDirectoryV2(String ghostingWriteDirectoryV2) {
		this.ghostingWriteDirectoryV2 = ghostingWriteDirectoryV2;
	}
}
