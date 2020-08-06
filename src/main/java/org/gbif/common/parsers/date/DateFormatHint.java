package org.gbif.common.parsers.date;

/**
 * Hints are given to the parser to help to select the right sets of DateTimeFormatter
 */
public enum DateFormatHint {
  YMDTZ,
  YMDT,
  EU_DMYT,
  US_MDYT,
  YMD,
  DMY,
  MDY,
  YM,
  YW,
  YD,
  Y,
  /** date format used in Chinese */
  HAN,
  NONE
}
