package org.gbif.common.parsers.date;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validate predefined parsers from {@link DateParsers}.
 */
public class DateParsersTest {

  //  -- DateParsers.ISO_YEAR --
  @Test
  public void testParsersISOYear() {
    assertNotNull(DateParsers.ISO_YEAR.parse("2016"));
  }

  @Test
  public void testParsersISOYearException1() {
    assertThrows(
        DateTimeParseException.class,
        () -> DateParsers.ISO_YEAR.parse("2016-10-45"));
  }

  @Test
  public void testParsersISOYearException2() {
    assertThrows(
        DateTimeParseException.class,
        () -> DateParsers.ISO_YEAR.parse("16"));
  }

  @Test
  public void testParsersISOYearException3() {
    assertThrows(
        DateTimeParseException.class,
        () -> DateParsers.ISO_YEAR.parse("2016-40"));
  }

  //  -- ISO_YEAR_MONTH --
  @Test
  public void testParsersISOYearMonth() {
    assertNotNull(DateParsers.ISO_YEAR_MONTH.parse("2016-05"));
  }

  @Test
  public void testParsersISOYearMonthException1() {
    assertThrows(
        DateTimeParseException.class,
        () -> DateParsers.ISO_YEAR_MONTH.parse("2016"));
  }

  @Test
  public void testParsersISOYearMonthException2() {
    assertThrows(
        DateTimeParseException.class,
        () -> DateParsers.ISO_YEAR_MONTH.parse("2016-07-25"));
  }

  // see https://github.com/ThreeTen/threetenbp/issues/49#issuecomment-238017644
  @Test
  public void testParsersISOYearMonthException3() {
    assertThrows(
        DateTimeException.class,
        () -> DateParsers.ISO_YEAR_MONTH.parse("2016-40", YearMonth::from));
  }

  //  -- ISO_LOCAL_PARTIAL_DATE --
  @Disabled
  public void testIsoLocalPartialDateParser() {
    // to be implemented
  }

}
