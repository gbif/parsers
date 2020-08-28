package org.gbif.common.parsers.date;

import static junit.framework.TestCase.assertEquals;
import static org.gbif.common.parsers.date.DateComponentOrdering.DMY_FORMATS;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAccessor;
import org.gbif.common.parsers.core.ParseResult;

import org.junit.Test;

public class PrefTexDateParserTest {

  private final TemporalParser parser =  PrefTextDateParser.getInstance(DMY_FORMATS);

  @Test
  public void testTextDateParsing(){
    ParseResult<TemporalAccessor> parseResult =  parser.parse("1/2/2000");
    assertEquals(LocalDate.of(2000, Month.FEBRUARY, 1), LocalDate.from(parseResult.getPayload()));
    parseResult =  parser.parse("2000-2-1");
    assertEquals(LocalDate.of(2000, Month.FEBRUARY, 1), LocalDate.from(parseResult.getPayload()));
    parseResult = parser.parse("23-March-1969");
    assertEquals(LocalDate.of(1969, Month.MARCH, 23), LocalDate.from(parseResult.getPayload()));
  }
}
