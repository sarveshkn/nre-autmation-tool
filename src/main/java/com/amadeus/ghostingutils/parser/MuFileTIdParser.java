package com.amadeus.ghostingutils.parser;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.amadeus.ghostingutils.datamodel.Variable;
import com.amadeus.ghostingutils.utils.CommonConstants;

public class MuFileTIdParser {

  private static List<Variable> variables;

  private static boolean isCollateral = false;

  private MuFileTIdParser() {
    throw new IllegalStateException("No instances for you for MuFileTIdParser!");
  }

  private static void printNote(NodeList nodeList, NodeList nListRequest) {

    for (int count = 0; count < nodeList.getLength(); count++) {
      Node tempNode = nodeList.item(count);
      // make sure it's element node.
      if (tempNode.getNodeType() == Node.ELEMENT_NODE && tempNode.hasAttributes()) {

        // get attributes names and values
        NamedNodeMap nodeMap = tempNode.getAttributes();

        for (int i = 0; i < nodeMap.getLength(); i++) {
          Node node = nodeMap.item(i);
          if (node.getNodeName().equals("TID")) {
            String xPath = CommonConstants.EMPTY_STRING;
            xPath = getFullRequestXPath(node, nListRequest, xPath);
            Variable variable = new Variable(node.getNodeValue(), xPath);
            String tid = "{{" + node.getNodeValue() + "}}";
            node.setNodeValue(tid);
            variables.add(variable);
          }
        }
      }
      if (tempNode.hasChildNodes()) {
        // loop again if has child nodes
        printNote(tempNode.getChildNodes(), nListRequest);
      }
    }
  }

  private static String getFullRequestXPath(Node nodeTolook, NodeList nListRequest, String xpath) {
    for (int count = 0; count < nListRequest.getLength(); count++) {
      Node tempNode = nListRequest.item(count);

      // make sure it's element node.
      if (tempNode.getNodeType() == Node.ELEMENT_NODE && tempNode.hasAttributes()) {

        // get attributes names and values
        NamedNodeMap nodeMap = tempNode.getAttributes();

        for (int i = 0; i < nodeMap.getLength(); i++) {
          Node node = nodeMap.item(i);
          if (node.getNodeName().equals(nodeTolook.getNodeName())
              && node.getNodeValue().equals(nodeTolook.getNodeValue())) {
            return getFullXPath(node);
          }
        }
      }
      if (tempNode.hasChildNodes()) {
        // loop again if has child nodes
        xpath = getFullRequestXPath(nodeTolook, tempNode.getChildNodes(), xpath);
      }
    }
    return xpath;
  }

  public static List<Variable> handleAndGetTidListFromMuFile(String absoluteNewFileName, String dlogPath) {
    variables = new ArrayList<>();
    isCollateral = false;

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

    DocumentBuilder builder;
    try {
      builder = factory.newDocumentBuilder();

      Document doc = builder.parse(new File(absoluteNewFileName));
      Document docRequest = builder.parse(new File(dlogPath));
      // Normalize the XML Structure; It's just too important !!
      doc.getDocumentElement().normalize();
      NodeList nList = doc.getElementsByTagName(CommonConstants.SOAP_BODY);
      NodeList nListRequest = docRequest.getElementsByTagName("Request");

      transformStreamResult(absoluteNewFileName, doc, nList, nListRequest);
    } catch (IOException | SAXException | ParserConfigurationException | TransformerException e) {
      e.printStackTrace();
    }

    return variables;
  }

  public static List<Variable> handleAndGetTidListFromCollateralMuFile(String absoluteNewFileName, String request) {
    variables = new ArrayList<>();
    isCollateral = true;

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    DocumentBuilder builder;
    try {
      builder = factory.newDocumentBuilder();

      Document docResponse = builder.parse(new File(absoluteNewFileName));
      Document docRequest = builder.parse(new InputSource(new StringReader(request)));
      // Normalize the XML Structure; It's just too important !!
      docResponse.getDocumentElement().normalize();
      docRequest.getDocumentElement().normalize();
      NodeList nList = docResponse.getChildNodes();
      NodeList nListRequest = docRequest.getChildNodes();

      transformStreamResult(absoluteNewFileName, docResponse, nList, nListRequest);
    } catch (IOException | SAXException | ParserConfigurationException | TransformerException e) {
      e.printStackTrace();
    }

    return variables;
  }

