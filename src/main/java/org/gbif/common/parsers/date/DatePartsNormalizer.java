package org.gbif.common.parsers.date;

import org.apache.commons.lang3.StringUtils;

/**
 * DatePartsNormalizer contract is to take String representing Year, Month and Day and return the corresponding
 * Integer as {@link NormalizedYearMonthDay} if possible.
 *
 * No validation will be applied to the normalized values.
 *
 */
public class DatePartsNormalizer {

  // Dictionary for interpreting string months
  // Important that the FIRST is the canonical form, followed
  // by abbreviations.  Be very careful not to add any ambiguous terms
  // such as "J" "A" etc.  Do not add a "." at the end of abbreviations
  // as normalization will automatically do this.
  protected static final String[][] MONTHS = {
          {"January", "Jan", "Ene", "Ja"},  // Ene. is abbreviated Spanish
          {"February", "Feb", "F", "Fe"},
          {"March", "Mar"},
          {"April", "Apr", "Ap", "Abr"},  // Abr. is abbreviated Spanish
          {"May"},
          {"June", "Jun", "Juni"},
          {"July", "Jul", "Juli"},
          {"August", "Aug", "Au", "Ago"},
          // Ago seen a lot in data
          {"September", "Sep", "Sept", "Set", "S"}, // Set is a common misspelling
          {"October", "Oct", "O", "Oc", "Okt"},
          {"November", "Novermber", "Nov", "N", "No"},
          {"December", "Dec", "D", "De"}
  };

  private static final String STRING_NULL = "\\N";

  /**
   * Normalize date parts value.
   *
   * @param year
   * @param month
   * @param day
   * @return result of normalization as NormalizedYearMonthDay
   */
  public static NormalizedYearMonthDay normalize(String year, String month, String day){
    year = normalizeFloat(year);
    month = normalizeFloat(month);
    day = normalizeFloat(day);

    Integer monthAsInt = parseOrNull(month);
    if(monthAsInt == null){
      monthAsInt = monthNameToNumerical(month);
    }

    Integer iYear = parseOrNull(year);
    Integer iMonth = monthAsInt;
    Integer iDay = parseOrNull(day);

    boolean yearDiscarded = wasDiscarded(year, iYear);
    boolean monthDiscarded = wasDiscarded(month, iMonth);
    boolean dayDiscarded = wasDiscarded(day, iDay);

    return new NormalizedYearMonthDay(iYear, iMonth, iDay, yearDiscarded, monthDiscarded, dayDiscarded);
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
   * @return the integer value (as String)
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

  /**
   * Try to parse the provided String as Integer. Returns null if not possible.
   * This function will trim the provided String.
   * @param integer
   * @return
   */
  private static Integer parseOrNull(String integer){
    if(integer != null){
      integer = integer.trim();
    }

    try{
      return Integer.valueOf(integer);
    }
    catch(NumberFormatException nfEx){}
    return null;
  }

  /**
   * Assert if a String value was discarded in the normalization process.
   *
   * @param strValue
   * @param intValue
   * @return the value should be considered discarded or not
   */
  private static boolean wasDiscarded(String strValue, Integer intValue){
    if(StringUtils.isBlank(strValue) || STRING_NULL.equals(strValue)){
      return false;
    }
    return intValue == null;
  }

  /**
   * Hold result of the normalization process.
   */
  public static class NormalizedYearMonthDay {

    private Integer year;
    private Integer month;
    private Integer day;

    private boolean yDiscarded;
    private boolean mDiscarded;
    private boolean dDiscarded;

    NormalizedYearMonthDay(Integer year, Integer month, Integer day, boolean yDiscarded, boolean mDiscarded,
                           boolean dDiscarded){
      this.year = year;
      this.month = month;
      this.day = day;

      this.yDiscarded = yDiscarded;
      this.mDiscarded = mDiscarded;
      this.dDiscarded = dDiscarded;
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

    public boolean yDiscarded() {
      return yDiscarded;
    }

    public boolean mDiscarded() {
      return mDiscarded;
    }

    public boolean dDiscarded() {
      return dDiscarded;
    }

    /**
     * The NormalizedYearMonthDay contains at least one discarded part.
     * @return
     */
    public boolean containsDiscardedPart(){
      return yDiscarded || mDiscarded || dDiscarded;
    }
  }
}
