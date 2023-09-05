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
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

/**
 * Main class to parse a date represented as a single String or as date parts into a {@link TemporalAccessor}.
 * If the String contains letters, the {@link TextualMonthDateTokenizer} and the {@link DatePartsNormalizer} will be used.
 * Otherwise, a {@link TemporalParser} configured for numerical dates will be used.
 *
 * This class is basically a decorator on top of default NumericalDateParser to handle months written in text.
 */
class TextDateParser implements TemporalParser, Serializable {
  /*
   * ISO format intervals, which are datetime/datetime or datetime/period.
   * Match 2013/2014, 2013/P1Y, 2013-02/03.
   * Fail 2013-12, 2013/02, 2018/10-23.
   */
  private static final Pattern ISO_YEAR_RANGE_PATTERN = Pattern.compile("^([12][0-9]{3})/([P12]\\d[^/]+)$");
  private static final Pattern ISO_DATE_RANGE_PATTERN = Pattern.compile("^([12][0-9]{3}[^/]+)/([^/]{2,})$");

  // This regex is not complete and will NOT handle date when the time zone is provided as text GMT
  private static final Pattern NUMERICAL_DATE_PATTERN =  Pattern.compile("[^a-zA-VX-Z]+[\\dT\\d]?[^a-zA-Z]+[Z]?$");
  private static final TextualMonthDateTokenizer TEXT_MONTH_TOKENIZER = TextualMonthDateTokenizer.newInstance();

  private static final TemporalParser NUMERICAL_DATE_PARSER = ThreeTenNumericalDateParser.newInstance();
  private static final DatePartsNormalizer DATE_PARTS_NORMALIZER = DatePartsNormalizer.newInstance();

  /**
   * Parse a date using the default (ISO) formats, or other unambiguous parsing.
   */
  @Override
  public ParseResult<TemporalAccessor> parse(String input) {
    if (StringUtils.isBlank(input)) {
      return ParseResult.fail();
    }

    Matcher matcher = ISO_YEAR_RANGE_PATTERN.matcher(input);
    if (!matcher.matches()) {
      matcher = ISO_DATE_RANGE_PATTERN.matcher(input);
    }

    if (matcher.matches()) {
      String from = matcher.group(1);
      // String to = matcher.group(2);
      return NUMERICAL_DATE_PARSER.parse(from, DateComponentOrdering.ISO_ETC);
    }

    // Check if the input text contains only punctuations and numbers
    // Also accept the T marker (e.g. 1978-12-21T02:12) from the ISO format
    // and the W week marker (e.g. 2018-W43).
    // We could also simply try to parse it but it is probably not optimal
    if (NUMERICAL_DATE_PATTERN.matcher(input).matches()) {
      return NUMERICAL_DATE_PARSER.parse(input, DateComponentOrdering.ISO_ETC);
    }

    TextualMonthDateTokenizer.DateTokens dt = TEXT_MONTH_TOKENIZER.tokenize(input);
    // for now we only handle cases where we can find year, month, day with confidence.
    if (!dt.containsDiscardedTokens() && dt.size() == 3) {
      DatePartsNormalizer.NormalizedYearMonthDay normalizedYearMonthDay = DATE_PARTS_NORMALIZER.normalize(
        dt.getToken(TextualMonthDateTokenizer.TokenType.INT_4).getToken(),
        dt.getToken(TextualMonthDateTokenizer.TokenType.TEXT).getToken(),
        dt.getToken(TextualMonthDateTokenizer.TokenType.INT_2).getToken());

      // no handling for partial dates with textual month for now
      if (normalizedYearMonthDay.getYear() != null &&
        normalizedYearMonthDay.getMonth() != null &&
        normalizedYearMonthDay.getDay() != null) {
        try {
          return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE,
            LocalDate.of(normalizedYearMonthDay.getYear(),
              normalizedYearMonthDay.getMonth(), normalizedYearMonthDay.getDay()));
        } catch (DateTimeException ignore) {
          // simply ignore bad dates
        }
      }
    }
    return ParseResult.fail();
  }

  /**
   * Parse a date restricted to the provided date component ordering.
   *
   * For now this is directly delegated the NumericalDateParser.
   */
  @Override
  public ParseResult<TemporalAccessor> parse(String input, @Nullable DateComponentOrdering ordering) {
    return NUMERICAL_DATE_PARSER.parse(input, ordering);
  }

  /**
   * Parse a date, and if given an ambiguous date, like 2/3/2000, use the orderings in turn to try
   * and parse the date.
   * <p>
   * NOTE, this behaviour <strong>differs</strong> from <code>parse(String input, DateComponentOrdering ordering)</code>
   */
  @Override
  public ParseResult<TemporalAccessor> parse(String input, DateComponentOrdering[] orderings) {
    ParseResult <TemporalAccessor> result = this.parse(input);
    if (result.isSuccessful()) {
      return result;
    } else {
      return NUMERICAL_DATE_PARSER.parse(input, orderings);
    }
  }

  /**
   * Parse date parts into a TemporalAccessor.
   * The {@link DatePartsNormalizer} will be applied on raw data.
   */
  @Override
  public ParseResult<TemporalAccessor> parse(String year, String month, String day) {
    DatePartsNormalizer.NormalizedYearMonthDay normalizedYearMonthDay = DATE_PARTS_NORMALIZER.normalize(
            year, month, day);

    // TODO when we can get the day but not the month in NormalizedYearMonthDay the parsing fails
    // we could actually keep the year by calling ThreeTenNumericalDateParser.parse(normalizedYearMonthDay.getYear(),
    // null, null); the CONFIDENCE would be set at PROBABLE.

    ParseResult<TemporalAccessor> parseResult = NUMERICAL_DATE_PARSER.parse(normalizedYearMonthDay.getYear(),
            normalizedYearMonthDay.getMonth(), normalizedYearMonthDay.getDay());

    //If we got a successful parsing BUT a part of the date was discarded we reduce confidence.
    if(parseResult.isSuccessful() && normalizedYearMonthDay.containsDiscardedPart()){
      return ParseResult.success(ParseResult.CONFIDENCE.PROBABLE, parseResult.getPayload());
    }

    return parseResult;
  }

  @Override
  public ParseResult<TemporalAccessor> parse(@Nullable Integer year, @Nullable Integer month, @Nullable Integer day) {
    return NUMERICAL_DATE_PARSER.parse(year, month, day);
  }

  /**
   * Parse date parts into a TemporalAccessor.
   * The {@link DatePartsNormalizer} will be applied on raw data.
   */
  @Override
  public ParseResult<TemporalAccessor> parse(String year, String dayOfYear) {
    DatePartsNormalizer.NormalizedYearDayOfYear normalizedYearDayOfYear = DATE_PARTS_NORMALIZER.normalize(
      year, dayOfYear);

    ParseResult<TemporalAccessor> parseResult = NUMERICAL_DATE_PARSER.parse(normalizedYearDayOfYear.getYear(),
      normalizedYearDayOfYear.getDayOfYear());

    // If we got a successful parsing BUT a part of the date was discarded we reduce confidence.
    if (parseResult.isSuccessful() && normalizedYearDayOfYear.containsDiscardedPart()) {
      return ParseResult.success(ParseResult.CONFIDENCE.PROBABLE, parseResult.getPayload());
    }

    return parseResult;
  }

  @Override
  public ParseResult<TemporalAccessor> parse(@Nullable Integer year, @Nullable Integer dayOfYear) {
    return NUMERICAL_DATE_PARSER.parse(year, dayOfYear);
  }
}
