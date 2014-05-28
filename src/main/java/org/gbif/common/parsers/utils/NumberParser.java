package org.gbif.common.parsers.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import com.google.common.base.Strings;

/**
 * Utils class to parse numbers trying various locales so that dots and comma based formats are both supported.
 * All methods swallow exceptions and return null instead.
 */
public class NumberParser {

  private NumberParser() {

  }

  public static Double parseDouble(String x) {
    final String trimmed = x == null ? null : x.trim();
    if (Strings.isNullOrEmpty(trimmed)) return null;

    try {
      return Double.parseDouble(x);
    } catch (NumberFormatException e) {
      NumberFormat format = DecimalFormat.getInstance(Locale.GERMANY);
      ParsePosition pos = new ParsePosition(0);
      // verify that the parsed position is at the end of the string!
      Number num = format.parse(trimmed, pos);
      if (num != null && trimmed.length() == pos.getIndex()) {
        return num.doubleValue();
      }
    }
    return null;
  }
}
