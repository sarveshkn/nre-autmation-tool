package com.amadeus.ghostingutils.formatter;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Utility Class for formatting XML
 *
 */
public class XmlFormatter {

	public static String transformXML(String xml, String indent) {
		try {
			if (xml.equals("")) {
				return "";
			}
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();
			XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//text()[normalize-space(.) = '']");
			NodeList blankTextNodes = (NodeList) xpath.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < blankTextNodes.getLength(); i++) {
				blankTextNodes.item(i).getParentNode().removeChild(blankTextNodes.item(i));
			}
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent);
			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			String modifiedRequestXML = result.getWriter().toString();
			int lastIndexOfStartingTag = modifiedRequestXML.lastIndexOf("</");
			int lastIndexOfEndingTag = modifiedRequestXML.lastIndexOf(">");
			String tag = StringUtils.substring(modifiedRequestXML, lastIndexOfStartingTag, lastIndexOfEndingTag);
			String replaceTag = "";
			int spaceCount = Integer.parseInt(indent);
			while (spaceCount > 0) {
				replaceTag += " "; 
				spaceCount--;
			}
			replaceTag += tag;
			modifiedRequestXML = StringUtils.replace(modifiedRequestXML, tag, replaceTag);
			return modifiedRequestXML;
		} catch (Exception e) {
			throw new IllegalStateException("Cannot parse xml " + xml, e);
		}
	}

	public static String removeNameSpaces(String xml, String indent) {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent);
			System.out.println("before xml = " + xml);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource inputSource = new InputSource(new StringReader(xml));
			Document xmlDoc = builder.parse(inputSource);
			Node root = xmlDoc.getDocumentElement();
			NodeList rootchildren = root.getChildNodes();
			Element newroot = xmlDoc.createElement(root.getNodeName());
			for (int i = 0; i < rootchildren.getLength(); i++) {
				newroot.appendChild(rootchildren.item(i).cloneNode(true));
			}
			xmlDoc.replaceChild(newroot, root);
			DOMSource requestXMLSource = new DOMSource(xmlDoc.getDocumentElement());
			StringWriter requestXMLStringWriter = new StringWriter();
			StreamResult requestXMLStreamResult = new StreamResult(requestXMLStringWriter);
			transformer.transform(requestXMLSource, requestXMLStreamResult);
			String modifiedRequestXML = requestXMLStringWriter.toString();
			int lastIndexOfStartingTag = modifiedRequestXML.lastIndexOf("</");
			int lastIndexOfEndingTag = modifiedRequestXML.lastIndexOf(">");
			String tag = StringUtils.substring(modifiedRequestXML, lastIndexOfStartingTag, lastIndexOfEndingTag);
			String replaceTag = "";
			int spaceCount = Integer.parseInt(indent);
			while (spaceCount > 0) {
				replaceTag += " "; 
				spaceCount--;
			}
			replaceTag += tag;
			modifiedRequestXML = StringUtils.replace(modifiedRequestXML, tag, replaceTag);
			return modifiedRequestXML;
		} catch (Exception e) {
			System.out.println("Could not parse message as xml: " + e.getMessage());
		}
		return "";
	}

}
