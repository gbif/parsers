package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.ParseResult;

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
import java.time.temporal.TemporalQuery;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.gbif.common.parsers.core.ParseResult.CONFIDENCE;
import org.gbif.common.parsers.core.ParseResult.STATUS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Map<DateFormatHint, List<DateTimeParser>> FORMATTERS_BY_HINT = Maps.newHashMap();

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
      ImmutableList.copyOf(
          DateTimeParserBuilder.newParserListBuilder()
                  .appendDateTimeParser("uuuuMMdd", DateFormatHint.YMD, LocalDate::from)
                  .appendDateTimeParser("uuuu-M-d[ HH[:]mm[:]ss]", DateFormatHint.YMDT, new TemporalQuery<?>[]{LocalDateTime::from, LocalDate::from}, HYPHEN, MINUS + ".")
                  // Either no fractional seconds, milliseconds or microseconds. T or space.
                  .appendDateTimeParser("uuuu-M-d' 'HH[[:]mm[[:]ss[.S]]]", DateFormatHint.YMDT, LocalDateTime::from)
                  .appendDateTimeParser("uuuu-M-d' 'HH[:]mm[[:]ss.SS]", DateFormatHint.YMDT, LocalDateTime::from)
                  .appendDateTimeParser("uuuu-M-d' 'HH[:]mm[[:]ss.SSS]", DateFormatHint.YMDT, LocalDateTime::from)
                  .appendDateTimeParser("uuuu-M-d' 'HH[:]mm[[:]ss.SSSSSS]", DateFormatHint.YMDT, LocalDateTime::from)
                  .appendDateTimeParser("uuuu-M-d' 'HH[:]mm[[:]ss.SSSSSSS]", DateFormatHint.YMDT, LocalDateTime::from)
                  .appendDateTimeParser("uuuu-M-d' 'HH[:]mm[[:]ss[.SSS]]X", DateFormatHint.YMDTZ, ZonedDateTime::from, HYPHEN, MINUS)
                  .appendDateTimeParser("uuuu-M-d'T'HH[[:]mm[[:]ss[.S]]]", DateFormatHint.YMDT, LocalDateTime::from)
                  .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss.SS]", DateFormatHint.YMDT, LocalDateTime::from)
                  .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss.SSS]", DateFormatHint.YMDT, LocalDateTime::from)
                  .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss.SSSSSS]", DateFormatHint.YMDT, LocalDateTime::from)
                  .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss.SSSSSSS]", DateFormatHint.YMDT, LocalDateTime::from)
                  // T, but with a time zone, accepting Z, +00, +0000 and +00:00 for UTC and - or − for negative offsets.
                  .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss[.SSS]]X", DateFormatHint.YMDTZ, ZonedDateTime::from, HYPHEN, MINUS)
                  .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[:]ss.SSSSSSX", DateFormatHint.YMDTZ, ZonedDateTime::from, HYPHEN, MINUS)
                  .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[[:]ss[.SSS]]xxx", DateFormatHint.YMDTZ, ZonedDateTime::from, HYPHEN, MINUS)
                  .appendDateTimeParser("uuuu-M-d'T'HH[:]mm[:]ss.SSSSSSxxx", DateFormatHint.YMDTZ, ZonedDateTime::from, HYPHEN, MINUS)

                  .appendDateTimeParser("uuuu-M", DateFormatHint.YM, YearMonth::from)
                  .appendDateTimeParser("uuuu", DateFormatHint.Y, Year::from)
                  .appendDateTimeParser("uuuu/MM/dd", DateFormatHint.YMD, LocalDate::from)
                  .appendDateTimeParser("uuuu/M/d", DateFormatHint.YMD, LocalDate::from)
                  .appendDateTimeParser("uuuu年MM月dd日", DateFormatHint.HAN, LocalDate::from)
                  .appendDateTimeParser("uuuu年M月d日", DateFormatHint.HAN, LocalDate::from)
                  .appendDateTimeParser("YYYY-'W'ww", DateFormatHint.YW, LocalDate::from) // ISO "week years", 2018-W43.
                  .appendDateTimeParser("uuuu-DDD", DateFormatHint.YD, LocalDate::from) // Week days, 2018-296.
                  .build()
  );

  // Possibly ambiguous dates will record an error in case more than one pattern can be applied
  private static final List<DateTimeMultiParser> MULTIPARSER_PARSER_LIST =
      ImmutableList.of(
          DateTimeParserBuilder.newMultiParserListBuilder()
              .preferredDateTimeParser(
                  "d.M.uuuu", DateFormatHint.DMY, LocalDate::from) // DE, DK, NO
              .appendDateTimeParser("M.d.uuuu", DateFormatHint.MDY, LocalDate::from)
              .build(),
          // the followings are mostly derived of the difference between FR,GB,ES (DMY) format and
          // US format (MDY)
          DateTimeParserBuilder.newMultiParserListBuilder()
              .appendDateTimeParser(
                  "d/M/uuuu'T'HH[[:]mm[[:]ss[.SSS]]]", DateFormatHint.EU_DMYT, LocalDateTime::from)
              .appendDateTimeParser(
                  "M/d/uuuu'T'HH[[:]mm[[:]ss[.SSS]]]", DateFormatHint.US_MDYT, LocalDateTime::from)
              .build(),
          DateTimeParserBuilder.newMultiParserListBuilder()
            .appendDateTimeParser(
              "d/M/uuuu'T'HH[[:]mm[[:]ss[.SSS]]][X]", DateFormatHint.EU_DMYT, ZonedDateTime::from)
            .appendDateTimeParser(
              "M/d/uuuu'T'HH[[:]mm[[:]ss[.SSS]]][X]", DateFormatHint.US_MDYT, ZonedDateTime::from)
            .build(),
          DateTimeParserBuilder.newMultiParserListBuilder()
            .appendDateTimeParser(
              "d/M/uuuu' 'HH[[:]mm[[:]ss[.SSS]]]", DateFormatHint.EU_DMYT, LocalDateTime::from)
            .appendDateTimeParser(
              "M/d/uuuu' 'HH[[:]mm[[:]ss[.SSS]]]", DateFormatHint.US_MDYT, LocalDateTime::from)
            .build(),
          DateTimeParserBuilder.newMultiParserListBuilder()
            .appendDateTimeParser(
              "d/M/uuuu' 'HH[[:]mm[[:]ss[.SSS]]][X]", DateFormatHint.EU_DMYT, ZonedDateTime::from)
            .appendDateTimeParser(
              "M/d/uuuu' 'HH[[:]mm[[:]ss[.SSS]]][X]", DateFormatHint.US_MDYT, ZonedDateTime::from)
          .build(),
         //Copied from DateFormatHint.DMY
          DateTimeParserBuilder.newMultiParserListBuilder()
           .appendDateTimeParser("d/M/uuuu", DateFormatHint.EU_DMYT, LocalDate::from)
           .appendDateTimeParser("M/d/uuuu", DateFormatHint.US_MDYT, LocalDate::from)
           .build(),
          // US EU ends.
          DateTimeParserBuilder.newMultiParserListBuilder()
              .appendDateTimeParser("d/M/uuuu", DateFormatHint.DMY, LocalDate::from,"/", HYPHEN + MINUS)
              .appendDateTimeParser("M/d/uuuu", DateFormatHint.MDY, LocalDate::from,"/", HYPHEN + MINUS)
              .build(),
          DateTimeParserBuilder.newMultiParserListBuilder()
              .appendDateTimeParser("ddMMuuuu", DateFormatHint.DMY, LocalDate::from)
              .appendDateTimeParser("MMdduuuu", DateFormatHint.MDY, LocalDate::from)
              .build(),
          // the followings are not officially supported by any countries but are sometimes used
          DateTimeParserBuilder.newMultiParserListBuilder()
              .appendDateTimeParser("d\\M\\uuuu", DateFormatHint.DMY, LocalDate::from, "\\", "_")
              .appendDateTimeParser("M\\d\\uuuu", DateFormatHint.MDY, LocalDate::from, "\\", "_")
              .build());

  static {
    for (DateTimeParser parser : BASE_PARSER_LIST) {
      FORMATTERS_BY_HINT.putIfAbsent(parser.getHint(), new ArrayList<>());
      FORMATTERS_BY_HINT.get(parser.getHint()).add(parser);
    }

    for (DateTimeMultiParser parserAmbiguity : MULTIPARSER_PARSER_LIST) {
      for (DateTimeParser parser : parserAmbiguity.getAllParsers()) {
        FORMATTERS_BY_HINT.putIfAbsent(parser.getHint(), new ArrayList<>());
        FORMATTERS_BY_HINT.get(parser.getHint()).add(parser);
      }
    }
  }

  // the active list/map are related to a specific instance
  private final Map<DateFormatHint, List<DateTimeParser>> activeFormattersByHint;
  private final List<DateTimeMultiParser> activeMultiParserList;

  /**
   * Get an instance of a default ThreeTenNumericalDateParser.
   *
   * @return
   */
  public static ThreeTenNumericalDateParser newInstance() {
    return new ThreeTenNumericalDateParser();
  }

  /**
   * Get an instance of a ThreeTenNumericalDateParser from a base year. Base year is used to handle
   * year represented by 2 digits.
   *
   * @param baseYear
   * @return
   */
  public static ThreeTenNumericalDateParser newInstance(Year baseYear) {
    return new ThreeTenNumericalDateParser(baseYear);
  }

  /**
   * Private constructor, use static methods {@link #newInstance()} and {@link #newInstance(Year)}.
   */
  private ThreeTenNumericalDateParser() {
    this.activeFormattersByHint = ImmutableMap.copyOf(FORMATTERS_BY_HINT);
    this.activeMultiParserList = MULTIPARSER_PARSER_LIST;
  }

  private ThreeTenNumericalDateParser(Year baseYear) {

    Preconditions.checkState(baseYear.getValue() <= LocalDate.now().getYear(), "Base year is less or equals to" +
              " the current year");

    Map<DateFormatHint, List<DateTimeParser>> formattersByHint = Maps.newHashMap(FORMATTERS_BY_HINT);

    List<DateTimeMultiParser> multiParserList = Lists.newArrayList(MULTIPARSER_PARSER_LIST);
    multiParserList.addAll(Lists.newArrayList(
            DateTimeParserBuilder.newMultiParserListBuilder()
                    .preferredDateTimeParser("d.M.uu", DateFormatHint.DMY, LocalDate::from, baseYear) //DE, DK, NO
                    .appendDateTimeParser("M.d.uu", DateFormatHint.MDY, LocalDate::from, baseYear)
                    .build(),
            DateTimeParserBuilder.newMultiParserListBuilder()
                    .appendDateTimeParser("d/M/uu", DateFormatHint.DMY, LocalDate::from, "/",HYPHEN + MINUS, baseYear)
                    .appendDateTimeParser("M/d/uu", DateFormatHint.MDY, LocalDate::from, "/",HYPHEN + MINUS, baseYear)
                    .build(),
            DateTimeParserBuilder.newMultiParserListBuilder()
                    .appendDateTimeParser("ddMMuu", DateFormatHint.DMY, LocalDate::from, baseYear)
                    .appendDateTimeParser("MMdduu", DateFormatHint.MDY, LocalDate::from, baseYear)
                    .build(),
            DateTimeParserBuilder.newMultiParserListBuilder()
                    .appendDateTimeParser("d\\M\\uu", DateFormatHint.DMY, LocalDate::from, "\\", "_", baseYear)
                    .appendDateTimeParser("M\\d\\uu", DateFormatHint.MDY, LocalDate::from, "\\", "_", baseYear)
                    .build()
    ));

    for(DateTimeMultiParser multiParser : multiParserList){
      for(DateTimeParser parser : multiParser.getAllParsers()) {
        formattersByHint.putIfAbsent(parser.getHint(), new ArrayList<DateTimeParser>());
        formattersByHint.get(parser.getHint()).add(parser);
      }
    }

    this.activeMultiParserList = ImmutableList.copyOf(multiParserList);
    this.activeFormattersByHint = ImmutableMap.copyOf(formattersByHint);
  }

  @Override
  public ParseResult<TemporalAccessor> parse(String input) {
    return parse(input, DateFormatHint.NONE);
  }

  @Override
  public ParseResult<TemporalAccessor> parse(String input, @Nullable DateFormatHint hint) {

    if(StringUtils.isBlank(input)){
      return ParseResult.fail();
    }
    // make sure hint is never null
    if (hint == null) {
      hint = DateFormatHint.NONE;
    }

    List<DateTimeParser> parserList =
        activeFormattersByHint.containsKey(hint)
            ? activeFormattersByHint.get(hint)
            : BASE_PARSER_LIST;

    // First attempt: find a match with definite confidence
    TemporalAccessor parsedTemporalAccessor;
    for (DateTimeParser parser : parserList) {
      parsedTemporalAccessor = parser.parse(input);
      if (parsedTemporalAccessor != null) {
        return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, parsedTemporalAccessor);
      }
    }

    // if a format hint was provided we already tried all possible format
    if (hint != DateFormatHint.NONE) {
      return ParseResult.fail();
    }

    // Second attempt: find one or multiple matches in the list of DateTimeMultiParser
    int numberOfPossiblyAmbiguousMatch = 0;
    TemporalAccessor lastParsedSuccess = null;
    TemporalAccessor lastParsedPreferred = null;
    Set<TemporalAccessor> otherParsed = new HashSet();
    //Checking if result is same due to duplicated format in multiple hints
    //like "d/M/uu" is defined in DateFormatHint.DMY and DateFormatHint.US_DMYT, it will generate same dates.
    List<TemporalAccessor> verificationDuplication = new ArrayList<>();
    // Are the results all equal (representing the same TemporalAccessor), used if there is no
    // preferred result defined
    boolean lastParsedSuccessOtherResultsEqual = false;
    DateTimeMultiParser.MultipleParseResult result = null;

    // here we do not stop when we find a match, we try them all to check for a possible ambiguity
    for (DateTimeMultiParser parserAmbiguity : activeMultiParserList) {
      result = parserAmbiguity.parse(input);

      if(result.getNumberParsed() > 0){
        lastParsedSuccess = result.getResult();
        //NOTE: if we define same formats into multiple hints,
        // like "d/M/uu" is defined in DateFormatHint.DMY and DateFormatHint.US_DMYT, it will generate same dates.
        // <code>otherResults</code> is a HashSet, value should be unique
        // thus, it should not be counted into numberOfPossiblyAmbiguousMatch
        if(!verificationDuplication.contains(lastParsedSuccess)) {
          numberOfPossiblyAmbiguousMatch += result.getNumberParsed();
          verificationDuplication.add(lastParsedSuccess);
        }


        // make sure to log in case lastParsedSuccessOtherResultsEqual already equals true
        if (lastParsedSuccessOtherResultsEqual) {
          LOGGER.warn("Issue with DateTimeMultiParser configuration: Input {} produces more results even " +
                  "if lastParsedSuccessOtherResultsEqual is set to true.", input);
        }
        lastParsedSuccessOtherResultsEqual = false;

        if(result.getPreferredResult() != null){
          if(lastParsedPreferred != null){
            LOGGER.warn("Issue with DateTimeMultiParser configuration: Input {} produces 2 preferred results", input);
          }
          lastParsedPreferred = result.getPreferredResult();
        }
        else{
          //if we have no PreferredResult but all results represent the same TemporalAccessor
          if(result.getOtherResults() != null && result.getOtherResults().size() > 1) {
            lastParsedSuccessOtherResultsEqual = allEquals(result.getOtherResults());
            otherParsed.addAll(result.getOtherResults());
          }
        }
      }
    }

    //if there is only one pattern that can be applied it is not ambiguous
    if(numberOfPossiblyAmbiguousMatch == 1){
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, lastParsedSuccess);
    }
    else if ( numberOfPossiblyAmbiguousMatch > 1 ){
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
      return new ParseResult(ParseResult.STATUS.FAIL, ParseResult.CONFIDENCE.POSSIBLE, null, new ArrayList(otherParsed), null);
    }
  }

  @Override
  public ParseResult<TemporalAccessor> parse(
      @Nullable String year, @Nullable String month, @Nullable String day) {

    // avoid possible misinterpretation when month is not provided (but day is)
    if (StringUtils.isBlank(month) && StringUtils.isNotBlank(day)) {
      return ParseResult.fail();
    }

    String date = Joiner.on(CHAR_HYPHEN).skipNulls().join(Strings.emptyToNull(year), Strings.emptyToNull(month),
            Strings.emptyToNull(day));
    TemporalAccessor tp = tryParse(date, ISO_PARSER, null);

    if (tp != null) {
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, tp);
    }
    return ParseResult.fail();
  }

  @Override
  public ParseResult<TemporalAccessor> parse(@Nullable Integer year, @Nullable Integer month, @Nullable Integer day) {

    // avoid possible misinterpretation when month is not provided (but day is)
    if(month == null && day != null){
      return ParseResult.fail();
    }

    String date = Joiner.on(CHAR_HYPHEN).skipNulls().join(year, month, day);
    TemporalAccessor tp = tryParse(date, ISO_PARSER, null);

    if(tp != null){
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, tp);
    }
    return ParseResult.fail();
  }

  /**
   * Utility private method to avoid throwing a runtime exception when the formatter can not parse the String.
   * TODO normalizer is call too often maybe this class should not take it and only try to parse
   * @param input
   * @param formatter
   * @param normalizer
   * @return
   */
  private static TemporalAccessor tryParse(String input, DateTimeFormatter formatter, DateTimeSeparatorNormalizer normalizer){

    if(normalizer != null){
      input = normalizer.normalize(input);
    }
    try {
      return formatter.parseBest(input, ZonedDateTime::from, LocalDateTime::from, LocalDate::from,
              YearMonth::from, Year::from);
    }
    catch (DateTimeParseException dpe){}
    return null;
  }

  /**
   * Check if all the TemporalAccessor of the list are equal.
   *
   * Return false for an empty list; true for a list with a single element.
   */
  private static boolean allEquals(List<TemporalAccessor> taList){
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
