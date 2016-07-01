package org.gbif.common.parsers.date;

/**
 * Factories for date parsing related instances.
 */
public class DateParsers {

  /**
   * Get a new instance of the default implementation of TemporalParser that handles
   * numerical dates.
   *
   * @return
   */
  public static TemporalParser defaultNumericalDateParser(){
    return ThreeTenNumericalDateParser.newInstance();
  }

}