  private static void transformStreamResult(String absoluteNewFileName, Document docResponse, NodeList nList,
      NodeList nListRequest) throws TransformerException {
    if (null != nList) {
      printNote(nList, nListRequest);
    }

    // write the DOM object to the file
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

    Transformer transformer = transformerFactory.newTransformer();
    DOMSource domSource = new DOMSource(docResponse);

    StreamResult streamResult = new StreamResult(new File(absoluteNewFileName));
    transformer.transform(domSource, streamResult);
  }

  public static String getFullXPath(Node n) {
    int nodeToIgnore = isCollateral ? 3 : 5;
    // abort early
    if (null == n)
      return null;

    // declarations
    Node parent;
    Deque<Node> hierarchy = new ArrayDeque<>();
    StringBuilder buffer = new StringBuilder();

    // push element on stack
    hierarchy.push(n);

    parent = setParent(n);

    while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
      // push on stack
      hierarchy.push(parent);

      // get parent of parent
      parent = parent.getParentNode();
    }
    buffer.append(CommonConstants.SLASH_STRING);
    // construct xpath
    Node obj;
    int head = 0;
    while (!hierarchy.isEmpty()) {
      obj = hierarchy.pop();
      if (head > nodeToIgnore) {

        if (obj.getNodeType() == Node.ELEMENT_NODE) {
          handleElementNode(buffer, obj);
        } else if (obj.getNodeType() == Node.ATTRIBUTE_NODE) {
          handleAttributeNode(buffer, obj, "/@");
        }
      } else {
        head++;
      }
    }
    // return buffer
    return buffer.toString();
  }

  private static void handleAttributeNode(StringBuilder buffer, Node node, String s) {
    buffer.append(s);
    buffer.append(getNodeName(node.getNodeName()));
  }

  private static void handleElementNode(StringBuilder buffer, Node node) {
    Element e = (Element)node;

    // is this the root element?
    if (buffer.length() == 0) {
      // root element - simply append element name
      buffer.append(node.getNodeName());
    } else {
      // child element - append slash and element name
      handleAttributeNode(buffer, node, CommonConstants.SLASH_STRING);

      if (node.hasAttributes()) {
        // see if the element has a name or id attribute
        if (e.hasAttribute("id")) {
          // id attribute found - use that
          buffer.append("[@id='").append(e.getAttribute("id")).append("']");
        } else if (e.hasAttribute("name")) {
          // name attribute found - use that
          buffer.append("[@name='").append(e.getAttribute("name")).append("']");
        }
      }
      // no known attribute we could use - get sibling index
      getSiblingIndex(buffer, node);

    }
  }

  private static void getSiblingIndex(StringBuilder buffer, Node node) {
    int prevSiblings = 1;
    boolean appendIndex = false;
    Node previousSibling = node.getPreviousSibling();
    while (null != previousSibling) {
      if (previousSibling.getNodeType() == node.getNodeType() && previousSibling.getNodeName()
          .equalsIgnoreCase(
              node.getNodeName())) {
        appendIndex = true;
        prevSiblings++;

      }
      previousSibling = previousSibling.getPreviousSibling();
    }
    if (appendIndex || (null != node.getNextSibling()
        && (node.getNodeName().equals(node.getNextSibling().getNodeName()))))
      buffer.append("[").append(prevSiblings).append("]");
  }

  private static Node setParent(Node n) {
    Node parent;
    switch (n.getNodeType()) {
      case Node.ATTRIBUTE_NODE:
        parent = ((Attr)n).getOwnerElement();
        break;
      case Node.ELEMENT_NODE:
      case Node.DOCUMENT_NODE:
        parent = n.getParentNode();
        break;
      default:
        throw new IllegalStateException("Unexpected Node type" + n.getNodeType());
    }
    return parent;
  }

  private static String getNodeName(String nodeName) {
    if (nodeName.contains(CommonConstants.COLON_STRING)) {
      String[] splitStrings = nodeName.split(CommonConstants.COLON_STRING);
      return splitStrings[1];
    } else {
      return nodeName;
    }
  }

}
