package org.gbif.common.parsers.date;


import org.gbif.common.parsers.core.ParseResult;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for parsing dates. Methods are implemented in static to reduce object creation and shorten coding for
 * clients. It is not anticipated that these methods would ever be mocked in unit tests.
 * Note: The returned date will always be in the systems default TimeZone. BE CAREFUL!
 */
@Deprecated
public class DateParseUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DateParseUtils.class);

  private static final Joiner DATE_JOINER = Joiner.on("-").skipNulls();

  // parsers are held as statics (effectively singletons) as they are all threadsafe
  private static  final StringToDateParser STRING_TO_DATE_PARSER = new StringToDateParser();

  // enumeration of the fields supported in the date handling
  public enum DATE_FIELD {
    DAY, MONTH, YEAR
  }

  // Dictionary for interpreting string months
  // Important that the FIRST is the canonical form, followed
  // by abbreviations.  Be very careful not to add any ambiguous terms
  // such as "J" "A" etc.  Do not add a "." at the end of abbreviations
  // as normalization will automatically do this.
  protected static final String[][] MONTHS = {{"January", "Jan", "J", "Ene", "Ja"},  // Ene. is abbreviated Spanish
    {"February", "Feb", "F", "Fe"}, {"March", "Mar"}, {"April", "Apr", "Ap", "Abr"},  // Abr. is abbreviated Spanish
    {"May"}, {"June", "Jun"}, {"July", "Jul"}, {"August", "Aug", "Au", "Ago"},
    // Ago seen a lot in data
    {"September", "Sep", "Sept", "Set", "S"}, // Set is a common mispelling
    {"October", "Oct", "O", "Oc"}, {"Novermber", "Nov", "N", "No"}, {"December", "Dec", "D", "De"},};

  protected static final int CURRENT_YEAR = new GregorianCalendar().get(Calendar.YEAR);

  /**
   * Takes a String representation and attempts to construct a date
   *
   * @param input To convert to a Date()
   *
   * @return The result of the parsing
   */
  public static ParseResult<Date> parse(String input) {
    input = normalizeMonth(input);
    return STRING_TO_DATE_PARSER.parse(input);
  }

  /**
   * Takes a String representation that is in the format specified and attempts to construct a date.
   *
   * @param input  To convert to a Date()
   * @param format The format of the date, e.g. dd/mm/yyyy
   *
   * @return The result of the parsing
   */
  public static ParseResult<Date> parse(String input, String format) {
    try {
      Date d = DateUtils.parseDate(input, format);
      LOGGER.debug("Parsed input[{}] in format[{}] to date[{}]", new Object[] {input, format, d});
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, d);
    } catch (ParseException e) {
      LOGGER.debug("Exception caught trying to parse a date", e);
      return ParseResult.fail();
    }
  }

  /**
   * Utility to check that the field appears valid in it's status use. Day must fall within 1-31, month within 1-12 and
   * year within 1-<currentyear>, null will always be invalid.
   *
   * @param value To test
   * @param field The context that the value is being used
   *
   * @return true or false depending on the validity of the value in it's context
   */
  public static boolean isValidUse(String value, DATE_FIELD field) {
    if (value == null) return false;

    try {
      int valueAsInt = Integer.parseInt(value);
      switch (field) {
        case DAY:
          return valueAsInt >= 1 && valueAsInt <= 31;
        case MONTH:
          return valueAsInt >= 1 && valueAsInt <= 12;
        case YEAR:
          return valueAsInt >= 1 && valueAsInt <= new GregorianCalendar().get(Calendar.YEAR);
      }
    } catch (NumberFormatException e) {
      LOGGER.debug("{} does not appear valid as a {}", value, field);
    }
    return false;
  }

  /**
   * Checks if a {@link YearMonthDay} object <i>maybe</i> represents a valid date in the sense that the date really
   * exists (e.g. no February 31st)
   * As the components of a YearMonthDay may be null it only checks this validity if all components are not null.
   *
   * @param ymd to check
   *
   * @return true if it may be a valid partial date, false if it is sure that it is an invalid date.
   */
  public static boolean isDateCoercible(YearMonthDay ymd) {
    if (ymd.getYear() == null || ymd.getMonth() == null || ymd.getDay() == null) {
      return true;
    }

    // SimpleDateFormat is not thread-safe. Do not reuse or if you reuse use Thread locals or other synchronization
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setLenient(false);
    return sdf.parse(DATE_JOINER.join(ymd.getYear(), ymd.getMonth(), ymd.getDay()), new ParsePosition(0)) != null;
  }

  /**
   * Tries to create a valid date (complete or partial) out of three Strings for year, month and day.
   * If all three components are null the parse will fail.
   *
   * @param year  to parse. May be null.
   * @param month to parse. May be null.
   * @param day   to parse. May be null.
   *
   * @return result of the parsing
   */
  public static ParseResult<Date> parse(@Nullable String year, @Nullable String month, @Nullable String day) {
    if (year == null && month == null && day == null) {
      return ParseResult.fail();
    }

    month = normalizeMonth(month);
    Date d = STRING_TO_DATE_PARSER.strictParse(day + "/" + month + "/" + year, new String[]{"dd/MM/yyyy", "dd/mm/yyyy"});
    if (d != null) {
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, d);
    }
    return ParseResult.fail();
  }

  /**
   * Note that YearMonthDay can be sparsely populated
   *
   * @param input To convert to a YearMonthDay()
   *
   * @return The result of the parsing
   */
  public static YearMonthDay atomize(Date input) {
    YearMonthDay ymd = new YearMonthDay();
    Calendar c = Calendar.getInstance();
    c.setTime(input);
    ymd.setDay(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
    ymd.setMonth(String.valueOf(c.get(Calendar.MONTH) + 1)); // months are zero indexed
    ymd.setYear(String.valueOf(c.get(Calendar.YEAR)));
    return ymd;
  }


  public static YearMonthDay normalize(String year, String month, String day) {
    YearMonthDay ymd = new YearMonthDay();
    // normalize float values
    year = normalizeFloat(year);
    month = normalizeFloat(month);
    day = normalizeFloat(day);

    // normalize string months
    month = normalizeMonth(month);

    // remove nulls
    day = "\\N".equalsIgnoreCase(day) ? null : day;
    month = "\\N".equalsIgnoreCase(month) ? null : month;
    year = "\\N".equalsIgnoreCase(year) ? null : year;
    year = inferCentury(year);
    if (isValidUse(year, DATE_FIELD.YEAR)) {
      ymd.setYear(year);
    }
    if (isValidUse(month, DATE_FIELD.MONTH)) {
      ymd.setMonth(month);
    }
    if (isValidUse(day, DATE_FIELD.DAY)) {
      ymd.setDay(day);
    }

    // if everything is null, it is possible that
    // the year contains a full date.  Attempt a parse of that
    if (ymd.representsNull() && StringUtils.isNotBlank(year)) {
      LOGGER.debug("Attempting to infer day, month and year from year field: {}", year);
      ParseResult<YearMonthDay> result = atomize(year);
      if (ParseResult.STATUS.SUCCESS == result.getStatus()) {
        LOGGER.debug("Year[{}] resulted in: {}", year, result.getPayload());
        ymd = result.getPayload();
      } else {
        try {
          // last ditch attempt to get a date
          // bad days are converted by the gregorian calendar (31/06/2000 for example becomes 01/07/2000)
          Date p = DateUtils.parseDate(year, new String[] {"yyyy-MM-dd", "yyyy/MM/dd"});
          GregorianCalendar gc = new GregorianCalendar();
          gc.setTime(p);
          ymd.setYear(String.valueOf(gc.get(Calendar.YEAR)));
          ymd.setMonth(String.valueOf(1+gc.get(Calendar.MONTH)));
          ymd.setDay(String.valueOf(gc.get(Calendar.DAY_OF_MONTH)));

        } catch (ParseException e) {
        }
      }
    }

    if (!isDateCoercible(ymd)) {
      ymd.setYear(null);
      ymd.setMonth(null);
      ymd.setDay(null);
    }

    // Final check before returning
    if (!isValidUse(ymd.getYear(), DATE_FIELD.YEAR)) {
      ymd.setYear(null);
    }
    if (!isValidUse(ymd.getMonth(), DATE_FIELD.MONTH)) {
      ymd.setMonth(null);
    }
    if (!isValidUse(ymd.getDay(), DATE_FIELD.DAY)) {
      ymd.setDay(null);
    }

    return ymd;
  }

  /**
   * Should the year be lower than 100, this will assume it is current century if makes sense, otherwise uses previous
   * century. At time of writing (2010) a value of 1-99 will be treated as 2010, whereas 11 will be considered 1911.
   * Next year, the same code would interpret 11 as 2011.  This is not foolproof, but much more likely than the year
   * 0011.
   */
  public static String inferCentury(String year) {
    try {
      int y = Integer.parseInt(year);
      if (y >= 0 && y <= 99) {
        // if it is in the future, it can't be
        if ((CURRENT_YEAR - (CURRENT_YEAR % 100)) + y > CURRENT_YEAR) {
          y = CURRENT_YEAR - (CURRENT_YEAR % 100) - 100 + y;
        } else {
          y = CURRENT_YEAR - (CURRENT_YEAR % 100) + y;
        }
        return String.valueOf(y);
      }
    } catch (NumberFormatException e) {
      // no problem, it does not look like a 2 digit year anyway
    }
    return year;
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

  /**
   * Parses the date and then atomizes it to the year month and day
   *
   * @param input To convert to a YearMonthDay()
   *
   * @return The result of the parsing
   */
  public static ParseResult<YearMonthDay> atomize(String input) {
    input = normalizeMonth(input);
    ParseResult<Date> parsed = parse(input);
    if (ParseResult.STATUS.SUCCESS == parsed.getStatus()) {
      // atomize
      YearMonthDay atomized = atomize(parsed.getPayload());
      // construct a combined result of the 2 stages
      return ParseResult.success(parsed.getConfidence(), atomized);

    } else if (ParseResult.STATUS.ERROR == parsed.getStatus()) {
      return ParseResult.error(parsed.getError());
    }

    return ParseResult.fail();
  }

  /**
   * Often months come in the form Sept. September etc. This will convert many variations into the numerical version
   *
   * @param input To normalize
   *
   * @return the normalized month
   */
  public static String normalizeMonth(String input) {
    if (input != null) {
      int m = 1;
      for (String[] monthValues : MONTHS) {
        for (String month : monthValues) {
          if (StringUtils.equalsIgnoreCase(month, input) || StringUtils.equalsIgnoreCase(month + ".", input)) {
            return padTo2Chars(m);
          }
        }
        m++;
      }
    }
    return input;
  }

  /**
   * @param value To convert into a 2 char String.  Must be 1-12 inclusive or else strange results might occur
   *
   * @return 1 becomes "01", 5 becomes "05", 10 becomes "10" etc
   */
  protected static String padTo2Chars(int value) {
    return value < 10 ? "0" + value : String.valueOf(value);
  }
}
