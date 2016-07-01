package org.gbif.common.parsers.date;

/**
 * Factories for date parsing related instances.
 */
public class DateParsers {

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


}
