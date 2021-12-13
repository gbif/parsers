/*
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
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import static org.gbif.common.parsers.utils.CSVBasedAssertions.assertTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit testing for ThreeTenNumericalDateParser.
 */
public class ThreeTenNumericalDateParserTest {

  private static final String BADDATE_TEST_FILE = "parse/date/threeten_bad_date_tests.txt";
  private static final String LOCALDATE_TEST_FILE = "parse/date/threeten_localdate_tests.txt";
  private static final String LOCALDATETIME_TEST_FILE = "parse/date/threeten_localdatetime_tests.txt";
  private static final String LOCALDATETIME_TZ_TEST_FILE = "parse/date/local_datetime_tz_tests.txt";

  private static final int RAW_VAL_IDX = 0;
  private static final int YEAR_VAL_IDX = 1;
  private static final int MONTH_VAL_IDX = 2;
  private static final int DAY_VAL_IDX = 3;
  private static final int HOUR_VAL_IDX = 4;
  private static final int MIN_VAL_IDX = 5;
  private static final int SEC_VAL_IDX = 6;
  private static final int NS_VAL_IDX = 7;
  private static final int TZ_VAL_IDX = 8;

  private static final TemporalParser PARSER = ThreeTenNumericalDateParser.newInstance();

  @Test
  public void testLocalDateFromFile() {
    assertTestFile(LOCALDATE_TEST_FILE,
            new Function<String[], Void>() {
              @Nullable
              @Override
              public Void apply(@Nullable String[] row) {
                String raw = row[RAW_VAL_IDX];
                try {
                  int year = Integer.parseInt(row[YEAR_VAL_IDX]);
                  int month = Integer.parseInt(row[MONTH_VAL_IDX]);
                  int day = Integer.parseInt(row[DAY_VAL_IDX]);
                  ParseResult<TemporalAccessor> result = PARSER.parse(raw);
                  assertNotNull(result.getPayload(), raw + " generated null payload");
                  assertEquals(LocalDate.of(year, month, day), LocalDate.from(result.getPayload()),
                      "Test file rawValue: " + raw);
                } catch (NumberFormatException nfEx){
                  fail("Error while parsing the test input file content." + nfEx.getMessage());
                }
                return null;
              }
            });
  }

  @Test
  public void testLocalDateTimeFromFile() {
    assertTestFile(LOCALDATETIME_TEST_FILE,
            new Function<String[], Void>() {
              @Nullable
              @Override
              public Void apply(@Nullable String[] row) {
                String raw = row[RAW_VAL_IDX];
                try {
                  int year = Integer.parseInt(row[YEAR_VAL_IDX]);
                  int month = Integer.parseInt(row[MONTH_VAL_IDX]);
                  int day = Integer.parseInt(row[DAY_VAL_IDX]);
                  int hour = Integer.parseInt(row[HOUR_VAL_IDX]);
                  int minute = Integer.parseInt(row[MIN_VAL_IDX]);
                  int second = Integer.parseInt(row[SEC_VAL_IDX]);
                  int nanosecond = Integer.parseInt(row[NS_VAL_IDX]);

                  ParseResult<TemporalAccessor> result = PARSER.parse(raw);
                  assertNotNull(result.getPayload(), raw + " generated null payload");

                  assertEquals(LocalDateTime.of(year, month, day, hour, minute, second, nanosecond), LocalDateTime.from(result.getPayload()),
                      "Test file rawValue: " + raw);
                } catch (NumberFormatException nfEx) {
                  fail("Error while parsing the test input file content." + nfEx.getMessage());
                }
                return null;
              }
            });
  }

  @Test
  public void testLocalDateTimeWithTimezoneFromFile() {
    assertTestFile(LOCALDATETIME_TZ_TEST_FILE,
            new Function<String[], Void>() {
              @Nullable
              @Override
              public Void apply(@Nullable String[] row) {
                String raw = row[RAW_VAL_IDX];
                try {
                  int year = Integer.parseInt(row[YEAR_VAL_IDX]);
                  int month = Integer.parseInt(row[MONTH_VAL_IDX]);
                  int day = Integer.parseInt(row[DAY_VAL_IDX]);
                  int hour = Integer.parseInt(row[HOUR_VAL_IDX]);
                  int minute = Integer.parseInt(row[MIN_VAL_IDX]);
                  int second = Integer.parseInt(row[SEC_VAL_IDX]);
                  int millisecond = Integer.parseInt(row[NS_VAL_IDX]);
                  String zoneId = row[TZ_VAL_IDX];

                  ParseResult<TemporalAccessor> result = PARSER.parse(raw);
                  assertNotNull(result.getPayload(), raw + " generated null payload");

                  assertEquals(ZonedDateTime.of(year, month, day, hour, minute, second,
                                            0, ZoneId.of(zoneId)).with(ChronoField.MILLI_OF_SECOND, millisecond), ZonedDateTime.from(result.getPayload()),
                      "Test file rawValue: " + raw);
                } catch (NumberFormatException nfEx) {
                  fail("Error while parsing the test input file content." + nfEx.getMessage());
                }
                return null;
              }
            });
  }

