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


import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;

/**
 * Factories for date parsing related instances.
 */
public class DateParsers {

  /**
   * {@link DateTimeFormatter} for ISO Year-MonthOfYear-DayOfMonth (4 digits year)
   * This formatter does NOT handle time and timezone.
   */
  public static final DateTimeFormatter ISO_LOCAL_DATE =
          new DateTimeFormatterBuilder()
                  .appendValue(ChronoField.YEAR, 4, 4, SignStyle.NEVER)
                  .appendLiteral('-')
                  .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
                  .appendLiteral('-')
                  .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NEVER)
                  .toFormatter().withResolverStyle(ResolverStyle.STRICT);

  /**
   * {@link DateTimeFormatter} for ISO Year (4 digits)
   */
  public static final DateTimeFormatter ISO_YEAR =
          new DateTimeFormatterBuilder()
                  .appendValue(ChronoField.YEAR, 4, 4, SignStyle.NEVER)
                  .toFormatter().withResolverStyle(ResolverStyle.STRICT);

  /**
   * {@link DateTimeFormatter} for ISO Year-MonthOfYear (4 digits year)
   */
  public static final DateTimeFormatter ISO_YEAR_MONTH =
          new DateTimeFormatterBuilder()
                  .appendValue(ChronoField.YEAR, 4, 4, SignStyle.NEVER)
                  .appendLiteral('-')
                  .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
                  .toFormatter().withResolverStyle(ResolverStyle.STRICT);


//  public static final DateTimeMultiParser ISO_PARSER = DateTimeParserBuilder.newMultiParserListBuilder()
//          .appendDateTimeFormatter(ISO_LOCAL_DATE, DateComponentOrdering.YMD, 8)
//          .appendDateTimeFormatter(ISO_YEAR_MONTH, DateComponentOrdering.YM, 6)
//          .appendDateTimeFormatter(ISO_YEAR, DateComponentOrdering.Y, 4)
//          .build();

  /**
   * Return a pre-configured {@link TemporalParser} instance.
   */
  public static TemporalParser defaultTemporalParser() {
    return new TextDateParser();
  }

  /**
   * Get a new instance of the default implementation of TemporalParser that handles
   * numerical dates.
   */
  public static TemporalParser defaultNumericalDateParser() {
    return ThreeTenNumericalDateParser.newInstance();
  }

}
