/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.common.parsers.date;

import org.gbif.utils.PreconditionUtils;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

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

    Objects.requireNonNull(formatter, "DateTimeFormatter can not be null");
    Objects.requireNonNull(ordering, "DateComponentOrdering can not be null");
    Objects.requireNonNull(type, "TemporalQuery can not be null");
    PreconditionUtils.checkArgument(minLength > 0, "minLength must be greater than 0");

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
