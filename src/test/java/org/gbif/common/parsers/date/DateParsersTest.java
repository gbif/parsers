package org.gbif.common.parsers.date;

import org.junit.Ignore;
import org.junit.Test;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.format.ResolverStyle;
import org.threeten.bp.format.SignStyle;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.TemporalAccessor;

import static org.junit.Assert.assertNotNull;

/**
 * Validate predefined parsers from {@link DateParsers}
 */
public class DateParsersTest {

  //  -- DateParsers.ISO_YEAR --
  @Test
  public void testParsersISOYear(){
    assertNotNull(DateParsers.ISO_YEAR.parse("2016"));
  }

  @Test(expected = DateTimeParseException.class)
  public void testParsersISOYearException1(){
    TemporalAccessor ta = DateParsers.ISO_YEAR.parse("2016-10-45");
    YearMonth.from(ta);
  }

  @Test(expected = DateTimeParseException.class)
  public void testParsersISOYearException2(){
    DateParsers.ISO_YEAR.parse("16");
  }


  //  -- ISO_YEAR_MONTH --
  @Test
  public void testParsersISOYearMonth(){
    DateParsers.ISO_YEAR_MONTH.parse("2016-05");
  }

  @Test(expected = DateTimeParseException.class)
  public void testParsersISOYearMonthException1(){
    assertNotNull(DateParsers.ISO_YEAR_MONTH.parse("2016"));
  }

  @Test(expected = DateTimeParseException.class)
  public void testParsersISOYearMonthException2(){
    DateParsers.ISO_YEAR_MONTH.parse("2016-07-25");
  }

  //  -- ISO_LOCAL_PARTIAL_DATE --
  @Test
  public void testIsoLocalPartialDateParser(){
    DateTimeFormatter parser = DateParsers.ISO_LOCAL_PARTIAL_DATE;

    TemporalAccessor ta = parser.parse("2016");
    assertNotNull(ta);

    ta = parser.parse("2016-07");
    assertNotNull(ta);

    ta = parser.parse("2016-07-25");
    assertNotNull(ta);

    //test wrong date that "works" https://github.com/ThreeTen/threetenbp/issues/49
    ta = parser.parse("2016-40");
    assertNotNull(ta);

    //test wrong date
   // ta = parser.parse("2016-40-10");
  //  assertNotNull(ta);

  }


  // see https://github.com/ThreeTen/threetenbp/issues/49
  @Test
  public void testISOOptionalMonthOfYear(){
    DateTimeFormatter isoOptionalDayOfMonthFormatter =
            new DateTimeFormatterBuilder()
                    .appendValue(ChronoField.YEAR, 4, 4, SignStyle.NEVER)
                    .optionalStart()
                    .appendLiteral('-')
                    .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
                    .optionalEnd()
                    .toFormatter().withResolverStyle(ResolverStyle.STRICT);
    TemporalAccessor ta = isoOptionalDayOfMonthFormatter.parse("2016-40");
  }

  @Ignore(" see https://github.com/ThreeTen/threetenbp/issues/49")
  public void testISOOptionalDayOfMonth(){
    DateTimeFormatter isoOptionalDayOfMonthFormatter =
            new DateTimeFormatterBuilder()
                    .appendValue(ChronoField.YEAR, 4, 4, SignStyle.NEVER)
                    .appendLiteral('-')
                    .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
                    .optionalStart()
                    .appendLiteral('-')
                    .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NEVER)
                    .optionalEnd()
                    .toFormatter().withResolverStyle(ResolverStyle.STRICT);
    isoOptionalDayOfMonthFormatter.parse("2016-10-40");
  }

}
