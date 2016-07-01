package org.gbif.common.parsers.date.threeten;

import org.gbif.common.parsers.date.DateFormatHint;

import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.Year;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.ResolverStyle;
import org.threeten.bp.temporal.ChronoField;

/**
 * The NumericalDateParserBuilder can build objects directly (build(..) methods) or return an instance
 * of itself to create more complex object.
 *
 */
public class NumericalDateParserBuilder {

  //the letter 'u' in all the patterns refers to YEAR as opposed to 'y' who refers to YEAR_OF_ERA
  private final static String YEAR_2_DIGITS_PATTERN_SUFFIX = "uu";
  private final static String IS_YEAR_2_DIGITS_PATTERN = "^.+[^u]"+YEAR_2_DIGITS_PATTERN_SUFFIX+"$";

  private NumericalDateParserBuilder(){}

  /**
   * Get a new builder to create a list of DateTimeParser.
   *
   * @return
   */
  public static ThreeTenDateParserListBuilder newParserListBuilder(){
    return new ThreeTenDateParserListBuilder();
  }

  /**
   * Get a new builder to create a list of DateTimeMultiParser.
   * @return
   */
  public static ThreeTenDateMultiParserListBuilder newMultiParserListBuilder(){
    return new ThreeTenDateMultiParserListBuilder();
  }

  /**
   * Build a single DateTimeParser.
   *
   * @param pattern
   * @param hint
   * @return
   */
  public static DateTimeParser build(@NotNull String pattern, @NotNull DateFormatHint hint){
    Preconditions.checkNotNull(pattern);
    Preconditions.checkNotNull(hint);

    int minLength = getMinimumStringLengthForPattern(pattern);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    return new DateTimeParser(dateTimeFormatter, null, hint, minLength);
  }

  /**
   * Build a single DateTimeParser with support for separator normalization.
   *
   * @param pattern
   * @param hint
   * @param separator
   * @param alternativeSeparators
   * @return
   */
  public static DateTimeParser build(String pattern, DateFormatHint hint, String separator,
                                             String alternativeSeparators){
    Preconditions.checkNotNull(pattern);
    Preconditions.checkNotNull(hint);
    Preconditions.checkArgument(StringUtils.isNotBlank(separator), "separator must NOT be blank");
    Preconditions.checkArgument(StringUtils.isNotBlank(alternativeSeparators), "alternativeSeparators must NOT be blank");

    DateTimeSeparatorNormalizer dateTimeNormalizer = new DateTimeSeparatorNormalizer(CharMatcher.anyOf(alternativeSeparators), separator);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    int minLength = getMinimumStringLengthForPattern(pattern);
    return new DateTimeParser(dateTimeFormatter, dateTimeNormalizer, hint, minLength);
  }

  /**
   *
   * Build a single DateTimeParser from a baseYear.
   *
   *
   * @param pattern pattern that includes a 2 digits year (-uu)
   * @param hint
   * @param baseYear
   * @return
   */
  public static DateTimeParser build(String pattern, DateFormatHint hint, Year baseYear){
    int minLength = getMinimumStringLengthForPattern(pattern);
    DateTimeFormatter dateTimeFormatter = build2DigitsYearDateTimeFormatter(pattern, baseYear);
    return new DateTimeParser(dateTimeFormatter, null, hint, minLength);
  }

