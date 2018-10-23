package org.gbif.common.parsers.date;

/**
 * Hints are given to the parser to help to select the right sets of DateTimeFormatter
 *
 * TODO maybe add the TimeZone YMDTZ
 */
public enum DateFormatHint {
  YMDT,
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
