package org.gbif.common.parsers.date;

import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQuery;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

/**
 * The DateTimeParserBuilder can build objects directly (build(..) methods) or return an instance
 * of itself to create more complex object.
 */
public class DateTimeParserBuilder {

  // The letter 'u' in all the patterns refers to YEAR as opposed to 'y' who refers to YEAR_OF_ERA
  private final static String YEAR_2_DIGITS_PATTERN_SUFFIX = "uu";
  private final static String IS_YEAR_2_DIGITS_PATTERN = "^.+[^u]"+YEAR_2_DIGITS_PATTERN_SUFFIX+"$";

  private DateTimeParserBuilder() {}

  /**
   * Get a new builder to create a list of DateTimeParser.
   */
  public static ThreeTenDateParserListBuilder newParserListBuilder() {
    return new ThreeTenDateParserListBuilder();
  }

  /**
   * Get a new builder to create a list of DateTimeMultiParser.
   */
  public static ThreeTenDateMultiParserListBuilder newMultiParserListBuilder() {
    return new ThreeTenDateMultiParserListBuilder();
  }

  /**
   * Build a single, strict,  DateTimeParser.
   */
  private static DateTimeParser build(@NotNull String pattern, @NotNull DateComponentOrdering ordering,
                                     @NotNull TemporalQuery<?> type) {
    Preconditions.checkNotNull(type);
    return build(pattern, ordering, new TemporalQuery[]{type});
  }

  /**
   * Build a single, strict, DateTimeParser with a specific ZoneId.
   */
  private static DateTimeParser build(@NotNull String pattern, @NotNull DateComponentOrdering ordering,
                                      @NotNull TemporalQuery<?> type, ZoneId zoneId) {
    Preconditions.checkNotNull(type);
    return build(pattern, ordering, new TemporalQuery[]{type}, zoneId);
  }

  /**
   * Build a single, possibly lenient, DateTimeParser.
   */
  private static DateTimeParser build(@NotNull String pattern, @NotNull DateComponentOrdering ordering, @NotNull TemporalQuery<?>[] type) {
    Preconditions.checkNotNull(pattern);
    Preconditions.checkNotNull(ordering);

    int minLength = getMinimumStringLengthForPattern(pattern);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    return new DateTimeParser(dateTimeFormatter, null, ordering, type, minLength);
  }

  private static DateTimeParser build(@NotNull String pattern, @NotNull DateComponentOrdering ordering,
                                      @NotNull TemporalQuery<?>[] type, ZoneId zoneId) {
    Preconditions.checkNotNull(pattern);
    Preconditions.checkNotNull(ordering);

    int minLength = getMinimumStringLengthForPattern(pattern);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
            .withResolverStyle(ResolverStyle.STRICT);
    return new DateTimeParser(dateTimeFormatter, null, ordering, type, minLength);
  }

  /**
   * Build a single, strict, DateTimeParser with support for separator normalization.
   */
  private static DateTimeParser build(String pattern, DateComponentOrdering ordering, @NotNull TemporalQuery<?> type,
                                      String separator, String alternativeSeparators) {
    return build(pattern, ordering, new TemporalQuery[]{type}, separator, alternativeSeparators);
  }

  /**
   * Build a single, possibly lenient, DateTimeParser with support for separator normalization.
   */
  private static DateTimeParser build(String pattern, DateComponentOrdering ordering, @NotNull TemporalQuery<?>[] type, String separator,
                                      String alternativeSeparators) {
    Preconditions.checkNotNull(pattern);
    Preconditions.checkNotNull(ordering);
    Preconditions.checkArgument(StringUtils.isNotBlank(separator), "separator must NOT be blank");
    Preconditions.checkArgument(StringUtils.isNotBlank(alternativeSeparators), "alternativeSeparators must NOT be blank");

    DateTimeSeparatorNormalizer dateTimeNormalizer = new DateTimeSeparatorNormalizer(CharMatcher.anyOf(alternativeSeparators), separator);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    int minLength = getMinimumStringLengthForPattern(pattern);
    return new DateTimeParser(dateTimeFormatter, dateTimeNormalizer, ordering, type, minLength);
  }