  /**
   *
   * Build a single DateTimeParser from a baseYear with support for separator normalization.
   *
   * @param pattern pattern that includes a 2 digits year (-uu)
   * @param hint
   * @param separator
   * @param alternativeSeparators
   * @param baseYear
   * @return
   */
  public static DateTimeParser build(String pattern, DateFormatHint hint, String separator,
                                             String alternativeSeparators, Year baseYear){
    Preconditions.checkNotNull(pattern);
    Preconditions.checkNotNull(hint);
    Preconditions.checkArgument(StringUtils.isNotBlank(separator), "separator must NOT be blank");
    Preconditions.checkArgument(StringUtils.isNotBlank(alternativeSeparators), "alternativeSeparators must NOT be blank");

    DateTimeSeparatorNormalizer dateTimeNormalizer = new DateTimeSeparatorNormalizer(CharMatcher.anyOf(alternativeSeparators), separator);
    DateTimeFormatter dateTimeFormatter = build2DigitsYearDateTimeFormatter(pattern, baseYear);
    int minLength = getMinimumStringLengthForPattern(pattern);
    return new DateTimeParser(dateTimeFormatter, dateTimeNormalizer, hint, minLength);
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
   * Builder used to build a List of {@link DateTimeParser}.
   */
  public static class ThreeTenDateParserListBuilder {
    private final List<DateTimeParser> dateTimeParsers = Lists.newArrayList();

    public ThreeTenDateParserListBuilder appendDateTimeParser(String pattern, DateFormatHint hint){
      dateTimeParsers.add(NumericalDateParserBuilder.build(pattern, hint));
      return this;
    }

    /**
     *
     * @param pattern
     * @param hint
     * @param separator
     * @param alternativeSeparators separator used in the pattern that should be used as replacement for alternativeSeparators
     * @return
     */
    public ThreeTenDateParserListBuilder appendDateTimeParser(String pattern, DateFormatHint hint,
                                                                   String separator, String alternativeSeparators){
      dateTimeParsers.add(NumericalDateParserBuilder.build(pattern, hint, separator, alternativeSeparators));
      return this;
    }

    public ThreeTenDateParserListBuilder append2DigitsYearDateTimeParser(String pattern, DateFormatHint hint,
                                                                              Year baseYear){
      int minLength = getMinimumStringLengthForPattern(pattern);
      DateTimeFormatter dateTimeFormatter = build2DigitsYearDateTimeFormatter(pattern, baseYear);
      dateTimeParsers.add(new DateTimeParser(dateTimeFormatter, null, hint, minLength));
      return this;
    }

    public ThreeTenDateParserListBuilder append2DigitsYearDateTimeParser(String pattern, DateFormatHint hint,
                                                                              String separator, String alternativeSeparators,
                                                                              Year baseYear){
      //get length before removing year part
      int minLength = getMinimumStringLengthForPattern(pattern);
      DateTimeSeparatorNormalizer dateTimeNormalizer = new DateTimeSeparatorNormalizer(CharMatcher.anyOf(alternativeSeparators), separator);
      DateTimeFormatter dateTimeFormatter = build2DigitsYearDateTimeFormatter(pattern, baseYear);
      dateTimeParsers.add(new DateTimeParser(dateTimeFormatter, dateTimeNormalizer, hint, minLength));
      return this;
    }

    public List<DateTimeParser> build(){
      return ImmutableList.copyOf(dateTimeParsers);
    }
  }

  /**
   * More specific builder Builder used to build a {@link DateTimeMultiParser}.
   */
  public static class ThreeTenDateMultiParserListBuilder {
    private DateTimeParser preferred;
    private List<DateTimeParser> otherParsers = Lists.newArrayList();

    public ThreeTenDateMultiParserListBuilder preferredDateTimeParser(String pattern, DateFormatHint hint){
      preferred = NumericalDateParserBuilder.build(pattern, hint);
      return this;
    }

    public ThreeTenDateMultiParserListBuilder preferredDateTimeParser(String pattern, DateFormatHint hint, Year year){
      preferred = NumericalDateParserBuilder.build(pattern, hint, year);
      return this;
    }

    public ThreeTenDateMultiParserListBuilder appendDateTimeParser(String pattern, DateFormatHint hint){
      otherParsers.add(NumericalDateParserBuilder.build(pattern, hint));
      return this;
    }

    public ThreeTenDateMultiParserListBuilder appendDateTimeParser(String pattern, DateFormatHint hint, Year year){
      otherParsers.add(NumericalDateParserBuilder.build(pattern, hint, year));
      return this;
    }

    public ThreeTenDateMultiParserListBuilder appendDateTimeParser(String pattern, DateFormatHint hint,
                                                               String separator, String alternativeSeparators){
      otherParsers.add(NumericalDateParserBuilder.build(pattern, hint, separator, alternativeSeparators));
      return this;
    }

    public ThreeTenDateMultiParserListBuilder appendDateTimeParser(String pattern, DateFormatHint hint,
                                                               String separator, String alternativeSeparators, Year year){
      otherParsers.add(NumericalDateParserBuilder.build(pattern, hint, separator, alternativeSeparators, year));
      return this;
    }

    /**
     * Ensure the builder is used with content we expect.
     * Currently (this could change) we should only have one DateTimeParser per DateFormatHint.
     *
     * @throws IllegalStateException
     */
    private void validate() throws IllegalStateException {
      Set<DateFormatHint> hints = Sets.newHashSet();
      if(preferred != null){
        hints.add(preferred.getHint());
      }

      for(DateTimeParser parser : otherParsers){
        if(!hints.add(parser.getHint())){
          throw new IllegalStateException("DateFormatHint can only be used once in a DateTimeMultiParser." +
                  "[" + parser.getHint() + "]");
        }
      }
    }

    public DateTimeMultiParser build() throws IllegalStateException {
      validate();
      return new DateTimeMultiParser(preferred, otherParsers);
    }
  }

}
