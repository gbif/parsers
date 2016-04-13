package org.gbif.common.parsers.date;

import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.Year;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.ResolverStyle;
import org.threeten.bp.temporal.ChronoField;

/**
 *
 */
public class ThreeTenNumericalDateParserBuilder {

  //the letter 'u' in all the patterns refers to YEAR as opposed to 'y' who refers to YEAR_OF_ERA
  private final static String YEAR_2_DIGITS_PATTERN_SUFFIX = "uu";
  private final static String IS_YEAR_2_DIGITS_PATTERN = "^.+[^u]"+YEAR_2_DIGITS_PATTERN_SUFFIX+"$";

  private final List<ThreeTenDateTimeParser> dateTimeParsers = Lists.newArrayList();

  //private  ThreeTenNumericalDateParserBuilder(){}

  public static ThreeTenDateMultiParserBuilder newMultiParserBuilder(){
    return new ThreeTenDateMultiParserBuilder();
  }

  /**
   * Build a single ThreeTenDateTimeParser.
   *
   * @param pattern
   * @param hint
   * @return
   */
  public static ThreeTenDateTimeParser build(String pattern, DateFormatHint hint){
    int minLength = getMinimumStringLengthForPattern(pattern);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    return new ThreeTenDateTimeParser(dateTimeFormatter, null, hint, minLength);
  }

  public static ThreeTenDateTimeParser build(String pattern, DateFormatHint hint, String separator,
                                             String alternativeSeparators){
    DateTimeSeparatorNormalizer dateTimeNormalizer = new DateTimeSeparatorNormalizer(CharMatcher.anyOf(alternativeSeparators), separator);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    int minLength = getMinimumStringLengthForPattern(pattern);
    return new ThreeTenDateTimeParser(dateTimeFormatter, dateTimeNormalizer, hint, minLength);
  }

  public static ThreeTenDateTimeParser build(String pattern, DateFormatHint hint, Year baseYear){
    int minLength = getMinimumStringLengthForPattern(pattern);
    DateTimeFormatter dateTimeFormatter = build2DigitsYearDateTimeFormatter(pattern, baseYear);
    return new ThreeTenDateTimeParser(dateTimeFormatter, null, hint, minLength);
  }

  public ThreeTenNumericalDateParserBuilder appendDateTimeParser(String pattern, DateFormatHint hint){
    dateTimeParsers.add(build(pattern, hint));
    return this;
  }

  public static ThreeTenDateTimeParser build(String pattern, DateFormatHint hint, String separator,
                                             String alternativeSeparators, Year baseYear){
    DateTimeSeparatorNormalizer dateTimeNormalizer = new DateTimeSeparatorNormalizer(CharMatcher.anyOf(alternativeSeparators), separator);
    DateTimeFormatter dateTimeFormatter = build2DigitsYearDateTimeFormatter(pattern, baseYear);
    int minLength = getMinimumStringLengthForPattern(pattern);
    return new ThreeTenDateTimeParser(dateTimeFormatter, dateTimeNormalizer, hint, minLength);
  }

  /**
   *
   * @param pattern
   * @param hint
   * @param separator
   * @param alternativeSeparators separator used in the pattern that should be used as replacement for alternativeSeparators
   * @return
   */
  public ThreeTenNumericalDateParserBuilder appendDateTimeParser(String pattern, DateFormatHint hint,
                                                                 String separator, String alternativeSeparators){
    dateTimeParsers.add(build(pattern, hint, separator, alternativeSeparators));
    return this;
  }

  public ThreeTenNumericalDateParserBuilder append2DigitsYearDateTimeParser(String pattern, DateFormatHint hint,
                                                                         Year baseYear){
    int minLength = getMinimumStringLengthForPattern(pattern);
    DateTimeFormatter dateTimeFormatter = build2DigitsYearDateTimeFormatter(pattern, baseYear);
    dateTimeParsers.add(new ThreeTenDateTimeParser(dateTimeFormatter, null, hint, minLength));
    return this;
  }

