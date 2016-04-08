package org.gbif.common.parsers.date;

import org.apache.commons.lang3.StringUtils;

/**
 * DateNormalizer contract is to take String representing Year, Month and Day and return the corresponding
 * Integer as {@link NormalizedYearMonthDay} if possible.
 *
 * No validation will be applied to the normalized values.
 *
 */
public class DateNormalizer {

  // Dictionary for interpreting string months
  // Important that the FIRST is the canonical form, followed
  // by abbreviations.  Be very careful not to add any ambiguous terms
  // such as "J" "A" etc.  Do not add a "." at the end of abbreviations
  // as normalization will automatically do this.
  protected static final String[][] MONTHS = {
          {"January", "Jan", "J", "Ene", "Ja"},  // Ene. is abbreviated Spanish
          {"February", "Feb", "F", "Fe"},
          {"March", "Mar"},
          {"April", "Apr", "Ap", "Abr"},  // Abr. is abbreviated Spanish
          {"May"},
          {"June", "Jun"},
          {"July", "Jul"},
          {"August", "Aug", "Au", "Ago"},
          // Ago seen a lot in data
          {"September", "Sep", "Sept", "Set", "S"}, // Set is a common misspelling
          {"October", "Oct", "O", "Oc"},
          {"November", "Novermber", "Nov", "N", "No"},
          {"December", "Dec", "D", "De"}
  };

  /**
   * Normalize date parts value.
   *
   * @param year
   * @param month
   * @param day
   * @return
   */
  public static NormalizedYearMonthDay normalize(String year, String month, String day){
    year = normalizeFloat(year);
    month = normalizeFloat(month);
    day = normalizeFloat(day);

    Integer monthAsInt = parseOrNull(month);
    if(monthAsInt == null){
      monthAsInt = monthNameToNumerical(month);
    }
    return new NormalizedYearMonthDay(parseOrNull(year), monthAsInt, parseOrNull(day));
  }

  /**
   * Often months come in the form Sept. September etc. This will convert many variations into the numerical version
   *
   * @param month name to normalize
   *
   * @return the numerical value of the month (January == 1 )
   */
  public static Integer monthNameToNumerical(String month) {
    if (StringUtils.isNotBlank(month)) {
      int m = 1;
      for (String[] monthValues : MONTHS) {
        for (String monthVal : monthValues) {
          if (StringUtils.equalsIgnoreCase(monthVal, month) || StringUtils.equalsIgnoreCase(monthVal + ".", month)) {
            return m;
          }
        }
        m++;
      }
    }
    return null;
  }


  /**
   * Often values are seen as Float rather than int, due to a database export The year "1978" is actually seen as
   * "1978.0".  Where this is detected, the string is normalized to the INT representation
   *
   * @param value To check
   *
   * @return the integer value
   */
  public static String normalizeFloat(String value) {
    if (value != null && value.contains(".0")) {
      try {
        Double d = new Double(value);
        if ((double) d.intValue() == d.doubleValue()) {
          return String.valueOf(d.intValue());
        }
      } catch (NumberFormatException e) {
      }
    }
    return value;
  }

  private static Integer parseOrNull(String integer){
    try{
      return Integer.valueOf(integer);
    }
    catch(NumberFormatException nfEx){}
    return null;
  }

  /**
   * The only reason why a such class exists is simply to possibly hold "invalid" data.
   * The normalizer job is simply to return Integer based on String.
   */
  public static class NormalizedYearMonthDay {

    private Integer year;
    private Integer month;
    private Integer day;

    public NormalizedYearMonthDay(Integer year, Integer month, Integer day){
      this.year = year;
      this.month = month;
      this.day = day;
    }

    public Integer getYear() {
      return year;
    }

    public Integer getMonth() {
      return month;
    }

    public Integer getDay() {
      return day;
    }
  }
}
