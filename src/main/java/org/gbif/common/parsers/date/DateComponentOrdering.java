package org.gbif.common.parsers.date;

/**
 * Date component orderings are given to the parser to help to select the right set of DateTimeFormatters
 */
public enum DateComponentOrdering {
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

  /** ISO formats */
  public static DateComponentOrdering[] ISO_FORMATS = new DateComponentOrdering[]{YMDTZ, YMDT, YMD, YM, YW, YD, Y};

  /** Day-Month-Year formats */
  public static DateComponentOrdering[] DMY_FORMATS = new DateComponentOrdering[]{DMYT, DMY};

  /** Month-Day-Year formats, primarily used in the USA */
  public static DateComponentOrdering[] MDY_FORMATS = new DateComponentOrdering[]{MDYT, MDY};
}
