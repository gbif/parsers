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
import org.gbif.utils.PreconditionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.common.parsers.date.DateComponentOrdering.DMY;
import static org.gbif.common.parsers.date.DateComponentOrdering.DMYT;
import static org.gbif.common.parsers.date.DateComponentOrdering.HAN;
import static org.gbif.common.parsers.date.DateComponentOrdering.ISO_ETC;
import static org.gbif.common.parsers.date.DateComponentOrdering.MDY;
import static org.gbif.common.parsers.date.DateComponentOrdering.MDYT;
import static org.gbif.common.parsers.date.DateComponentOrdering.Y;
import static org.gbif.common.parsers.date.DateComponentOrdering.YD;
import static org.gbif.common.parsers.date.DateComponentOrdering.YM;
import static org.gbif.common.parsers.date.DateComponentOrdering.YMD;
import static org.gbif.common.parsers.date.DateComponentOrdering.YMDT;
import static org.gbif.common.parsers.date.DateComponentOrdering.YMDTZ;
import static org.gbif.common.parsers.date.DateComponentOrdering.YW;

/**
 * Numerical DateParser based on threetenbp (JSR310 backport) library which also means it is almost
 * ready for Java 8.
 *
 * <p>Months are in numerical value starting at 1 for January.
 *
 * <p>Note that LocalDateTime and LocalDate are TimeZone agnostic.
 *
 * <p>Be aware that LocalDate and LocalDateTime doesn't map correctly to Date object for all dates
 * before the Gregorian cut-off date (1582-10-15). To transform a such date use GregorianCalendar by
 * setting the date according to the TemporalAccessor you got back from that class.
 *
 * <p>Thread-Safe after creation.
 */
