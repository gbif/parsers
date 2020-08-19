package org.gbif.common.parsers.date;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;

/**
 * Adds some flexibility around {@link DateTimeFormatter} with the {@link DateTimeSeparatorNormalizer} and
 * simple optimization the support of DateComponentOrdering.
 * <p>
 * This class is thread-safe once an instance is created.
 */
public class DateTimeParser {

  private final DateTimeFormatter formatter;
  private final DateTimeSeparatorNormalizer normalizer;
  private final DateComponentOrdering ordering;

  private final TemporalQuery<?>[] types;
  private final int minLength;

  /**
   * Package protected constructor.
   * Use {@link DateTimeParserBuilder}
   */
  DateTimeParser(@NotNull DateTimeFormatter formatter, @Nullable DateTimeSeparatorNormalizer normalizer,
                 @NotNull DateComponentOrdering ordering, TemporalQuery<?>[] type, int minLength) {

    Preconditions.checkNotNull(formatter, "DateTimeFormatter can not be null");
    Preconditions.checkNotNull(ordering, "DateComponentOrdering can not be null");
    Preconditions.checkNotNull(type, "TemporalQuery can not be null");
    Preconditions.checkArgument(minLength > 0, "minLength must be greater than 0");

    this.formatter = formatter;
    this.ordering = ordering;
    this.normalizer = normalizer;
    this.minLength = minLength;
    this.types = type;
  }

  public DateComponentOrdering getOrdering() {
    return ordering;
  }

  /**
   * Parses the provided String as a TemporalAccessor if possible, otherwise returns null.
   * <p>
   * This function fully support partial dates and will return the best possible date resolution based
   * on the {@link DateComponentOrdering} provided.
   * <p>
   * This function will not throw DateTimeParseException but returns null in case the input
   * can not be parsed.
   *
   * @return TemporalAccessor or null in case the input can not be parsed.
   */
  public TemporalAccessor parse(String input) {

    // return fast if minimum length is not meet
    if (input.length() < minLength) {
      return null;
    }

    if (normalizer != null) {
      input = normalizer.normalize(input);
    }

    try {
      if (types.length > 1) {
        return formatter.parseBest(input, types);
      }
      return (TemporalAccessor) formatter.parse(input, types[0]);
    } catch (DateTimeParseException dpe) {
    }
    return null;
  }

}
