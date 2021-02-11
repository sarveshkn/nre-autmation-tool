package com.amadeus.ghostingutils.datamodel;

public class Variable {

  String name;

  String xpath;

  public Variable(String name, String xpath) {
    super();
    this.name = name;
    this.xpath = xpath;
  }

  public Variable() {

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getXpath() {
    return xpath;
  }

  public void setXpath(String path) {
    this.xpath = path;
  }

}
