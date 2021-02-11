package com.amadeus.ghostingutils.utils;

/**
 * Constants used across Ghosting Util V2
 */
public class CommonConstants {

  /**
   * private constructor to hide the explicit public one
   */
  private CommonConstants() {
    throw new IllegalStateException("No instances for you for CommonConstants!");
  }

  // File Name Constants
  public static final String TEST_CONFIG_JSON_FILE_NAME = "test-config.json";
  public static final String MATCHER_JSON_FILE_NAME = "matcher.json";
  public static final String TEST_CONFIG_TEMPLATE_JSON_FILE_NAME = "test_config-template.json";
  public static final String collateral_AIR_OFFERS_MU_FILE_NAME = "/collateral_airOffers.mu";
  public static final String DLOGS_MU_FILE_NAME = "/dlogs.mu";

  // Directory Constants
  public static final String TEMPLATE_DIR = "template/";
  public static final String MESSAGES_DIR = "messages/";

  // Special Characters
  public static final String COLON_STRING = ":";
  public static final String SLASH_STRING = "/";
  public static final String EMPTY_STRING = "";
  public static final String COMMA_STRING = ",";
  public static final String UNDERSCORE_STRING = "_";
  public static final String AMPERSAND_STRING = "&";
  public static final String EQUALS_STRING = "=";
  public static final String QUESTION_STRING = "?";

  // Most used literals
  public static final String AY_STRING = "AY";
  public static final String VALUE_STRING = "value";
  public static final String KEY_STRING = "key";

  // XML Constants
  public static final String EXTENDED_DATA = "<ExtendedData>";
  public static final String MESSAGE_HEADER = "</MessageHeader>";
  public static final String SOAP_BODY = "soap:Body";
  public static final String VERB_TAG = "<Verb_";

  // Variables
  public static final String TRXNB_VARIABLE = "{{trxnb}}";
  public static final String SOAP_BODY_VARIABLE = "{{soapBody}}";
  public static final String SERVICE_VARIABLES = "{{service}}";
}
