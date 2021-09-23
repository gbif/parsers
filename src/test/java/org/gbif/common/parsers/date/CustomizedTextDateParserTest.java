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

import org.gbif.common.parsers.core.ParseResult;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.TemporalAccessor;

import org.junit.jupiter.api.Test;

import static org.gbif.common.parsers.date.DateComponentOrdering.DMY_FORMATS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CustomizedTextDateParserTest {

  private final TemporalParser parser =  CustomizedTextDateParser.getInstance(DMY_FORMATS);

  /**
   * Can parse: 1969-02
   * Cannot parse: 1969/02
   */
  @Test
  public void testTextDateParsing(){
    ParseResult<TemporalAccessor> parseResult =  parser.parse("1/2/2000");
    assertEquals(LocalDate.of(2000, Month.FEBRUARY, 1), LocalDate.from(parseResult.getPayload()));
    parseResult =  parser.parse("2000-2-1");
    assertEquals(LocalDate.of(2000, Month.FEBRUARY, 1), LocalDate.from(parseResult.getPayload()));
    parseResult = parser.parse("23-March-1969");
    assertEquals(LocalDate.of(1969, Month.MARCH, 23), LocalDate.from(parseResult.getPayload()));
    parseResult = parser.parse("1969-02");
    assertEquals("1969-02", YearMonth.from(parseResult.getPayload()).toString());
  }

  /**
   * Because TextDateParse does not initiate ThreeTenNumericalDateParse with start year, like 19
   */
  @Test
  public void shouldFail(){
    ParseResult<TemporalAccessor> parseResult = parser.parse("7/10/08");
    assertFalse(parseResult.isSuccessful());
    parseResult = parser.parse("17/10/78");
    assertFalse(parseResult.isSuccessful());
    parseResult = parser.parse("1969/02");
    assertFalse(parseResult.isSuccessful());
  }
}
