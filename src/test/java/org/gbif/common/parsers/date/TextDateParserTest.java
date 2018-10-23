package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.ParseResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.TemporalAccessor;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TextDateParser}.
 */
public class TextDateParserTest {

  private TextDateParser TEXTDATE_PARSER = new TextDateParser();

  @Test
  public void testTextDateParsing(){
    ParseResult<TemporalAccessor> parseResult = TEXTDATE_PARSER.parse("2nd jan. 2018");
    assertEquals(LocalDate.of(2018, Month.JANUARY, 2), LocalDate.from(parseResult.getPayload()));

    parseResult = TEXTDATE_PARSER.parse("2018 january 2");
    assertEquals(LocalDate.of(2018, Month.JANUARY, 2), LocalDate.from(parseResult.getPayload()));

    parseResult = TEXTDATE_PARSER.parse("2018-01-02");
    assertEquals(LocalDate.of(2018, Month.JANUARY, 2), LocalDate.from(parseResult.getPayload()));

    parseResult = TEXTDATE_PARSER.parse("2018/01/02");
    assertEquals(LocalDate.of(2018, Month.JANUARY, 2), LocalDate.from(parseResult.getPayload()));

    parseResult = TEXTDATE_PARSER.parse("2018年1月2日");
    assertEquals(LocalDate.of(2018, Month.JANUARY, 2), LocalDate.from(parseResult.getPayload()));
  }

  @Test
  public void testTextDateTimeParsing(){
    ParseResult<TemporalAccessor> parseResult = TEXTDATE_PARSER.parse("1978-1-1T00:00");
    assertEquals(LocalDate.of(1978, Month.JANUARY, 1), LocalDate.from(parseResult.getPayload()));

    parseResult = TEXTDATE_PARSER.parse("1978-01-01T02:12:43Z");
    assertEquals(LocalDate.of(1978, Month.JANUARY, 1), LocalDate.from(parseResult.getPayload()));
  }

  @Test
  public void testDateParts(){
    ParseResult<TemporalAccessor> parseResult = TEXTDATE_PARSER.parse("2018.0", "1.0", "02.0");
    assertEquals(LocalDate.of(2018, Month.JANUARY, 2), LocalDate.from(parseResult.getPayload()));

    parseResult = TEXTDATE_PARSER.parse("2018.0", "jan", "02.0");
    assertEquals(LocalDate.of(2018, Month.JANUARY, 2), LocalDate.from(parseResult.getPayload()));

    parseResult = TEXTDATE_PARSER.parse("2018.0", "jan", "");
    assertEquals(YearMonth.of(2018, Month.JANUARY), YearMonth.from(parseResult.getPayload()));
  }

  @Test
  public void testTextDateParsingNullEmpty(){
    ParseResult<TemporalAccessor> parseResult = TEXTDATE_PARSER.parse("");
    assertFalse(parseResult.isSuccessful());

    parseResult = TEXTDATE_PARSER.parse(null);
    assertFalse(parseResult.isSuccessful());
  }

  @Test
  public void testTextDateParsingInvalidDate(){
    ParseResult<TemporalAccessor> parseResult = TEXTDATE_PARSER.parse("32nd jan. 2018");
    assertFalse(parseResult.isSuccessful());

    parseResult = TEXTDATE_PARSER.parse("2nd n/a 2018");
    assertFalse(parseResult.isSuccessful());

    parseResult = TEXTDATE_PARSER.parse("15 jan-fev 2018");
    assertFalse(parseResult.isSuccessful());
  }

  @Test
  public void testTextDatePartsParsing(){
    ParseResult<TemporalAccessor> parseResult = TEXTDATE_PARSER.parse("1879", "11", "1");
    assertEquals(LocalDate.of(1879, Month.NOVEMBER, 1), LocalDate.from(parseResult.getPayload()));

    parseResult = TEXTDATE_PARSER.parse("1879", "a", "1");
    assertFalse(parseResult.isSuccessful());
  }

