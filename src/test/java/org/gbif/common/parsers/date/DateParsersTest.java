package org.gbif.common.parsers.date;

import org.junit.Test;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeParseException;
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
    DateParsers.ISO_YEAR.parse("2016-10-45");
  }

  @Test(expected = DateTimeParseException.class)
  public void testParsersISOYearException2(){
    DateParsers.ISO_YEAR.parse("16");
  }

  @Test(expected = DateTimeParseException.class)
  public void testParsersISOYearException3(){
    DateParsers.ISO_YEAR.parse("2016-40");
  }

  //  -- ISO_YEAR_MONTH --
  @Test
  public void testParsersISOYearMonth(){
    assertNotNull(DateParsers.ISO_YEAR_MONTH.parse("2016-05"));
  }

  @Test(expected = DateTimeParseException.class)
  public void testParsersISOYearMonthException1(){
    DateParsers.ISO_YEAR_MONTH.parse("2016");
  }

  @Test(expected = DateTimeParseException.class)
  public void testParsersISOYearMonthException2(){
    DateParsers.ISO_YEAR_MONTH.parse("2016-07-25");
  }

  // see https://github.com/ThreeTen/threetenbp/issues/49#issuecomment-238017644
  @Test(expected = DateTimeException.class)
  public void testParsersISOYearMonthException3(){
    TemporalAccessor ta = DateParsers.ISO_YEAR_MONTH.parse("2016-40");
    System.out.println(YearMonth.from(ta));
  }

  //  -- ISO_LOCAL_PARTIAL_DATE --
  public void testIsoLocalPartialDateParser(){
    // to be implemented
  }

}
