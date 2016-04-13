package org.gbif.common.parsers.date;

/**
 * Hints are given to the parser to help to select the right sets of DateTimeFormatter
 */
public enum DateFormatHint {
  YMDT,
  YMD,
  DMY,
  MDY,
  YM,
  Y,
  /** date format used in Chinese */
  HAN,
  NONE
}
