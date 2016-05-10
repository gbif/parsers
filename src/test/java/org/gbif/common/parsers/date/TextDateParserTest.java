package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.ParseResult;

import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.YearMonth;
import org.threeten.bp.temporal.TemporalAccessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

    parseResult = TEXTDATE_PARSER.parse("2018年1月2日");
    assertEquals(LocalDate.of(2018, Month.JANUARY, 2), LocalDate.from(parseResult.getPayload()));
  }

  @Test
  public void testTextDateTimeParsing(){
    ParseResult<TemporalAccessor> parseResult = TEXTDATE_PARSER.parse("1978-1-1T00:00");
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
}
