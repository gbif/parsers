package org.gbif.common.parsers.date;

/**
 * Hints are given to the parser to help to select the right sets of DateTimeFormatter
 */
public enum DateFormatHint {
  /** Year, month, day, time, zone */
  YMDTZ,
  /** Year, month, day, time */
  YMDT,
  /** Year, month, day */
  YMD,
  /** Day, month, year, time */
  DMYT,
  /** Day, month, year */
  DMY,
  /** Month, day, year, time */
  MDYT,
  /** Month, day, year */
  MDY,
  /** Year, month */
  YM,
  /** Year, week number */
  YW,
  /** Year, day of year */
  YD,
  /** Year */
  Y,
  /** Chinese, Japanese, Korean: Year, month, day. */
  HAN,
  NONE;

  public static DateFormatHint[] ISO_FORMATS = new DateFormatHint[]{YMDTZ, YMDT, YMD, YM, YW, YD, Y};

  public static DateFormatHint[] DMY_FORMATS = new DateFormatHint[]{DMYT, DMY};

  public static DateFormatHint[] MDY_FORMATS = new DateFormatHint[]{MDYT, MDY};
}
