package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.ParseResult;

import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.TemporalAccessor;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link TextDateParser}.
 */
public class TextDateParserTest {

  private TextDateParser TEXTDATE_PARSER = new TextDateParser();

  @Test
  public void testTextDateParsing(){
    ParseResult<TemporalAccessor> parseResult = TEXTDATE_PARSER.parse("2nd jan. 2018");
    assertEquals(LocalDate.of(2018, Month.JANUARY, 2), LocalDate.from(parseResult.getPayload()));

    parseResult = TEXTDATE_PARSER.parse("2018-01-02");
    assertEquals(LocalDate.of(2018, Month.JANUARY, 2), LocalDate.from(parseResult.getPayload()));
  }
}