  public ThreeTenNumericalDateParserBuilder append2DigitsYearDateTimeParser(String pattern, DateFormatHint hint,
                                                                         String separator, String alternativeSeparators,
                                                                         Year baseYear){
    //get length before removing year part
    int minLength = getMinimumStringLengthForPattern(pattern);
    DateTimeSeparatorNormalizer dateTimeNormalizer = new DateTimeSeparatorNormalizer(CharMatcher.anyOf(alternativeSeparators), separator);
    DateTimeFormatter dateTimeFormatter = build2DigitsYearDateTimeFormatter(pattern, baseYear);
    dateTimeParsers.add(new ThreeTenDateTimeParser(dateTimeFormatter, dateTimeNormalizer, hint, minLength));
    return this;
  }

  public List<ThreeTenDateTimeParser> buildList(){
    return ImmutableList.copyOf(dateTimeParsers);
  }

  /**
   * From a {@link }DateTimeFormatter} pattern in String, get the minimum String length required for an input String to apply
   * the pattern. This is used to quickly discard DateTimeFormatter simply based on String length of the input.
   * Minimum length is the length of the pattern String minus the optional section(s) and quotes.
   *
   * @param pattern
   * @return
   */
  private static int getMinimumStringLengthForPattern(String pattern){
    pattern = ThreeTenNumericalDateParser.OPTIONAL_PATTERN_PART.matcher(pattern).replaceAll("").replaceAll("'", "");
    return pattern.length();
  }

  private static DateTimeFormatter build2DigitsYearDateTimeFormatter(String pattern, Year baseYear){
    Preconditions.checkState(pattern.matches(IS_YEAR_2_DIGITS_PATTERN) || pattern.equals(YEAR_2_DIGITS_PATTERN_SUFFIX),
            "build2DigitsYearDateTimeFormatter can only be used for patterns with 2 digit year");
    pattern = StringUtils.removeEnd(pattern, YEAR_2_DIGITS_PATTERN_SUFFIX);
    return new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern(pattern))
            .appendValueReduced(ChronoField.YEAR, 2, 2, baseYear.getValue()).parseStrict().toFormatter();
  }

  /**
   * More specific builder for ThreeTenDateTimeMultiParser
   */
  public static class ThreeTenDateMultiParserBuilder {
    private ThreeTenDateTimeParser preferred;
    private List<ThreeTenDateTimeParser> otherParsers = Lists.newArrayList();

    public ThreeTenDateMultiParserBuilder preferredDateTimeParser(String pattern, DateFormatHint hint){
      preferred = ThreeTenNumericalDateParserBuilder.build(pattern, hint);
      return this;
    }

    public ThreeTenDateMultiParserBuilder preferredDateTimeParser(String pattern, DateFormatHint hint, Year year){
      preferred = ThreeTenNumericalDateParserBuilder.build(pattern, hint, year);
      return this;
    }

    public ThreeTenDateMultiParserBuilder appendDateTimeParser(String pattern, DateFormatHint hint){
      otherParsers.add(ThreeTenNumericalDateParserBuilder.build(pattern, hint));
      return this;
    }

    public ThreeTenDateMultiParserBuilder appendDateTimeParser(String pattern, DateFormatHint hint, Year year){
      otherParsers.add(ThreeTenNumericalDateParserBuilder.build(pattern, hint, year));
      return this;
    }

    public ThreeTenDateMultiParserBuilder appendDateTimeParser(String pattern, DateFormatHint hint,
                                                               String separator, String alternativeSeparators){
      otherParsers.add(ThreeTenNumericalDateParserBuilder.build(pattern, hint, separator, alternativeSeparators));
      return this;
    }

    public ThreeTenDateMultiParserBuilder appendDateTimeParser(String pattern, DateFormatHint hint,
                                                               String separator, String alternativeSeparators, Year year){
      otherParsers.add(ThreeTenNumericalDateParserBuilder.build(pattern, hint, separator, alternativeSeparators, year));
      return this;
    }

    public ThreeTenDateTimeMultiParser build(){
      return new ThreeTenDateTimeMultiParser(preferred, otherParsers);
    }

  }

}
