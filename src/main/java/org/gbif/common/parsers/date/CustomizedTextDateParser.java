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

import org.gbif.common.parsers.core.ParseResult;

import java.io.Serializable;
import java.time.temporal.TemporalAccessor;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Wrap a set of DateTimeFormatters to <code>parse</code> method.
 * <p>
 * If the ISO format parsing process fails, use <code>orderings</code> formatters to parse the date.
 */
public class CustomizedTextDateParser implements TemporalParser, Serializable {
  private DateComponentOrdering[] orderings;
  private TextDateParser parser = new TextDateParser();

  /**
   * @param orderings a set of DateTimeFormatters
   */
  public static TemporalParser getInstance(DateComponentOrdering[] orderings) {
    CustomizedTextDateParser ptdp = new CustomizedTextDateParser();
    ptdp.orderings = orderings;
    return ptdp;
  }

  @Override
  public ParseResult<TemporalAccessor> parse(String input) {
    if (ArrayUtils.isNotEmpty(orderings)) {
      return parser.parse(input, orderings);
    } else {
      return parser.parse(input);
    }
  }

  @Override
  public ParseResult<TemporalAccessor> parse(String input,
                                             @Nullable DateComponentOrdering ordering) {
    return parser.parse(input, ordering);
  }

  @Override
  public ParseResult<TemporalAccessor> parse(String input,
                                             @Nullable DateComponentOrdering[] orderings) {
    return parser.parse(input, orderings);
  }

  @Override
  public ParseResult<TemporalAccessor> parse(
    @Nullable String year,
    @Nullable String month,
    @Nullable String day) {
    return parser.parse(year, month, day);
  }

  @Override
  public ParseResult<TemporalAccessor> parse(
    @Nullable Integer year,
    @Nullable Integer month,
    @Nullable Integer day) {
    return parser.parse(year, month, day);
  }
}
