package org.gbif.common.parsers.date.threeten;

import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.common.parsers.date.DateFormatHint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Year;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.format.ResolverStyle;
import org.threeten.bp.format.SignStyle;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * Numerical DateParser based on threetenbp (JSR310 backport) library which also means it is almost ready for Java 8.
 * This is a numerical Date Parser which means it is not responsible to parse dates that contains text for representing
 * a part of the dates (e.g. January 1 1980)
 *
 * Months are in numerical value starting at 1 for January.
 *
 * This parser uses LocalDateTime and LocalDate which means it is TimeZone agnostic.
 *
 * Be aware that LocalDate and LocalDateTime doesn't map correctly to Date object for all dates before the
 * Gregorian cut-off date (1582-10-15). To transform a such date use GregorianCalendar by setting the date according
 * to the TemporalAccessor you got back from that class.
 *
 */
public class ThreeTenNumericalDateParser implements Parsable<TemporalAccessor> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThreeTenNumericalDateParser.class);

  public static final Pattern OPTIONAL_PATTERN_PART = Pattern.compile("\\[.*\\]");

  // ISO 8601 specifies a Unicode minus (CHAR_MINUS), with a hyphen (CHAR_HYPHEN) as an alternative.
  public static final char CHAR_HYPHEN = '\u002d'; // Unicode hyphen, U+002d, char '-'
  public static final char CHAR_MINUS = '\u2212'; // Unicode minus, U+2212, char '−'

  public enum DateParsingHint {MIN, MAX}

  private static List<ThreeTenDateTimeParser> DEFINITE_FORMATTERS;
  private static final Map<DateFormatHint, List<ThreeTenDateTimeParser>> FORMATTERS_BY_HINT = Maps.newHashMap();

  // DateTimeFormatter includes some ISO parsers but just to make it explicit we define our own
  private final static DateTimeFormatter ISO_PARSER = (new DateTimeFormatterBuilder()
          .appendValue(ChronoField.YEAR, 2, 4, SignStyle.NEVER)
          .optionalStart().appendLiteral('-')
          .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
          .optionalStart().appendLiteral('-')
          .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NEVER))
          .optionalEnd()
          .optionalEnd()
          .toFormatter().withResolverStyle(ResolverStyle.STRICT);

  //brackets [] represent optional section of the pattern
  //separator is a CHAR_HYPHEN
  private static List<ThreeTenDateTimeParser> DEFINITE_PATTERNS_LIST = ThreeTenNumericalDateParserBuilder
          .newParserListBuilder()
          .appendDateTimeParser("uuuuMMdd", DateFormatHint.YMD)
          .appendDateTimeParser("uuuu-M-d[ HH:mm:ss]", DateFormatHint.YMDT)
          .appendDateTimeParser("uuuu-M-d'T'HH[:mm[:ss]]", DateFormatHint.YMDT)
          .appendDateTimeParser("uuuu-M-d'T'HHmm[ss]", DateFormatHint.YMDT)
          .appendDateTimeParser("uuuu-M-d'T'HH:mm:ssZ", DateFormatHint.YMDT)
          .appendDateTimeParser("uuuu-M-d'T'HH:mm:ssxxx", DateFormatHint.YMDT) //covers 1978-12-21T02:12:43+01:00
          .appendDateTimeParser("uuuu-M-d'T'HH:mm:ss'Z'", DateFormatHint.YMDT)
          .appendDateTimeParser("uuuu-M", DateFormatHint.YM)
          .appendDateTimeParser("uuuu", DateFormatHint.Y)
          .appendDateTimeParser("uuuu年MM月dd日", DateFormatHint.HAN)
          .appendDateTimeParser("uuuu年M月d日", DateFormatHint.HAN)
          .appendDateTimeParser("uu年M月d日", DateFormatHint.HAN)
          .build();

  //Possibly ambiguous dates will record an error in case more than one pattern can be applied
  private static List<ThreeTenDateTimeMultiParser> POSSIBLY_AMBIGUOUS_PATTERNS =
          Lists.newArrayList(
                  ThreeTenNumericalDateParserBuilder.newMultiParserListBuilder()
                          .preferredDateTimeParser("d.M.uuuu", DateFormatHint.DMY) //DE, DK, NO
                          .appendDateTimeParser("M.d.uuuu", DateFormatHint.MDY)
                          .build(),
                  // the followings are mostly derived of the difference between FR,GB,ES (DMY) format and US format (MDY)
                  ThreeTenNumericalDateParserBuilder.newMultiParserListBuilder()
                          .appendDateTimeParser("d/M/uuuu", DateFormatHint.DMY, "/", String.valueOf(CHAR_HYPHEN) + String.valueOf(CHAR_MINUS))
                          .appendDateTimeParser("M/d/uuuu", DateFormatHint.MDY, "/", String.valueOf(CHAR_HYPHEN) + String.valueOf(CHAR_MINUS))
                          .build(),
                  ThreeTenNumericalDateParserBuilder.newMultiParserListBuilder()
                          .appendDateTimeParser("ddMMuuuu", DateFormatHint.DMY)
                          .appendDateTimeParser("MMdduuuu", DateFormatHint.MDY)
                          .build(),
                  // the followings are not officially supported by any countries but are sometimes used
                  ThreeTenNumericalDateParserBuilder.newMultiParserListBuilder()
                          .appendDateTimeParser("d\\M\\uuuu", DateFormatHint.DMY, "\\", "_")
                          .appendDateTimeParser("M\\d\\uuuu", DateFormatHint.MDY, "\\", "_")
                          .build()
          );

  public static ThreeTenNumericalDateParser getParser(){
    return new ThreeTenNumericalDateParser();
  }

  public static ThreeTenNumericalDateParser getParser(Year baseYear){
    return new ThreeTenNumericalDateParser(baseYear);
  }

  private ThreeTenNumericalDateParser(){
    DEFINITE_FORMATTERS = ImmutableList.copyOf(DEFINITE_PATTERNS_LIST);

    for(ThreeTenDateTimeParser parser : DEFINITE_PATTERNS_LIST){
      FORMATTERS_BY_HINT.putIfAbsent(parser.getHint(), new ArrayList<ThreeTenDateTimeParser>());
      FORMATTERS_BY_HINT.get(parser.getHint()).add(parser);
    }

    for(ThreeTenDateTimeMultiParser parserAmbiguity : POSSIBLY_AMBIGUOUS_PATTERNS){
      for(ThreeTenDateTimeParser parser : parserAmbiguity.getAllParsers()) {
        FORMATTERS_BY_HINT.putIfAbsent(parser.getHint(), new ArrayList<ThreeTenDateTimeParser>());
        FORMATTERS_BY_HINT.get(parser.getHint()).add(parser);
      }
    }
  }

  private ThreeTenNumericalDateParser(Year baseYear) {
    this();
    Preconditions.checkState(baseYear.getValue() <= LocalDate.now().getYear(), "Base year is less or equals to" +
            " the current year");
    addPossiblyAmbiguous2DigitsYear(baseYear);
  }

  /**
   * TODO review this
   * @param baseYear
   */
  private void addPossiblyAmbiguous2DigitsYear(Year baseYear){

    List<ThreeTenDateTimeMultiParser> POSSIBLY_AMBIGUOUS_2DIGITS_YEAR_PATTERNS = Lists.newArrayList(
            ThreeTenNumericalDateParserBuilder.newMultiParserListBuilder()
              .preferredDateTimeParser("d.M.uu", DateFormatHint.DMY, baseYear) //DE, DK, NO
            .appendDateTimeParser("M.d.uu", DateFormatHint.MDY, baseYear)
            .build(),
            ThreeTenNumericalDateParserBuilder.newMultiParserListBuilder()
                    .appendDateTimeParser("d/M/uu", DateFormatHint.DMY, "/",
                            String.valueOf(CHAR_HYPHEN) + String.valueOf(CHAR_MINUS), baseYear)
                    .appendDateTimeParser("M/d/uu", DateFormatHint.MDY, "/",
                            String.valueOf(CHAR_HYPHEN) + String.valueOf(CHAR_MINUS), baseYear)
                    .build(),
            ThreeTenNumericalDateParserBuilder.newMultiParserListBuilder()
                    .appendDateTimeParser("ddMMuu", DateFormatHint.DMY, baseYear)
                    .appendDateTimeParser("MMdduu", DateFormatHint.MDY, baseYear)
                    .build(),
            ThreeTenNumericalDateParserBuilder.newMultiParserListBuilder()
                    .appendDateTimeParser("d\\M\\uu", DateFormatHint.DMY, "\\", "_", baseYear)
                    .appendDateTimeParser("M\\d\\uu", DateFormatHint.MDY, "\\", "_", baseYear)
                    .build()
    );

      //2 digits year pattern is ambiguous
     // buildPossibleAmbiguousDateTimeParser(buildDateTimeParserFor2DigitYear("uu", DateFormatHint.Y, baseYear))
    //};

    for(ThreeTenDateTimeMultiParser parserAmbiguity : POSSIBLY_AMBIGUOUS_2DIGITS_YEAR_PATTERNS){
      for(ThreeTenDateTimeParser parser : parserAmbiguity.getAllParsers()) {
        FORMATTERS_BY_HINT.putIfAbsent(parser.getHint(), new ArrayList<ThreeTenDateTimeParser>());
        FORMATTERS_BY_HINT.get(parser.getHint()).add(parser);
      }
    }
    POSSIBLY_AMBIGUOUS_PATTERNS.addAll(Lists.newArrayList(POSSIBLY_AMBIGUOUS_2DIGITS_YEAR_PATTERNS));
  }

  @Override
  public ParseResult<TemporalAccessor> parse(String input) {
    return parse(input, DateFormatHint.NONE);
  }

  /**
   * Parse year, month, day strings as a TemporalAccessor.
   *
   * @param year
   * @param month
   * @param day
   * @return
   */
  public static ParseResult<TemporalAccessor> parse(@Nullable String year, @Nullable String month, @Nullable String day) {
    String date = Joiner.on(CHAR_HYPHEN).skipNulls().join(Strings.emptyToNull(year), Strings.emptyToNull(month),
            Strings.emptyToNull(day));
    TemporalAccessor tp = tryParse(date, ISO_PARSER, null);

    if(tp != null){
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, tp);
    }
    return ParseResult.fail();
  }

  /**
   *
   * @param input
   * @param hint
   * @return
   */
  public ParseResult<TemporalAccessor> parse(String input, DateFormatHint hint) {

    if(StringUtils.isBlank(input)){
      return ParseResult.fail();
    }
    // make sure hint is never null
    if(hint == null){
      hint = DateFormatHint.NONE;
    }

    List<ThreeTenDateTimeParser> parserList = FORMATTERS_BY_HINT.containsKey(hint) ? FORMATTERS_BY_HINT.get(hint) : DEFINITE_FORMATTERS;

    TemporalAccessor parsedTemporalAccessor;
    for(ThreeTenDateTimeParser parser : parserList){
      parsedTemporalAccessor = parser.parse(input);
      if(parsedTemporalAccessor != null){
        return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, parsedTemporalAccessor);
      }
    }

    // if a format hint was provided we already tried all possible format
    if( hint != DateFormatHint.NONE){
      return ParseResult.fail();
    }

    int numberOfPossiblyAmbiguousMatch = 0;
    TemporalAccessor lastParsedSuccess = null;
    TemporalAccessor lastParsedPreferred = null;
    ThreeTenDateTimeMultiParser.MultipleParseResult result;

    for(ThreeTenDateTimeMultiParser parserAmbiguity : POSSIBLY_AMBIGUOUS_PATTERNS){
      result = parserAmbiguity.parse(input);
      numberOfPossiblyAmbiguousMatch += result.getNumberParsed();

      if(result.getNumberParsed() > 0){
        lastParsedSuccess = result.getResult();

        if(result.getPreferredResult() != null){
          if(lastParsedPreferred != null){
            LOGGER.warn("Issue with ThreeTenDateTimeMultiParser configuration. {} produces 2 preferred result", input);
          }
          lastParsedPreferred = result.getPreferredResult();
        }
      }
    }

    //if there is only one pattern that can be applied it is not ambiguous
    if(numberOfPossiblyAmbiguousMatch == 1){
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, lastParsedSuccess);
    }
    else if ( numberOfPossiblyAmbiguousMatch > 1 ){
      //check if we have result from the preferred parser
      if(lastParsedPreferred != null){
        return ParseResult.success(ParseResult.CONFIDENCE.PROBABLE, lastParsedPreferred);
      }
    }

    LOGGER.debug("Number of matches for {} : {}", input, numberOfPossiblyAmbiguousMatch);
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
      return formatter.parseBest(input, LocalDateTime.FROM, LocalDate.FROM, YearMonth.FROM, Year.FROM);
    }
    catch (DateTimeParseException dpe){}
    return null;
  }

}
