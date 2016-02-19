package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
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
 * WORK IN PROGRESS
 *
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

  // ISO 8601 specifies a Unicode minus (CHAR_MINUS), with a hyphen (CHAR_HYPHEN) as an alternative.
  public static final char CHAR_HYPHEN = '\u002d'; // Unicode hyphen, U+002d, char '-'
  public static final char CHAR_MINUS = '\u2212'; // Unicode minus, U+2212, char '−'

  //Should be mutually exclusive
  //Hints are given to the parser to help to select the right sets of DateTimeFormatter
  //HAN = date format used in Chinese
  public enum DateFormatHint {YMDT, YMD, DMY, MDY, YM, Y, HAN, NONE}
  public enum DateParsingHint {MIN, MAX}

  private static final List<InternalDateTimeParser> DEFINITE_FORMATTERS;
  private static final Map<DateFormatHint, List<InternalDateTimeParser>> FORMATTERS_BY_HINT = Maps.newHashMap();

  //the letter 'u' in all the patterns refers to YEAR as opposed to 'y' who refers to YEAR_OF_ERA
  private final static String YEAR_2_DIGITS_PATTERN_SUFFIX = "uu";
  private final static String IS_YEAR_2_DIGITS_PATTERN = "^.+[^u]"+YEAR_2_DIGITS_PATTERN_SUFFIX+"$";

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
  private static InternalDateTimeParser[] DEFINITE_PATTERNS = {
          buildDateTimeParser("uuuuMMdd", DateFormatHint.YMD),
          buildDateTimeParser("uuuu-M-d[ HH:mm:ss]", DateFormatHint.YMDT),
          buildDateTimeParser("uuuu-M-d'T'HH[:mm[:ss]]", DateFormatHint.YMDT),
          buildDateTimeParser("uuuu-M-d'T'HHmm[ss]", DateFormatHint.YMDT),
          buildDateTimeParser("uuuu-M-d'T'HH:mm:ssZ", DateFormatHint.YMDT),
          buildDateTimeParser("uuuu-M-d'T'HH:mm:ssxxx", DateFormatHint.YMDT), //covers 1978-12-21T02:12:43+01:00
          buildDateTimeParser("uuuu-M-d'T'HH:mm:ss'Z'", DateFormatHint.YMDT),
          buildDateTimeParser("uuuu-M", DateFormatHint.YM),
          buildDateTimeParser("uuuu", DateFormatHint.Y),
          buildDateTimeParser("yyyy年mm月dd日", DateFormatHint.HAN),
          buildDateTimeParser("yyyy年m月d日", DateFormatHint.HAN),
          buildDateTimeParser("yy年m月d日", DateFormatHint.HAN)
  };

  //Possibly ambiguous dates will record an error in case more than one pattern can be applied
  private static InternalDateTimeParserAmbiguity[] POSSIBLY_AMBIGUOUS_PATTERNS = {

          buildPossibleAmbiguousDateTimeParserWithPreferred(
                  buildDateTimeParser("d.M.uuuu", DateFormatHint.DMY), //DE, DK, NO
                  buildDateTimeParser("M.d.uuuu", DateFormatHint.MDY)
          ),
          buildPossibleAmbiguousDateTimeParserWithPreferred(
                  buildDateTimeParser("d.M.uu", DateFormatHint.DMY), //DE, DK, NO
                  buildDateTimeParser("M.d.uu", DateFormatHint.MDY)
          ),

          // the followings are mostly derived of the difference between FR,GB,ES (DMY) format and US format (MDY)
          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("d/M/uuuu", DateFormatHint.DMY, "/", String.valueOf(CHAR_HYPHEN) + String.valueOf(CHAR_MINUS)),
                  buildDateTimeParser("M/d/uuuu", DateFormatHint.MDY, "/", String.valueOf(CHAR_HYPHEN) + String.valueOf(CHAR_MINUS))
          ),
          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("d/M/uu", DateFormatHint.DMY, "/", String.valueOf(CHAR_HYPHEN) + String.valueOf(CHAR_MINUS)),
                  buildDateTimeParser("M/d/uu", DateFormatHint.MDY, "/", String.valueOf(CHAR_HYPHEN) + String.valueOf(CHAR_MINUS))
          ),

          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("ddMMuuuu", DateFormatHint.DMY),
                  buildDateTimeParser("MMdduuuu", DateFormatHint.MDY)
          ),
          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("ddMMuu", DateFormatHint.DMY),
                  buildDateTimeParser("MMdduu", DateFormatHint.MDY)
          ),

          // the followings are not officially supported by any countries but are sometimes used
          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("d\\M\\uuuu", DateFormatHint.DMY, "\\", "_"),
                  buildDateTimeParser("M\\d\\uuuu", DateFormatHint.MDY, "\\", "_")
          ),
          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("d\\M\\uu", DateFormatHint.DMY, "\\", "_"),
                  buildDateTimeParser("M\\d\\uu", DateFormatHint.MDY, "\\", "_")
          ),

          //2 digits year pattern is ambiguous
          buildPossibleAmbiguousDateTimeParser(buildDateTimeParser("uu", DateFormatHint.Y))
  };

  // initialize DEFINITE_FORMATTERS and FORMATTERS_BY_HINT
  static{

    DEFINITE_FORMATTERS = ImmutableList.copyOf(DEFINITE_PATTERNS);

    for(InternalDateTimeParser parser : DEFINITE_PATTERNS){
      FORMATTERS_BY_HINT.putIfAbsent(parser.getHint(), new ArrayList<InternalDateTimeParser>());
      FORMATTERS_BY_HINT.get(parser.getHint()).add(parser);
    }

    for(InternalDateTimeParserAmbiguity parserAmbiguity : POSSIBLY_AMBIGUOUS_PATTERNS){
      for(InternalDateTimeParser parser : parserAmbiguity.getAllParsers()) {
        FORMATTERS_BY_HINT.putIfAbsent(parser.getHint(), new ArrayList<InternalDateTimeParser>());
        FORMATTERS_BY_HINT.get(parser.getHint()).add(parser);
      }
    }
  }

  private static InternalDateTimeParser buildDateTimeParser(String pattern, DateFormatHint hint){
    DateTimeFormatter dateTimeFormatter;

    if(pattern.matches(IS_YEAR_2_DIGITS_PATTERN) || pattern.equals(YEAR_2_DIGITS_PATTERN_SUFFIX)){
      dateTimeFormatter = buildWith2DigitYear(StringUtils.removeEnd(pattern, YEAR_2_DIGITS_PATTERN_SUFFIX));
    }
    else{
      dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    }

    return InternalDateTimeParser.of(dateTimeFormatter, pattern, hint);
  }

  /**
   *
   * @param pattern
   * @param hint
   * @param separator separator used in the pattern that should be used as replacement for alternativeSeparators
   * @param alternativeSeparators
   * @return
   */
  private static InternalDateTimeParser buildDateTimeParser(String pattern, DateFormatHint hint,
                                                            String separator, String alternativeSeparators){
    DateTimeFormatter dateTimeFormatter;

    InternalDateTimeNormalizer dateTimeNormalizer = new InternalDateTimeNormalizer(CharMatcher.anyOf(alternativeSeparators), separator);

    if(pattern.matches(IS_YEAR_2_DIGITS_PATTERN) || pattern.equals(YEAR_2_DIGITS_PATTERN_SUFFIX)){
      dateTimeFormatter = buildWith2DigitYear(StringUtils.removeEnd(pattern, YEAR_2_DIGITS_PATTERN_SUFFIX));
    }
    else{
      dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    }

    return InternalDateTimeParser.of(dateTimeFormatter, pattern, dateTimeNormalizer, hint);
  }

  private static InternalDateTimeParserAmbiguity buildPossibleAmbiguousDateTimeParserWithPreferred(InternalDateTimeParser preferred, InternalDateTimeParser ... possibleAmbiguity){
    return InternalDateTimeParserAmbiguity.of(preferred, Arrays.asList(possibleAmbiguity));
  }

  private static InternalDateTimeParserAmbiguity buildPossibleAmbiguousDateTimeParser(InternalDateTimeParser ... possibleAmbiguity){
    return InternalDateTimeParserAmbiguity.of(Arrays.asList(possibleAmbiguity));
  }

  /**
   * By default in JSR310 and Java 8, dates with 2 digits are 2000 based.
   * This method will APPEND a 2 digit year in the pattern.
   *
   * @param pattern must NOT include the year in the pattern (e.g. MM-dd- will create a formatter for MM-dd-uu)
   * @return
   */
  private static DateTimeFormatter buildWith2DigitYear(String pattern){
    return new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern(pattern)).appendValueReduced(
                    ChronoField.YEAR, 2, 2, Year.now().getValue() - 80).parseStrict().toFormatter();
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
  public static ParseResult<TemporalAccessor> parse(String input, DateFormatHint hint) {

    if(StringUtils.isBlank(input)){
      return ParseResult.fail();
    }
    // make sure hint is never null
    if(hint == null){
      hint = DateFormatHint.NONE;
    }

    //if there is no separator we can ignore all InternalDateTimeParser that use separator
    final boolean usesDelimiter = !StringUtils.isNumeric(input);

    List<InternalDateTimeParser> parserList = FORMATTERS_BY_HINT.containsKey(hint) ? FORMATTERS_BY_HINT.get(hint) : DEFINITE_FORMATTERS;

    TemporalAccessor parsedTemporalAccessor = null;
    for(InternalDateTimeParser parser : parserList){
      if(usesDelimiter || !usesDelimiter && !parser.isWithDelimiters()){
        parsedTemporalAccessor = tryParse(input, parser.getFormatter(), parser.getNormalizer());
        if(parsedTemporalAccessor != null){
          return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, parsedTemporalAccessor);
        }
      }
    }
    // if a format hint was provided we already tried all possible format
    if( hint != DateFormatHint.NONE){
      return ParseResult.fail();
    }

    // could be rewritten in a more DRY way
    int numberOfPossiblyAmbiguousMatch = 0;
    TemporalAccessor lastParsedTemporalAccessorSuccess = null;
    InternalDateTimeParserAmbiguity lastParserAmbiguitySuccess = null;
    for(InternalDateTimeParserAmbiguity parserAmbiguity : POSSIBLY_AMBIGUOUS_PATTERNS){
      for(InternalDateTimeParser parser : parserAmbiguity.getAllParsers()){
        if(usesDelimiter || !usesDelimiter && !parser.isWithDelimiters()){
          parsedTemporalAccessor = tryParse(input, parser.getFormatter(),parser.getNormalizer());
          if(parsedTemporalAccessor != null){
            numberOfPossiblyAmbiguousMatch++;
            lastParsedTemporalAccessorSuccess = parsedTemporalAccessor;
            lastParserAmbiguitySuccess = parserAmbiguity;
          }
        }
      }
    }

    //if there is only one pattern that can be applied it is not ambiguous
    if(numberOfPossiblyAmbiguousMatch == 1){
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, lastParsedTemporalAccessorSuccess);
    }
    else if ( numberOfPossiblyAmbiguousMatch > 1 ){
      //check if there is a preferred parser
      if(lastParserAmbiguitySuccess.getPreferredParser() != null){
        lastParsedTemporalAccessorSuccess = lastParserAmbiguitySuccess.getPreferredParser().getFormatter().parse(input);
        return ParseResult.success(ParseResult.CONFIDENCE.PROBABLE, lastParsedTemporalAccessorSuccess);
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
  private static TemporalAccessor tryParse(String input, DateTimeFormatter formatter, InternalDateTimeNormalizer normalizer){

    if(normalizer != null){
      input = normalizer.normalize(input);
    }

    try {
      return formatter.parseBest(input, LocalDateTime.FROM, LocalDate.FROM, YearMonth.FROM, Year.FROM);
    }
    catch (DateTimeParseException dpe){}
    return null;
  }


  /**
   * Internal representation of an ambiguity between 2 or more InternalDateTimeParser.
   *
   */
  private static class InternalDateTimeParserAmbiguity {

    private InternalDateTimeParser preferred;
    private List<InternalDateTimeParser> allParsers;

    /**
     * Get instance of a InternalDateTimeParserAmbiguity with a preferred.
     *
     * @param preferred
     * @param others
     * @return
     */
    public static InternalDateTimeParserAmbiguity of(InternalDateTimeParser preferred, List<InternalDateTimeParser> others){
      return new InternalDateTimeParserAmbiguity(preferred, others);
    }

    /**
     * Get instance of a InternalDateTimeParserAmbiguity without any preferred InternalDateTimeParser.
     *
     * @param others
     * @return
     */
    public static InternalDateTimeParserAmbiguity of(List<InternalDateTimeParser> others){
      return new InternalDateTimeParserAmbiguity(null, others);
    }

    private InternalDateTimeParserAmbiguity(InternalDateTimeParser preferred, List<InternalDateTimeParser> others){
      this.preferred = preferred;

      this.allParsers = Lists.newArrayList();
      if(preferred != null){
        allParsers.add(preferred);
      }
      allParsers.addAll(others);
    }

    /**
     * Get all parsers including the preferred one (if exists).
     * @return
     */
    public List<InternalDateTimeParser> getAllParsers(){
      return allParsers;
    }

    public InternalDateTimeParser getPreferredParser(){
      return preferred;
    }

  }

  /**
   * Internal normalizer used to support different separators
   */
  private static class InternalDateTimeNormalizer {
    private CharMatcher charMatcher;
    private String replacementChar;

    public InternalDateTimeNormalizer(CharMatcher charMatcher, String replacementChar){
      this.charMatcher = charMatcher;
      this.replacementChar = replacementChar;
    }

    public String normalize(String input){
      return charMatcher.replaceFrom(input, replacementChar);
    }
  }

  /**
   * Internal representation of a DateTimeFormatter.
   */
  private static class InternalDateTimeParser {

    private DateTimeFormatter formatter;
    private InternalDateTimeNormalizer normalizer;
    private String pattern;
    private DateFormatHint hint;

    private boolean withDelimiters;

    /**
     *
     * @param formatter
     * @param pattern
     * @param hint
     */
    private InternalDateTimeParser(DateTimeFormatter formatter, String pattern, DateFormatHint hint){
      this(formatter, pattern, null, hint);
    }

    private InternalDateTimeParser(DateTimeFormatter formatter, String pattern, InternalDateTimeNormalizer normalizer, DateFormatHint hint){
      this.formatter = formatter;
      this.pattern = pattern;
      this.hint = hint;
      this.normalizer = normalizer;

      this.withDelimiters = !StringUtils.isAlpha(pattern) || hint == DateFormatHint.HAN;
    }


    public static InternalDateTimeParser of(DateTimeFormatter formatter, String pattern, DateFormatHint hint){
      return new InternalDateTimeParser(formatter, pattern, hint);
    }

    public static InternalDateTimeParser of(DateTimeFormatter formatter, String pattern, InternalDateTimeNormalizer normalizer, DateFormatHint hint){
      return new InternalDateTimeParser(formatter, pattern, normalizer, hint);
    }

    public DateTimeFormatter getFormatter(){
      return formatter;
    }

    public String getPattern() {
      return pattern;
    }

    public InternalDateTimeNormalizer getNormalizer(){
      return normalizer;
    }

    public DateFormatHint getHint() {
      return hint;
    }

    public boolean isWithDelimiters() {
      return withDelimiters;
    }

  }

}