  /**
   * Build a single DateTimeParser from a baseYear.
   *
   * @param pattern pattern that includes a 2 digits year (-uu)
   */
  private static DateTimeParser build(String pattern, DateComponentOrdering ordering, @NotNull TemporalQuery<?>[] type, Year baseYear) {
    int minLength = getMinimumStringLengthForPattern(pattern);
    DateTimeFormatter dateTimeFormatter = build2DigitsYearDateTimeFormatter(pattern, baseYear);
    return new DateTimeParser(dateTimeFormatter, null, ordering, type, minLength);
  }

  /**
   * Build a single DateTimeParser from a baseYear with support for separator normalization.
   */
  private static DateTimeParser build(String pattern, DateComponentOrdering ordering, @NotNull TemporalQuery<?>[] type, String separator,
                                      String alternativeSeparators, Year baseYear) {
    Preconditions.checkNotNull(pattern);
    Preconditions.checkNotNull(ordering);
    Preconditions.checkArgument(StringUtils.isNotBlank(separator), "separator must NOT be blank");
    Preconditions.checkArgument(StringUtils.isNotBlank(alternativeSeparators), "alternativeSeparators must NOT be blank");

    DateTimeSeparatorNormalizer dateTimeNormalizer = new DateTimeSeparatorNormalizer(CharMatcher.anyOf(alternativeSeparators), separator);
    DateTimeFormatter dateTimeFormatter = build2DigitsYearDateTimeFormatter(pattern, baseYear);
    int minLength = getMinimumStringLengthForPattern(pattern);
    return new DateTimeParser(dateTimeFormatter, dateTimeNormalizer, ordering, type, minLength);
  }

  /**
   * From a {@link }DateTimeFormatter} pattern in String, get the minimum String length required for an input String to apply
   * the pattern. This is used to quickly discard DateTimeFormatter simply based on String length of the input.
   * Minimum length is the length of the pattern String minus the optional section(s) and quotes.
   */
  private static int getMinimumStringLengthForPattern(String pattern) {
    pattern = ThreeTenNumericalDateParser.OPTIONAL_PATTERN_PART.matcher(pattern).replaceAll("").replaceAll("'", "");
    return pattern.length();
  }

  private static DateTimeFormatter build2DigitsYearDateTimeFormatter(String pattern, Year baseYear) {
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

    /**
     * @param type expected {@link TemporalQuery} from the provided pattern
     */
    public ThreeTenDateParserListBuilder appendDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?> type) {
      dateTimeParsers.add(DateTimeParserBuilder.build(pattern, ordering, type));
      return this;
    }