  @Test
  public void testBadDateFromFile() {
    assertTestFile(BADDATE_TEST_FILE,
            new Function<String[], Void>() {
              @Nullable
              @Override
              public Void apply(@Nullable String[] row) {
                assertEquals(ParseResult.STATUS.FAIL, PARSER.parse(row[RAW_VAL_IDX]).getStatus(), "Test file rawValue: " + row[RAW_VAL_IDX]);
                return null;
              }
            });
  }

  @Test
  public void testParseAsLocalDateTime() {
    ThreeTenNumericalDateParser parser = ThreeTenNumericalDateParser.newInstance(Year.of(1900));

    // month first with 2 digits years >_<
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("122178").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12/21/78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12\\21\\78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12.21.78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12-21-78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12_21_78").getPayload()));

    // month/year alone
    assertEquals(YearMonth.of(1978, 12), YearMonth.from(parser.parse("1978-12").getPayload()));

    // year alone
    assertEquals(Year.of(1978), Year.from(parser.parse("1978").getPayload()));
    // assertEquals(Year.of(1978), Year.from(parser.parse("78").getPayload()));
  }

  @Test
  public void testParseAsLocalDateByDateParts() {
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(PARSER.parse("1978", "12", "21").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(PARSER.parse(1978, 12, 21).getPayload()));

    assertEquals(LocalDate.of(1978, Month.DECEMBER, 1), LocalDate.from(PARSER.parse("1978", "12", "1").getPayload()));
    assertEquals(YearMonth.of(1978, 12), YearMonth.from(PARSER.parse("1978", "12", null).getPayload()));
    assertEquals(YearMonth.of(1978, 12), YearMonth.from(PARSER.parse(1978, 12, null).getPayload()));
    assertEquals(Year.of(1978), Year.from(PARSER.parse("1978", "", null).getPayload()));

    // providing the day without the month should result in an error
    assertEquals(ParseResult.STATUS.FAIL, PARSER.parse("1978", "", "2").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, PARSER.parse(1978, null, 2).getStatus());
  }

//  @Ignore("not implemented yet")
//  @Test
//  public void testUnsupportedFormat() {
//    ParseResult<TemporalAccessor> result = PARSER.parse("16/11/1996 0:00:00");
//
//    System.out.println(PARSER.parse("1996-11-16T00:00:00"));
//  }

  @Test
  public void testParsePreserveZoneOffset() {
    ZonedDateTime offsetDateTime = ZonedDateTime.of(1978, 12, 21, 0, 0, 0,
            0, ZoneOffset.of("+02:00"));
    assertEquals(offsetDateTime, PARSER.parse("1978-12-21T00:00:00+02:00").getPayload());
  }

  @Test
  public void testAmbiguousDates() {
    ParseResult<TemporalAccessor> result;

    // Ambiguous
    result = PARSER.parse("1/2/1996");
    assertNull(result.getPayload());
    assertEquals(2, result.getAlternativePayloads().size());
    assertTrue(result.getAlternativePayloads().contains(LocalDate.of(1996, 2, 1)));
    assertTrue(result.getAlternativePayloads().contains(LocalDate.of(1996, 1, 2)));

    // Not ambiguous
    result = PARSER.parse("1/1/1996");
    assertEquals(LocalDate.of(1996, 1, 1), result.getPayload());
    assertNull(result.getAlternativePayloads());

    result = PARSER.parse("31/1/1996");
    assertEquals(LocalDate.of(1996, 1, 31), result.getPayload());
    assertNull(result.getAlternativePayloads());

    // Dots aren't used in America.
    result = PARSER.parse("4.5.1996");
    assertEquals(LocalDate.of(1996, 5, 4), result.getPayload());
    assertNull(result.getAlternativePayloads());
  }

  @Test
  public void testBlankDates() {
    assertEquals(ParseResult.STATUS.FAIL, PARSER.parse(" ").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, PARSER.parse("").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, PARSER.parse(null).getStatus());
  }
}
