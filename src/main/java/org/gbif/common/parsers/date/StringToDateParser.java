package org.gbif.common.parsers.date;

import org.gbif.common.parsers.Parsable;
import org.gbif.common.parsers.ParseResult;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation to take String and algorithmically convert to Date objects.
 */
class StringToDateParser implements Parsable<String, Date> {

  private static final Logger LOGGER = LoggerFactory.getLogger(StringToDateParser.class);

  protected static String[] allPatterns =
    {"dd/MM/yy", "ddMMyy", "dd\\MM\\yy", "dd.MM.yy", "dd-MM-yy", "dd_MM_yy", "MM/dd/yy", "MMddyy", "MM\\dd\\yy",
      "MM.dd.yy", "MM-dd-yy", "MM_dd_yy", "dd/MM/yyyy", "ddMMyyyy", "dd\\MM\\yyyy", "dd.MM.yyyy", "dd-MM-yyyy",
      "dd_MM_yyyy", "MM/dd/yyyy", "MMddyyyy", "MM\\dd\\yyyy", "MM.dd.yyyy", "MM-dd-yyyy", "MM_dd_yyyy", "yyyy-MM-dd",
      "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssZZ", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM"};

  private static final Pattern DOUBLE_ZERO_PATTERN = Pattern.compile("00");
  private static final Pattern DOUBLE_ZERO_ONE_PATTERN = Pattern.compile("0101");

  @Override
  public ParseResult<Date> parse(String input) {
    if (input.length() == 6) { // looks likely to be a 2 digit year
      // now try and get common 2 digit years
      Date ddmm = strictParse(input, new String[] {"ddMMyy"});
      Date mmdd = strictParse(input, new String[] {"MMddyy"});

      if (ddmm != null && mmdd == null) {
        return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, ddmm);
      } else if (ddmm == null && mmdd != null) {
        return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, mmdd);
      } else if (ddmm != null) {
        // dilema - could be ddMM or MMdd
        // since we see 50% split on "MM/dd/yyyy" "dd/MM/yyyy" but
        // it is much more common to see yyyyMMdd than yyyyddMM we
        // assume ddmm format
        // Hint: consider looking at several records and inferring
        // the format by looking at outliers on 1-31 and 1-12 range
        return ParseResult.success(ParseResult.CONFIDENCE.PROBABLE, ddmm);
      } else {
        // ignore and continue
      }
    } else {
      // starts with the most commonly seen dates, before dropping back into
      // a catch as many as possible algorithm
      Date ddmm = strictParse(input, new String[] {"dd/MM/yy", "dd/MM/yyyy", "yyyyMMdd", "yyyy-MM-dd", "ddMMyyyy"});
      Date mmdd = strictParse(input, new String[] {"MM/dd/yy", "MM/dd/yyyy", "yyyyddMM", "yyyy-dd-MM", "MMddyyyy"});

      if (ddmm != null && mmdd == null) {
        return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, ddmm);
      } else if (ddmm == null && mmdd != null) {
        return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, mmdd);
      } else if (ddmm != null) {
        // dilemma - could be ddMM or MMdd
        // since we see 50% split on "MM/dd/yyyy" "dd/MM/yyyy" but
        // it is much more common to see yyyyMMdd than yyyyddMM we
        // assume ddmm format
        // Hint: consider looking at several records and inferring
        // the format by looking at outliers on 1-31 and 1-12 range
        return ParseResult.success(ParseResult.CONFIDENCE.PROBABLE, ddmm);
      } else {
        // ignore and continue
      }

    }
    // now try and catch something that looks reasonable
    try {
      Date d;
      try {
        d = fallbackParse(input, allPatterns);
      } catch (ParseException e) {
        // sometime we see 00 instead of 01 for days and months
        // but if it has not separators, we might have just made
        // 00000000 become 01010101
        // 00/00/0000 should become 01/01/0000 though - see below
        input = DOUBLE_ZERO_PATTERN.matcher(input).replaceAll("01");
        input = DOUBLE_ZERO_ONE_PATTERN.matcher(input).replaceAll("00"); // so catch stupidity
        d = fallbackParse(input, allPatterns);
      }
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(d);
      LOGGER.debug("Day[{}] Month[{}] Year[{}]",
        new Object[] {calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.DAY_OF_MONTH),
          calendar.get(Calendar.YEAR)});
      if (d != null && !(calendar.get(Calendar.DAY_OF_MONTH) == 1 // remember above 0 becomes 1
                         && calendar.get(Calendar.MONTH) == 1 && calendar.get(Calendar.YEAR) == 0)) {
        // possible as this is not an exact sciensce by any means
        return ParseResult.success(ParseResult.CONFIDENCE.POSSIBLE, d);
      } else {
        return ParseResult.fail();
      }
    } catch (ParseException e) {
      return ParseResult.fail();
    }
  }

  /**
   * Tries strict and then lenient parsing of the date
   *
   * @param input    To parse
   * @param patterns to use in parsing
   *
   * @return The date if possible
   *
   * @throws ParseException On error
   */
  Date fallbackParse(String input, String[] patterns) throws ParseException {
    Date d;
    try {
      d = DateUtils.parseDateStrictly(input, patterns);
    } catch (RuntimeException e) {
      // try leniently
      d = DateUtils.parseDate(input, patterns);
    }
    return d;
  }

  /**
   * Parses strictly swallowing errors
   *
   * @param input    to parse
   * @param patterns to use in parsing
   *
   * @return null on error or the date
   */
  Date strictParse(String input, String[] patterns) {
    try {
      return DateUtils.parseDateStrictly(input, patterns);
    } catch (ParseException e) {
      return null;
    }
  }

}