    /**
     * @param type expected {@link TemporalQuery} from the provided pattern
     */
    public ThreeTenDateParserListBuilder appendDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?> type, ZoneId zoneId) {
      dateTimeParsers.add(DateTimeParserBuilder.build(pattern, ordering, type, zoneId));
      return this;
    }

    /**
     * @param type possible {@link TemporalQuery} from the provided pattern (ordered)
     */
    public ThreeTenDateParserListBuilder appendDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?>[] type) {
      dateTimeParsers.add(DateTimeParserBuilder.build(pattern, ordering, type));
      return this;
    }

    /**
     * @param alternativeSeparators separator used in the pattern that should be used as replacement for alternativeSeparators
     */
    public ThreeTenDateParserListBuilder appendDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?> type,
                                                              String separator, String alternativeSeparators
    ) {
      dateTimeParsers.add(DateTimeParserBuilder.build(pattern, ordering, type, separator, alternativeSeparators));
      return this;
    }

    /**
     * @param alternativeSeparators separator used in the pattern that should be used as replacement for alternativeSeparators
     */
    public ThreeTenDateParserListBuilder appendDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?>[] type,
                                                              String separator, String alternativeSeparators
    ) {
      dateTimeParsers.add(DateTimeParserBuilder.build(pattern, ordering, type, separator, alternativeSeparators));
      return this;
    }

    public ThreeTenDateParserListBuilder append2DigitsYearDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?> type,
                                                                         Year baseYear) {
      dateTimeParsers.add(DateTimeParserBuilder.build(pattern, ordering, new TemporalQuery[]{type}, baseYear));
      return this;
    }

    public ThreeTenDateParserListBuilder append2DigitsYearDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?> type,
                                                                         String separator, String alternativeSeparators,
                                                                         Year baseYear) {
      dateTimeParsers.add(DateTimeParserBuilder.build(pattern, ordering, new TemporalQuery[]{type}, separator, alternativeSeparators, baseYear));
      return this;
    }

    public List<DateTimeParser> build() {
      return ImmutableList.copyOf(dateTimeParsers);
    }
  }

  /**
   * More specific builder Builder used to build a {@link DateTimeMultiParser}.
   */
  public static class ThreeTenDateMultiParserListBuilder {
    private DateTimeParser preferred;
    private List<DateTimeParser> otherParsers = Lists.newArrayList();

    public ThreeTenDateMultiParserListBuilder preferredDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?> type) {
      preferred = DateTimeParserBuilder.build(pattern, ordering, type);
      return this;
    }

    public ThreeTenDateMultiParserListBuilder preferredDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?> type, Year year) {
      preferred = DateTimeParserBuilder.build(pattern, ordering, new TemporalQuery[]{type}, year);
      return this;
    }

    public ThreeTenDateMultiParserListBuilder appendDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?> type) {
      otherParsers.add(DateTimeParserBuilder.build(pattern, ordering, type));
      return this;
    }

    public ThreeTenDateMultiParserListBuilder appendDateTimeFormatter(DateTimeFormatter dateTimeFormatter, DateComponentOrdering ordering, TemporalQuery<?> type, int minLength) {
      otherParsers.add(new DateTimeParser(dateTimeFormatter, null, ordering, new TemporalQuery[]{type},  minLength));
      return this;
    }

    public ThreeTenDateMultiParserListBuilder appendDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?> type, Year year) {
      otherParsers.add(DateTimeParserBuilder.build(pattern, ordering, new TemporalQuery[]{type}, year));
      return this;
    }

    public ThreeTenDateMultiParserListBuilder appendDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?> type,
                                                                   String separator, String alternativeSeparators) {
      otherParsers.add(DateTimeParserBuilder.build(pattern, ordering, new TemporalQuery[]{type}, separator, alternativeSeparators));
      return this;
    }

    public ThreeTenDateMultiParserListBuilder appendDateTimeParser(String pattern, DateComponentOrdering ordering, TemporalQuery<?> type,
                                                                   String separator, String alternativeSeparators, Year year) {
      otherParsers.add(DateTimeParserBuilder.build(pattern, ordering, new TemporalQuery[]{type}, separator, alternativeSeparators, year));
      return this;
    }

    /**
     * Ensure the builder is used with content we expect.
     * Currently (this could change) we should only have one DateTimeParser per DateComponentOrdering.
     */
    private void validate() throws IllegalStateException {
      Set<DateComponentOrdering> orderings = Sets.newHashSet();
      if(preferred != null) {
        orderings.add(preferred.getOrdering());
      }

      for(DateTimeParser parser : otherParsers) {
        if(!orderings.add(parser.getOrdering())) {
          throw new IllegalStateException("DateComponentOrdering can only be used once in a DateTimeMultiParser." +
                  "[" + parser.getOrdering() + "]");
        }
      }
    }

    public DateTimeMultiParser build() throws IllegalStateException {
      validate();
      return new DateTimeMultiParser(preferred, otherParsers);
    }
  }
}
