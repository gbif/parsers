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
  /** The default set of unambiguous, year-month-day-like orderings. */
  ISO_ETC;

  /** ISO formats */
  public static DateComponentOrdering[] ISO_FORMATS = new DateComponentOrdering[]{YMDTZ, YMDT, YMD, YM, YW, YD, Y};

  /** Day-Month-Year formats */
  public static DateComponentOrdering[] DMY_FORMATS = new DateComponentOrdering[]{DMYT, DMY};

  /** Month-Day-Year formats, primarily used in the USA */
  public static DateComponentOrdering[] MDY_FORMATS = new DateComponentOrdering[]{MDYT, MDY};
}
