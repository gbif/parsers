package org.gbif.common.parsers.date;


import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.temporal.TemporalAccessor;
import org.threeten.bp.temporal.TemporalQuery;

/**
 * Adds some flexibility around {@link DateTimeFormatter} with the {@link DateTimeSeparatorNormalizer} and
 * simple optimization the support of DateFormatHint.
 *
 *
 * This class is thread-safe once an instance is created.
 *
 */
public class DateTimeParser {

  private final DateTimeFormatter formatter;
  private final DateTimeSeparatorNormalizer normalizer;
  private final DateFormatHint hint;

  private final TemporalQuery<?>[] types;
  private final int minLength;

  /**
   * Package protected constructor.
   * Use {@link DateTimeParserBuilder}
   *
   * @param formatter
   * @param normalizer optional, can be null
   * @param hint
   * @param type
   * @param minLength
   */
  DateTimeParser(@NotNull DateTimeFormatter formatter, @Nullable DateTimeSeparatorNormalizer normalizer,
                 @NotNull DateFormatHint hint, TemporalQuery<?>[] type, int minLength){

    Preconditions.checkNotNull(formatter, "DateTimeFormatter can not be null");
    Preconditions.checkNotNull(hint, "DateFormatHint can not be null");
    Preconditions.checkNotNull(type, "TemporalQuery can not be null");
    Preconditions.checkArgument(minLength > 0, "minLength must be greater than 0");

    this.formatter = formatter;
    this.hint = hint;
    this.normalizer = normalizer;
    this.minLength = minLength;
    this.types = type;
  }

  public DateFormatHint getHint() {
    return hint;
  }

  /**
   * Parses the provided String as a TemporalAccessor if possible, otherwise returns null.
   *
   * This function fully support partial dates and will return the best possible date resolution based
   * on the {@link DateFormatHint} provided.
   *
   * This function will not throw DateTimeParseException but returns null in case the input
   * can not be parsed.
   *
   * @param input
   * @return TemporalAccessor or null in case the input can not be parsed.
   */
  public TemporalAccessor parse(String input){

    // return fast if minimum length is not meet
    if(input.length() < minLength){
      return null;
    }

    if(normalizer != null){
      input = normalizer.normalize(input);
    }

    try {
      if(types.length > 1) {
        return formatter.parseBest(input, types);
      }
      return (TemporalAccessor)formatter.parse(input, types[0]);
    }
    catch (DateTimeParseException dpe){}
    return null;
  }

}
