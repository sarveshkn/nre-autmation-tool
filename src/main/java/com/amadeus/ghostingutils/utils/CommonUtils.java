package com.amadeus.ghostingutils.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Util class for Ghosting Tool V2
 */
public class CommonUtils {

  /**
   * private constructor to hide the explicit public one
   */
  private CommonUtils() {
    throw new IllegalStateException("No instances for you for CommonUtils!");
  }

  /**
   * Used for string cleaning up from special characters
   *
   * @param inputString String
   * @return String
   */
  public static String cleanUpString(String inputString) {
    return inputString.replaceAll("[{}()]", "").trim();
  }

  public static List<String> loadAllProjectList(boolean isUsr) {
    if (isUsr) {
      return Arrays.asList("JBOSS_SER_RTLS", "JBOSS_CRT_RTLS", "JBOSS_DS2_RTLS", "JBOSS_ODR_RTLS");
    } else {
      return Arrays.asList("rtl-crt", "rtl-odr", "rtl-ser", "rtl-ds2");
    }
  }

}