class ThreeTenNumericalDateParser implements TemporalParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThreeTenNumericalDateParser.class);

  static final Pattern OPTIONAL_PATTERN_PART = Pattern.compile("\\[.*\\]");

  // ISO 8601 specifies a Unicode minus (CHAR_MINUS), with a hyphen (CHAR_HYPHEN) as an alternative.
  static final char CHAR_HYPHEN = '\u002d'; // Unicode hyphen, U+002d, char '-'
  static final char CHAR_MINUS = '\u2212'; // Unicode minus, U+2212, char '−'
  static final String HYPHEN = String.valueOf(CHAR_HYPHEN);
  static final String MINUS = String.valueOf(CHAR_MINUS);

  private static final Map<DateComponentOrdering, List<DateTimeParser>> FORMATTERS_BY_ORDERING = new HashMap<>();

  // DateTimeFormatter includes some ISO parsers but just to make it explicit we define our own
  private static final DateTimeFormatter ISO_PARSER = (new DateTimeFormatterBuilder()
    .appendValue(ChronoField.YEAR, 2, 4, SignStyle.NEVER)
    .optionalStart().appendLiteral('-')
    .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
    .optionalStart().appendLiteral('-')
    .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NEVER))
    .optionalEnd()
    .optionalEnd()
    .toFormatter().withResolverStyle(ResolverStyle.STRICT);

  /*
   * Brackets [] represent optional sections of the pattern. (And subsequent patters don't make parts optional, if an earlier pattern already matched that.)
   * Unfortunately, it's not possible to specify an arbitrary decimal for seconds in one pattern.
   */
  // separator is a CHAR_HYPHEN
  private static final List<DateTimeParser> BASE_PARSER_LIST =
      Collections.unmodifiableList(
          DateTimeParserBuilder.newParserListBuilder()
              .appendDateTimeParser("uuuuMMdd", YMD, LocalDate::from)
              .appendDateTimeParser("uuuu-M-d", YMD, LocalDate::from, HYPHEN, MINUS + ".")

              // Either no fractional seconds, milliseconds or microseconds. T or space.
              .appendDateTimeParser("uuuu-M-d' 'HH[[:]mm[[:]ss[.S]]]", YMDT, LocalDateTime::from, HYPHEN, MINUS + ".")
              .appendDateTimeParser("uuuu-M-d' 'HH[:]mm[[:]ss.SS]", YMDT, LocalDateTime::from)
              .appendDateTimeParser("uuuu-M-d' 'HH[:]mm[[:]ss.SSS]", YMDT, LocalDateTime::from)
              .appendDateTimeParser("uuuu-M-d' 'HH[:]mm[[:]ss.SSSSSS]", YMDT, LocalDateTime::from)
              .appendDateTimeParser("uuuu-M-d' 'HH[:]mm[[:]ss.SSSSSSS]", YMDT, LocalDateTime::from)
              .appendDateTimeParser("uuuu-M-d' 'HH[:]mm[[:]ss[.SSS]]X", YMDTZ, ZonedDateTime::from, HYPHEN, MINUS)
              .appendDateTimeParser("uuuu-M-d'T'HH[[:]mm[[:]ss[.S]]]", YMDT, LocalDateTime::from)
              .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss.SS]", YMDT, LocalDateTime::from)
              .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss.SSS]", YMDT, LocalDateTime::from)
              .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss.SSSSSS]", YMDT, LocalDateTime::from)
              .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss.SSSSSSS]", YMDT, LocalDateTime::from)
              // T, but with a time zone, accepting Z, +00, +0000 and +00:00 for UTC and - or − for negative offsets.
              .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss[.SSS]]X", YMDTZ, ZonedDateTime::from, HYPHEN, MINUS)
              .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[:]ss.SSSSSSX", YMDTZ, ZonedDateTime::from, HYPHEN, MINUS)
              .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss[.SSS]]xxx", YMDTZ, ZonedDateTime::from, HYPHEN, MINUS)
              .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[:]ss.SSSSSSxxx", YMDTZ, ZonedDateTime::from, HYPHEN, MINUS)

              .appendDateTimeParser("uuuu-M", YM, YearMonth::from)
              .appendDateTimeParser("uuuu", Y, Year::from)
              .appendDateTimeParser("uuuu/MM/dd", YMD, LocalDate::from)
              .appendDateTimeParser("uuuu/M/d", YMD, LocalDate::from)
              .appendDateTimeParser("uuuu年MM月dd日", HAN, LocalDate::from)
              .appendDateTimeParser("uuuu年M月d日", HAN, LocalDate::from)
              .appendDateTimeParser("YYYY-'W'ww", YW, LocalDate::from) // ISO "week years", 2018-W43.
              .appendDateTimeParser("uuuu-DDD", YD, LocalDate::from) // Week days, 2018-296.
              .build()
      );

  // Possibly ambiguous dates will record an error in case more than one pattern can be applied
  private static final List<DateTimeMultiParser> MULTIPARSER_PARSER_LIST =
    Arrays.asList(
      DateTimeParserBuilder.newMultiParserListBuilder()
        // Dot-formats are used by some European languages (German, Danish, Swedish etc), but not by
        // the USA, so we can prefer the d.M.uuuu pattern.
        .preferredDateTimeParser("d.M.uuuu", DMY, LocalDate::from)
        .appendDateTimeParser("M.d.uuuu", MDY, LocalDate::from)
        .build(),
      // These are mostly derived of the difference between the common DMY format with slashes and US MDY format
      DateTimeParserBuilder.newMultiParserListBuilder()
        .appendDateTimeParser("d/M/uuuu'T'HH[[:]mm[[:]ss[.SSS]]]", DMYT, LocalDateTime::from, "/", HYPHEN + MINUS)
        .appendDateTimeParser("M/d/uuuu'T'HH[[:]mm[[:]ss[.SSS]]]", MDYT, LocalDateTime::from, "/", HYPHEN + MINUS)
        .build(),
      DateTimeParserBuilder.newMultiParserListBuilder()
        .appendDateTimeParser("d/M/uuuu'T'HH[[:]mm[[:]ss[.SSS]]][X]", DMYT, ZonedDateTime::from, "/", HYPHEN + MINUS)
        .appendDateTimeParser("M/d/uuuu'T'HH[[:]mm[[:]ss[.SSS]]][X]", MDYT, ZonedDateTime::from, "/", HYPHEN + MINUS)
        .build(),
      DateTimeParserBuilder.newMultiParserListBuilder()
        .appendDateTimeParser("d/M/uuuu' 'HH[[:]mm[[:]ss[.SSS]]]", DMYT, LocalDateTime::from, "/", HYPHEN + MINUS)
        .appendDateTimeParser("M/d/uuuu' 'HH[[:]mm[[:]ss[.SSS]]]", MDYT, LocalDateTime::from, "/", HYPHEN + MINUS)
        .build(),
      DateTimeParserBuilder.newMultiParserListBuilder()
        .appendDateTimeParser("d/M/uuuu' 'HH[[:]mm[[:]ss[.SSS]]][X]", DMYT, ZonedDateTime::from, "/", HYPHEN + MINUS)
        .appendDateTimeParser("M/d/uuuu' 'HH[[:]mm[[:]ss[.SSS]]][X]", MDYT, ZonedDateTime::from, "/", HYPHEN + MINUS)
        .build(),
      DateTimeParserBuilder.newMultiParserListBuilder()
        .appendDateTimeParser("d/M/uuuu", DMY, LocalDate::from, "/", HYPHEN + MINUS)
        .appendDateTimeParser("M/d/uuuu", MDY, LocalDate::from, "/", HYPHEN + MINUS)
        .build(),
      // Dates with no separator.
      DateTimeParserBuilder.newMultiParserListBuilder()
        .appendDateTimeParser("ddMMuuuu", DMY, LocalDate::from)
        .appendDateTimeParser("MMdduuuu", MDY, LocalDate::from)
        .build(),
      // Backslashes and underscores are not officially recommended by any country, but are sometimes used
      DateTimeParserBuilder.newMultiParserListBuilder()
        .appendDateTimeParser("d\\M\\uuuu", DMY, LocalDate::from, "\\", "_")
        .appendDateTimeParser("M\\d\\uuuu", MDY, LocalDate::from, "\\", "_")
        .build());

  static {
    for (DateTimeParser parser : BASE_PARSER_LIST) {
      FORMATTERS_BY_ORDERING.putIfAbsent(parser.getOrdering(), new ArrayList<>());
      FORMATTERS_BY_ORDERING.get(parser.getOrdering()).add(parser);
    }

    for (DateTimeMultiParser parserAmbiguity : MULTIPARSER_PARSER_LIST) {
      for (DateTimeParser parser : parserAmbiguity.getAllParsers()) {
        FORMATTERS_BY_ORDERING.putIfAbsent(parser.getOrdering(), new ArrayList<>());
        FORMATTERS_BY_ORDERING.get(parser.getOrdering()).add(parser);
      }
    }
  }

  // the active list/map are related to a specific instance
  private final Map<DateComponentOrdering, List<DateTimeParser>> activeFormattersByOrdering;
  private final List<DateTimeMultiParser> activeMultiParserList;

  /**
   * Get an instance of a default ThreeTenNumericalDateParser.
   */
  public static ThreeTenNumericalDateParser newInstance() {
    return new ThreeTenNumericalDateParser();
  }

  /**
   * Get an instance of a ThreeTenNumericalDateParser from a base year. Base year is used to handle
   * year represented by 2 digits.
   *
   * @param baseYear
   */
  public static ThreeTenNumericalDateParser newInstance(Year baseYear) {
    return new ThreeTenNumericalDateParser(baseYear);
  }

  /**
   * Private constructor, use static methods {@link #newInstance()} and {@link #newInstance(Year)}.
   */
  private ThreeTenNumericalDateParser() {
    this.activeFormattersByOrdering = Collections.unmodifiableMap(FORMATTERS_BY_ORDERING);
    this.activeMultiParserList = MULTIPARSER_PARSER_LIST;
  }

  private ThreeTenNumericalDateParser(Year baseYear) {
    PreconditionUtils.checkState(baseYear.getValue() <= LocalDate.now().getYear(),
      "Base year is less or equals to the current year");

    Map<DateComponentOrdering, List<DateTimeParser>> formattersByOrdering = new HashMap<>(FORMATTERS_BY_ORDERING);

    List<DateTimeMultiParser> multiParserList = new ArrayList<>(MULTIPARSER_PARSER_LIST);
    multiParserList.addAll(new ArrayList<>(
        Arrays.asList(
            DateTimeParserBuilder.newMultiParserListBuilder()
                .preferredDateTimeParser("d.M.uu", DMY, LocalDate::from, baseYear)
                .appendDateTimeParser("M.d.uu", MDY, LocalDate::from, baseYear)
                .build(),
            DateTimeParserBuilder.newMultiParserListBuilder()
                .appendDateTimeParser("d/M/uu", DMY, LocalDate::from, "/", HYPHEN + MINUS, baseYear)
                .appendDateTimeParser("M/d/uu", MDY, LocalDate::from, "/", HYPHEN + MINUS, baseYear)
                .build(),
            DateTimeParserBuilder.newMultiParserListBuilder()
                .appendDateTimeParser("ddMMuu", DMY, LocalDate::from, baseYear)
                .appendDateTimeParser("MMdduu", MDY, LocalDate::from, baseYear)
                .build(),
            DateTimeParserBuilder.newMultiParserListBuilder()
                .appendDateTimeParser("d\\M\\uu", DMY, LocalDate::from, "\\", "_", baseYear)
                .appendDateTimeParser("M\\d\\uu", MDY, LocalDate::from, "\\", "_", baseYear)
                .build()
        )));

    for (DateTimeMultiParser multiParser : multiParserList) {
      for (DateTimeParser parser : multiParser.getAllParsers()) {
        formattersByOrdering.putIfAbsent(parser.getOrdering(), new ArrayList<>());
        formattersByOrdering.get(parser.getOrdering()).add(parser);
      }
    }

    this.activeMultiParserList = Collections.unmodifiableList(multiParserList);
    this.activeFormattersByOrdering = Collections.unmodifiableMap(formattersByOrdering);
  }

  @Override
  public ParseResult<TemporalAccessor> parse(String input) {
    return parse(input, ISO_ETC);
  }

  @Override
  public ParseResult<TemporalAccessor> parse(String input, @Nullable DateComponentOrdering ordering) {

    if (StringUtils.isBlank(input)) {
      return ParseResult.fail();
    }
    // make sure ordering is never null
    if (ordering == null) {
      ordering = ISO_ETC;
    }

    // If ordering is given, BASE_PARSER_LIST is ignored.
    List<DateTimeParser> parserList =
        activeFormattersByOrdering.getOrDefault(ordering, BASE_PARSER_LIST);

    // First attempt: find a match with definite confidence
    TemporalAccessor parsedTemporalAccessor;
    for (DateTimeParser parser : parserList) {
      parsedTemporalAccessor = parser.parse(input);
      if (parsedTemporalAccessor != null) {
        return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, parsedTemporalAccessor);
      }
    }

    // if a format ordering was provided we already tried all possible format
    if (ordering != ISO_ETC) {
      return ParseResult.fail();
    }

    // Second attempt: find one or multiple matches in the list of DateTimeMultiParser
    int numberOfPossiblyAmbiguousMatch = 0;
    TemporalAccessor lastParsedSuccess = null;
    TemporalAccessor lastParsedPreferred = null;
    Set<TemporalAccessor> otherParsed = new HashSet<>();
    // Checking if result is the same due to duplicated format in multiple orderings
    // like "d/M/uu" is defined in DMY and DMYT, it will generate same dates.
    List<TemporalAccessor> verificationDuplication = new ArrayList<>();
    // Are the results all equal (representing the same TemporalAccessor), used if there is no
    // preferred result defined
    boolean lastParsedSuccessOtherResultsEqual = false;
    DateTimeMultiParser.MultipleParseResult result = null;

    // here we do not stop when we find a match, we try them all to check for a possible ambiguity
    for (DateTimeMultiParser parserAmbiguity : activeMultiParserList) {
      result = parserAmbiguity.parse(input);

      if (result.getNumberParsed() > 0) {
        lastParsedSuccess = result.getResult();
        if (!verificationDuplication.contains(lastParsedSuccess)) {
          numberOfPossiblyAmbiguousMatch += result.getNumberParsed();
          verificationDuplication.add(lastParsedSuccess);
        }

        // make sure to log in case lastParsedSuccessOtherResultsEqual already true
        if (lastParsedSuccessOtherResultsEqual) {
          LOGGER.warn("Issue with DateTimeMultiParser configuration: Input {} produces more results even " +
            "if lastParsedSuccessOtherResultsEqual is set to true.", input);
        }
        lastParsedSuccessOtherResultsEqual = false;

        if (result.getPreferredResult() != null) {
          if (lastParsedPreferred != null) {
            LOGGER.warn("Issue with DateTimeMultiParser configuration: Input {} produces 2 preferred results", input);
          }
          lastParsedPreferred = result.getPreferredResult();
        } else {
          //if we have no PreferredResult but all results represent the same TemporalAccessor
          if (result.getOtherResults() != null && result.getOtherResults().size() > 1) {
            lastParsedSuccessOtherResultsEqual = allEquals(result.getOtherResults());
            otherParsed.addAll(result.getOtherResults());
          }
        }
      }
    }

    //if there is only one pattern that can be applied it is not ambiguous
    if (numberOfPossiblyAmbiguousMatch == 1) {
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, lastParsedSuccess);
    } else if (numberOfPossiblyAmbiguousMatch > 1) {
      // if all the possible ambiguities are equal, there is no ambiguity
      if (lastParsedSuccessOtherResultsEqual) {
        return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, lastParsedSuccess);
      }

      // check if we have result from the preferred parser
      if (lastParsedPreferred != null) {
        return ParseResult.success(ParseResult.CONFIDENCE.PROBABLE, lastParsedPreferred);
      }
    }

    LOGGER.debug("Number of matches for {} : {}", input, numberOfPossiblyAmbiguousMatch);
    if (result == null) {
      return ParseResult.fail();
    } else {
      return new ParseResult<>(ParseResult.STATUS.FAIL, ParseResult.CONFIDENCE.POSSIBLE, null, new ArrayList<>(otherParsed), null);
    }
  }

  /**
   * Parse a date, and if given an ambiguous date, like 2/3/2000, try {@code orderings} to parse date,
   * and return the first successful result.
   * <p>
   * NOTE, this behaviour <strong>differs</strong> from <code>parse(String input, DateComponentOrdering ordering)</code>
   */
  @Override
  public ParseResult<TemporalAccessor> parse(String input, DateComponentOrdering[] orderings) {
    ParseResult<TemporalAccessor> result = parse(input);
    if (result.getStatus() == ParseResult.STATUS.FAIL
      && result.getConfidence() == ParseResult.CONFIDENCE.POSSIBLE
      && result.getAlternativePayloads().size() > 1) {
      for (DateComponentOrdering ordering : orderings) {
        result = parse(input, ordering);
        if (result.isSuccessful()) {
          return result;
        }
      }
    }
    return result;
  }

  @Override
  public ParseResult<TemporalAccessor> parse(
    @Nullable String year, @Nullable String month, @Nullable String day) {

    // avoid possible misinterpretation when month is not provided (but day is)
    if (StringUtils.isBlank(month) && StringUtils.isNotBlank(day)) {
      return ParseResult.fail();
    }

    String date = Stream.of(StringUtils.trimToNull(year), StringUtils.trimToNull(month), StringUtils.trimToNull(day))
        .filter(Objects::nonNull)
        .collect(Collectors.joining(String.valueOf(CHAR_HYPHEN)));

    TemporalAccessor tp = tryParse(date, ISO_PARSER, null);

    if (tp != null) {
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, tp);
    }
    return ParseResult.fail();
  }

  @Override
  public ParseResult<TemporalAccessor> parse(@Nullable Integer year, @Nullable Integer month, @Nullable Integer day) {

    // avoid possible misinterpretation when month is not provided (but day is)
    if (month == null && day != null) {
      return ParseResult.fail();
    }

    String date = Stream.of(year, month, day)
        .filter(Objects::nonNull)
        .map(String::valueOf)
        .collect(Collectors.joining(String.valueOf(CHAR_HYPHEN)));

    TemporalAccessor tp = tryParse(date, ISO_PARSER, null);

    if (tp != null) {
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, tp);
    }
    return ParseResult.fail();
  }

  /**
   * Utility private method to avoid throwing a runtime exception when the formatter can not parse the String.
   * TODO normalizer is call too often maybe this class should not take it and only try to parse
   *
   * @param input
   * @param formatter
   * @param normalizer
   * @return
   */
  private static TemporalAccessor tryParse(String input, DateTimeFormatter formatter, DateTimeSeparatorNormalizer normalizer) {

    if (normalizer != null) {
      input = normalizer.normalize(input);
    }
    try {
      return formatter.parseBest(input, ZonedDateTime::from, LocalDateTime::from, LocalDate::from,
        YearMonth::from, Year::from);
    } catch (DateTimeParseException dpe) {
    }
    return null;
  }

  /**
   * Check if all the TemporalAccessor of the list are equal.
   * <p>
   * Return false for an empty list; true for a list with a single element.
   */
  private static boolean allEquals(List<TemporalAccessor> taList) {
    if (taList == null || taList.isEmpty()) {
      return false;
    }

    TemporalAccessor reference = taList.get(1);
    for (TemporalAccessor ta : taList) {
      if (!ta.equals(reference)) {
        return false;
      }
    }
    return true;
  }
}
