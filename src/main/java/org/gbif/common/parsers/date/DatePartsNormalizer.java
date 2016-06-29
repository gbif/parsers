package org.gbif.common.parsers.date;

import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Month;

/**
 * DatePartsNormalizer contract is to take String representing Year, Month and Day and return the corresponding
 * Integer as {@link NormalizedYearMonthDay} if possible.
 *
 * No validation will be applied to the normalized values.
 *
 * Thread-Safe after creation.
 *
 */
public class DatePartsNormalizer {

  private static final Logger LOG = LoggerFactory.getLogger(DatePartsNormalizer.class);

  private static final String STRING_NULL = "\\N";
  private static final String COLUMN_SEPARATOR = ";";
  private static final String ROW_ELEMENT_SEPARATOR = ",";
  private static final String COMMENT_MARKER = "#";
  private static final String MONTH_FILEPATH = "dictionaries/parse/month.csv";

  private static final String[][] MONTHS = new String[Month.values().length][];

  // Load all the month names and alternative month names from a file
  static {
    Map<String, Set<String>> monthMap = Maps.newHashMapWithExpectedSize(Month.values().length);
    String keyName;
    for(Month m : Month.values()){
      keyName = m.name().toLowerCase();
      monthMap.put(keyName, new HashSet<String>());
      //add the key itself
      monthMap.get(keyName).add(keyName);
    }

    File testInputFile = FileUtils.getClasspathFile(MONTH_FILEPATH);
    if(testInputFile == null){
      LOG.error("Month file can not be loaded. File not found: {}", MONTH_FILEPATH);
    }
    else{
      try{
        CSVReader csv = CSVReaderFactory.build(testInputFile, COLUMN_SEPARATOR, false);

        String monthKey;
        for (String[] row : csv) {
          if (row == null || row[0].startsWith(COMMENT_MARKER)) {
            continue;
          }
          monthKey = row[0].toLowerCase();
          if(monthMap.containsKey(monthKey)){
            for(String monthAltName : row[1].split(ROW_ELEMENT_SEPARATOR)){
              if(!Strings.isNullOrEmpty(monthAltName)) {
                monthMap.get(monthKey).add(monthAltName.trim().toLowerCase());
              }
            }
          }
          else{
            LOG.error("Unknown month found in: {}", MONTH_FILEPATH);
          }
        }
      } catch (IOException e) {
        LOG.error("Error loading month alternative names", e);
      }

      // keep it in an array
      int index = 0;
      for(Month m : Month.values()){
        MONTHS[index] = monthMap.get(m.name().toLowerCase()).toArray(new String[0]);
        index++;
      }
    }
  }

  /**
   * Private constructor use static method {@link #newInstance()}
   */
  private DatePartsNormalizer(){}

  public static DatePartsNormalizer newInstance(){
    return new DatePartsNormalizer();
  }

  /**
   * Normalize date parts value.
   *
   * @param year
   * @param month
   * @param day
   * @return result of normalization as NormalizedYearMonthDay
   */
  public NormalizedYearMonthDay normalize(String year, String month, String day){
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
          if (monthVal.equals(month.toLowerCase()) || (monthVal+ ".").equals(month.toLowerCase())) {
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
  private Integer parseOrNull(String integer){
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
  private boolean wasDiscarded(String strValue, Integer intValue){
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

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
              .append("year", year)
              .append("month", month)
              .append("day", day)
              .append("yDiscarded", yDiscarded)
              .append("mDiscarded", mDiscarded)
              .append("dDiscarded", dDiscarded)
              .toString();
    }
  }
}
