package org.gbif.common.parsers.date;

import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.ResolverStyle;
import org.threeten.bp.format.SignStyle;
import org.threeten.bp.temporal.ChronoField;

/**
 * Factories for date parsing related instances.
 */
public class DateParsers {

  /**
   * Do not use for now, see  DateParsersTest
   * Different with DateTimeFormatter.ISO_LOCAL_DATE is that DAY and MONTH are optionals
   */
  public static final DateTimeFormatter ISO_LOCAL_PARTIAL_DATE =
          new DateTimeFormatterBuilder()
                  .appendValue(ChronoField.YEAR, 4, 4, SignStyle.NEVER)
                  .optionalStart().appendLiteral('-')
                  .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
                  .optionalStart().appendLiteral('-')
                  .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NEVER)
                  .optionalEnd()
                  .optionalEnd()
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

  /**
   * Do not use for now, see  DateParsersTest
   * Returns a new instance of DateTimeParser with support for partial local dates LocalDate, YearMonth and Year
   * @return DateTimeParser
   */
  public static DateTimeParser isoLocalPartialDateParser(){
    return new DateTimeParser(ISO_LOCAL_PARTIAL_DATE, null, DateFormatHint.YMD, 4);
  }

}
