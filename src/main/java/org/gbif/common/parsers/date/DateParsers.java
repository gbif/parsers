package org.gbif.common.parsers.date;

/**
 * Factories for date parsing related instances.
 */
public class DateParsers {

  /**
   * Get a new instance of the default implementation of NumericalDateParser.
   *
   * @return
   */
  public static NumericalDateParser defaultNumericalDateParser(){
    return ThreeTenNumericalDateParser.newInstance();
  }
}
