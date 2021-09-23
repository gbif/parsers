/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.common.parsers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

/**
 * Utils class to parse numbers trying various locales so that dots and comma based formats are both supported.
 * All methods swallow exceptions and return null instead.
 */
public class NumberParser {

  private NumberParser() {

  }

  public static Double parseDouble(String x) {
    final String trimmed = x == null ? null : x.trim();
    if (StringUtils.isEmpty(trimmed)) return null;

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

  public static Integer parseInteger(String x) {
    final String trimmed = x == null ? null : x.trim();
    if (StringUtils.isEmpty(trimmed)) return null;

    try {
      return Integer.parseInt(x);
    } catch (NumberFormatException e) {
      NumberFormat format = DecimalFormat.getInstance(Locale.GERMANY);
      ParsePosition pos = new ParsePosition(0);
      // verify that the parsed position is at the end of the string!
      Number num = format.parse(trimmed, pos);
      if (num != null && trimmed.length() == pos.getIndex()) {
        return num.intValue();
      }
    }
    return null;
  }
}
