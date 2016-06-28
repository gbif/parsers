package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation to take String and algorithmically convert to Date objects.
 */
@Deprecated
class StringToDateParser implements Parsable<Date> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringToDateParser.class);
    // maximum year parser considers a validly parsed date
    private static final Date MAX_VALID_YEAR;

    static {
        Calendar cal = Calendar.getInstance();
        //TODO: reduce this to 2100 ???
        cal.add(Calendar.YEAR, 3000);
        MAX_VALID_YEAR = cal.getTime();
    }

    // see also http://en.wikipedia.org/wiki/Date_format_by_country
    protected static String[] allPatterns =
        {"dd/MM/yy", "ddMMyy", "dd\\MM\\yy", "dd.MM.yy", "dd-MM-yy", "dd_MM_yy", "MM/dd/yy", "MMddyy", "MM\\dd\\yy",
            "MM.dd.yy", "MM-dd-yy", "MM_dd_yy", "dd/MM/yyyy", "ddMMyyyy", "dd\\MM\\yyyy", "dd.MM.yyyy", "dd-MM-yyyy",
            "dd_MM_yyyy", "MM/dd/yyyy", "MMddyyyy", "MM\\dd\\yyyy", "MM.dd.yyyy", "MM-dd-yyyy", "MM_dd_yyyy", "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HHmm",
            "yyyy-MM-dd'T'HHmmss", "yyyy-MM-dd'T'HH:mm:ssZZ", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM"};

    private static Pattern FULL_YEAR = Pattern.compile("\\d\\d\\d\\d");
    private static String[] ASIAN = {"yyyy年mm月dd日", "yyyy年m月d日", "yy年m月d日"};
    private static String[] TIME_FORMATS =
        {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HHmm",
            "yyyy-MM-dd'T'HHmmss", "yyyy-MM-dd'T'HH:mm:ssZZ", "yyyy-MM-dd'T'HH:mm:ss'Z'"};
    private static String[] FREQUENT_FULL = {"yyyy-MM-dd", "dd.MM.yyyy", "dd-MM-yyyy"};

    private static String[] CHAR6_ONLY_DIGITS = {"ddMMyy", "MMddyy", "yyMMdd"};
    private static String[] CHAR8_ONLY_DIGITS = {"yyyyMMdd", "ddMMyyyy"};

    private static final Pattern DOUBLE_ZERO_PATTERN = Pattern.compile("00");
    private static final Pattern DOUBLE_ZERO_ONE_PATTERN = Pattern.compile("0101");

    /**
     * Verifies if day and year cannot be confused with the month and returns appropriate confidence level.
     */
    private static ParseResult.CONFIDENCE checkMonthDay(Date d, boolean verifyYear) {
        if (d == null) {
            return ParseResult.CONFIDENCE.DEFINITE;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;

        if (day > 12 || day == month) {
            if (verifyYear) {
                int year = cal.get(Calendar.YEAR);
                int yearInCentury = year % 100;
                if (yearInCentury > 31) {
                    return ParseResult.CONFIDENCE.DEFINITE;
                } else if (year < 1700) {
                    return ParseResult.CONFIDENCE.POSSIBLE;
                }
                return ParseResult.CONFIDENCE.PROBABLE;
            }
            return ParseResult.CONFIDENCE.DEFINITE;
        }
        // day and month might have been confused
        return ParseResult.CONFIDENCE.POSSIBLE;
    }

    /**
     * Parses a numerical date or datetime string trying various formats.
     * Parsing is done based on the string length and delimiters found in the input primarily.
     * There is no cleaning done that removes question marks.
     * Only superflous whitespace is removed.
     */
    @Override
    public ParseResult<Date> parse(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return ParseResult.fail();
        }

        final int len = input.length();
        Date d = null;
        ParseResult.CONFIDENCE confidence = ParseResult.CONFIDENCE.DEFINITE;

        if (input.contains("年")) {
            d = strictParse(input, ASIAN);

        }
        if (StringUtils.isNumeric(input)) {
            if (len <= 4) {
                // year only
                d = strictParse(input, "yy", "yyyy");

            } else if (len == 6) {
                // no delimiter, year as 2 digits
                d = strictParse(input, CHAR6_ONLY_DIGITS);
                confidence = checkMonthDay(d, true);
                confidence = confidence == ParseResult.CONFIDENCE.DEFINITE ? ParseResult.CONFIDENCE.PROBABLE : confidence;
            } else if (len == 8) {
                // no delimiter, year as 2 digits
                d = strictParse(input, CHAR8_ONLY_DIGITS);
                confidence = checkMonthDay(d, true);
            }

        } else {
            // find what delimiter exists how often
            Character del = null;
            int delimCount = 0;
            for (char c : input.toCharArray()) {
                if (!Character.isDigit(c) && !Character.isAlphabetic(c)) {
                    delimCount++;
                    if (del == null) {
                        del = c;
                    } else if (!del.equals(c)) {
                        // different delimiters in use - could be datetime
                        d = strictParse(input, TIME_FORMATS);
                        confidence = checkMonthDay(d, true);
                        break;
                    }
                }
            }

            if (del == null) {
              return ParseResult.fail();
            }

            if (Character.isLetter(del)) {
                confidence = ParseResult.CONFIDENCE.POSSIBLE;
            }

            // check if full year is given within the string
            final boolean fullYear = FULL_YEAR.matcher(input).find();
            try {
                if (delimCount == 1) {
                    // year & month only
                    String[] formats;
                    if (fullYear) {
                        formats = new String[]{"yyyy" + del + "MM", "MM" + del + "yyyy"};

                    } else {
                        formats = new String[]{"yy" + del + "MM", "MM" + del + "yy"};
                        confidence = ParseResult.CONFIDENCE.POSSIBLE;
                    }
                    d = strictParse(input, formats);

                } else if (delimCount == 2) {
                    if (fullYear) {
                        // first try very common formats (main US, European, Chinese, etc)
                        d = strictParse(input, FREQUENT_FULL);
                        if (d == null) {
                            // maybe a datetime format ?
                            d = strictParse(input, TIME_FORMATS);
                            if (d == null) {
                                String[] formats = new String[]{"yyyy" + del + "MM" + del + "dd", "dd" + del + "MM" + del + "yyyy",
                                    "MM" + del + "dd" + del + "yyyy"};
                                d = strictParse(input, formats);
                                confidence = checkMonthDay(d, false);
                            }
                        }

                    } else {
                        String[] formats;
                        if (del == '-') {
                            // iso formats use dash and start with years
                            formats = new String[]{"yy" + del + "MM" + del + "dd", "dd" + del + "MM" + del + "yy",
                                "MM" + del + "dd" + del + "yy"};
                        } else {
                            formats = new String[]{"dd" + del + "MM" + del + "yy", "MM" + del + "dd" + del + "yy",
                                "yy" + del + "MM" + del + "dd"};
                        }
                        d = strictParse(input, formats);
                        confidence = checkMonthDay(d, true);
                    }

                } else {
                    // more than 3 delimiters, with time?
                    d = strictParse(input, TIME_FORMATS);
                }
            } catch (RuntimeException e) {
                return ParseResult.fail();
            }
        }

        if (d != null) {
            // basic year validation to avoid year = 12321
            if (d.before(MAX_VALID_YEAR)) {
                return ParseResult.success(confidence, d);
            }
        }
        return ParseResult.fail();
    }


    /**
     * Parses strictly swallowing errors
     *
     * @param input    to parse
     * @param patterns to use in parsing
     * @return null on error or the date
     */
    Date strictParse(String input, String... patterns) {
        try {
            return DateUtils.parseDateStrictly(input, patterns);
        } catch (ParseException e) {
            return null;
        }
    }

}