  @Test
  public void testDateRanges() {
    // Some of the many ways of specifying a whole year.
    // So many cases aren't really needed, as we're only taking the start of the interval anyway.
    // Just with years
    testDateRangeStart(1999, 1, 1, "1999");
    testDateRangeStart(1999, 1, 1, "1999/2000");
    testDateRangeStart(1999, 1, 1, "1999/P1Y");
    // With years and months
    testDateRangeStart(1999, 1, 1, "1999-01/1999-12");
    testDateRangeStart(1999, 1, 1, "1999-01/12");
    testDateRangeStart(1999, 1, 1, "1999-01/P12M");
    // With years and days
    testDateRangeStart(1999, 1, 1, "1999-001/1999-365");
    testDateRangeStart(1999, 1, 1, "1999-001/P365D");
    // With years, months and days
    testDateRangeStart(1999, 1, 1, "1999-01-01/1999-12-31");
    testDateRangeStart(1999, 1, 1, "1999-01-01/12-31");
    testDateRangeStart(1999, 1, 1, "1999-01-01/P365D");
    // With years, months, days and a time
    testDateRangeStart(1999, 1, 1, "1999-01-01T00/1999-12-31T24");
    testDateRangeStart(1999, 1, 1, "1999-01-01T00:00/1999-12-31T24:00");
    testDateRangeStart(1999, 1, 1, "1999-01-01T00:00:00/1999-12-31T24:00:00");
    testDateRangeStart(1999, 1, 1, "1999-01-01T00:00:00+0000/1999-12-31T24:00:00+0000");

    // A whole month.
    // With years and months
    testDateRangeStart(1999, 2, 1, "1999-02");
    testDateRangeStart(1999, 2, 1, "1999-02/1999-03");
    testDateRangeStart(1999, 2, 1, "1999-02/03");
    testDateRangeStart(1999, 2, 1, "1999-02/P1M");
    // With years and days
    testDateRangeStart(1999, 2, 1, "1999-032/1999-59");
    testDateRangeStart(1999, 2, 1, "1999-032/P28D");
    // With years, months and days
    testDateRangeStart(1999, 2, 1, "1999-02-01/1999-02-28");
    testDateRangeStart(1999, 2, 1, "1999-02-01/28");
    testDateRangeStart(1999, 2, 1, "1999-02-01/P28D");
    // With years, months, days and a time
    testDateRangeStart(1999, 2, 1, "1999-02-01T00/1999-02-28T24");
    testDateRangeStart(1999, 2, 1, "1999-02-01T00:00/1999-02-28T24:00");
    testDateRangeStart(1999, 2, 1, "1999-02-01T00:00:00/1999-02-28T24:00:00");
    testDateRangeStart(1999, 2, 1, "1999-02-01T00:00:00+0000/1999-02-28T24:00:00+0000");

    // A period of several days, from 2004-12-30 to 2005-03-13
    // With years and days
    testDateRangeStart(2004, 12, 30, "2004-365/2005-072");
    testDateRangeStart(2004, 12, 30, "2004-365/P74D");
    // With years, months and days
    testDateRangeStart(2004, 12, 30, "2004-12-30/2005-03-13");
    //testDateRangeStart(2004, 12, 30, "2004-12-30/31");
    testDateRangeStart(2004, 12, 30, "2004-12-30/P74D");
    // With years, months, days and a time
    testDateRangeStart(2004, 12, 30, "2004-12-30T00/2005-03-13T24");
    testDateRangeStart(2004, 12, 30, "2004-12-30T00:00/2005-03-13T24:00");
    testDateRangeStart(2004, 12, 30, "2004-12-30T00:00:00/2005-03-13T24:00:00");
    testDateRangeStart(2004, 12, 30, "2004-12-30T00:00:00+0000/2005-03-13T24:00:00+0000");

    // A period of hours
    testDateRangeStart(2004, 12, 30, 8, 0, 0, "2004-12-30T08/2005-03-13T18");
    testDateRangeStart(2004, 12, 30, 8, 0, 0, "2004-12-30T08:00/2005-03-13T18:00");
    testDateRangeStart(2004, 12, 30, 8, 0, 0, "2004-12-30T08:00:00/2005-03-13T18:00:00");
    testDateRangeStart(2004, 12, 30, 8, 0, 0, "2004-12-30T08:00:00+0000/2005-03-13T18:00:00+0000");
  }

  /**
   * These aren't ranges, they are non-ISO dates that could be confused for them.
   */
  @Test
  public void testDateNotARange(){
    assertFalse(TEXTDATE_PARSER.parse("1986/03").isSuccessful());
    assertFalse(TEXTDATE_PARSER.parse("1986/03-13").isSuccessful());
  }

  private void testDateRangeStart(int y, int m, int d, String inDate) {
    testDateRangeStart(y, m, d, 0, 0, 0, inDate);
  }

  private void testDateRangeStart(int y, int m, int d, int hh, int mm, int ss, String inDate) {
    ParseResult<TemporalAccessor> parseResult = TEXTDATE_PARSER.parse(inDate);
    assertTrue(parseResult.isSuccessful());
    assertEquals(LocalDateTime.of(y, m, d, hh, mm, ss), TemporalAccessorUtils.toEarliestLocalDateTime(parseResult.getPayload(), true));
  }
}
