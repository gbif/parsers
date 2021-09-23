/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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

import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Supports multiple {@link DateTimeParser} that are considered ambiguous. Two {@link DateTimeParser} are considered
 * ambiguous when they can potentially produce 2 different {@link TemporalAccessor}.
 * e.g. "dd/MM/yyyy" and "MM/dd/yyyy"
 *
 * <p>This class will try all the parsers and keep the all the successful results.
 *
 * <p>This class is thread-safe once an instance is created.
 */
public class DateTimeMultiParser {

  private final DateTimeParser preferred;
  private final List<DateTimeParser> otherParsers;
  private final List<DateTimeParser> allParsers;

  /**
   * Create a new instance of {@link DateTimeMultiParser}.
   * @param parsers requires more than 1 element in list
   */
  DateTimeMultiParser(@NotNull List<DateTimeParser> parsers){
    this(null, parsers);
  }

  /**
   *
   * Create a new instance of {@link DateTimeMultiParser}.
   * At least 2 {@link DateTimeParser} must be provided see details on parameters.
   *
   * @param preferred the preferred {@link DateTimeParser} or null
   * @param otherParsers list of {@link DateTimeParser} containing more than 1 element if no
   *                     preferred {@link DateTimeParser} is provided. Otherwise, the list must contain at least 1 element.
   */
  DateTimeMultiParser(@Nullable DateTimeParser preferred, @NotNull List<DateTimeParser> otherParsers) {
    Objects.requireNonNull(otherParsers, "otherParsers list can not be null");
    PreconditionUtils.checkArgument(otherParsers.size() > 0, "otherParsers must contain at least 1 element");

    if (preferred == null) {
      PreconditionUtils.checkArgument(otherParsers.size() > 1, "If no preferred DateTimeParser is provided, " +
              "the otherParsers list must contain more than 1 element");
    }

    this.preferred = preferred;
    this.otherParsers = new ArrayList<>(otherParsers);

    List<DateTimeParser> resultList = new ArrayList<>();

    if (preferred != null) {
      resultList.add(preferred);
    }
    resultList.addAll(otherParsers);

    this.allParsers = Collections.unmodifiableList(resultList);
  }

  /**
   * Get the list of all parsers: the preferred (if specified in the constructor) + otherParsers.
   *
   * @return never null
   */
  public List<DateTimeParser> getAllParsers(){
    return allParsers;
  }

  /**
   * Try to parse the input using all the parsers specified in the constructor.
   *
   * @param input
   * @return {@link MultipleParseResult} instance, never null.
   */
  public MultipleParseResult parse(String input) {

    int numberParsed = 0;
    TemporalAccessor lastParsed = null;
    TemporalAccessor preferredResult = null;
    List<String> usedFormats = new ArrayList<>();

    // lazily initialized assuming it should not be used most of the time
    List<TemporalAccessor> otherResults = null;
    for (DateTimeParser currParser : otherParsers) {
      lastParsed = currParser.parse(input);
      if (lastParsed != null) {
        numberParsed++;
        if (otherResults == null) {
          otherResults = new ArrayList<>();
        }
        otherResults.add(lastParsed);
        usedFormats.add(currParser.getOrdering().name());
      }
    }

    // try the preferred DateTimeParser
    if (this.preferred != null) {
      lastParsed = this.preferred.parse(input);
      if (lastParsed != null) {
        numberParsed++;
        preferredResult = lastParsed;
      }
    }

    return new MultipleParseResult(numberParsed, usedFormats, preferredResult, otherResults);
  }

  /**
   * Nested class representing the result of a multi-parse.
   */
  public static class MultipleParseResult {
    private int numberParsed;
    private TemporalAccessor preferredResult;
    private List<TemporalAccessor> otherResults;
    public List<String> formats;

    public MultipleParseResult(int numberParsed, List<String> formats, TemporalAccessor preferredResult, List<TemporalAccessor> otherResults) {
      this.numberParsed = numberParsed;
      this.formats = formats;
      this.preferredResult = preferredResult;
      this.otherResults = otherResults;
    }

    public int getNumberParsed() {
      return numberParsed;
    }

    public List<String> getFormats() {
      return formats;
    }

    public TemporalAccessor getPreferredResult() {
      return preferredResult;
    }

    public List<TemporalAccessor> getOtherResults() {
      return otherResults;
    }

    /**
     * Return the preferredResult if available otherwise the first element of otherResults.
     * If otherResults is empty, null is returned.
     */
    public TemporalAccessor getResult() {
      if (preferredResult != null) {
        return preferredResult;
      }

      if (otherResults != null && otherResults.size() > 0) {
        return otherResults.get(0);
      }
      return null;
    }
  }
}
