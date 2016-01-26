package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
 */
public class ThreeTenNumericalDateParser implements Parsable<TemporalAccessor> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThreeTenNumericalDateParser.class);

  //Should be mutually exclusive
  //Hints are given to the parser to help to select the right sets of DateTimeFormatter
  public enum DateFormatHint {YMDT, YMD, DMY, MDY, YM, Y, ASIAN, NONE};

  private static final List<InternalDateTimeParser> DEFINITE_FORMATTERS;
  private static final Map<DateFormatHint, List<InternalDateTimeParser>> FORMATTERS_BY_HINT = Maps.newHashMap();

  //the letter 'u' in all the patterns refers to YEAR as opposed to 'y' who refers to YEAR_OF_ERA
  private final static String YEAR_2_DIGITS_PATTERN_SUFFIX = "uu";
  private final static String IS_YEAR_2_DIGITS_PATTERN = "^.+[^u]"+YEAR_2_DIGITS_PATTERN_SUFFIX+"$";

  //brackets [] represent optional section of the pattern
  protected static InternalDateTimeParser[] DEFINITE_PATTERNS = {
          buildDateTimeParser("uuuuMMdd", DateFormatHint.YMD),
          buildDateTimeParser("uuuu-MM-dd[ HH:mm:ss]", DateFormatHint.YMDT),
          buildDateTimeParser("uuuu-MM-dd'T'HH[:mm[:ss]]", DateFormatHint.YMDT),
          buildDateTimeParser("uuuu-MM-dd'T'HHmm[ss]", DateFormatHint.YMDT),
          buildDateTimeParser("uuuu-MM-dd'T'HH:mm:ssZ", DateFormatHint.YMDT),
          buildDateTimeParser("uuuu-MM-dd'T'HH:mm:ssxxx", DateFormatHint.YMDT), //covers 1978-12-21T02:12:43+01:00
          buildDateTimeParser("uuuu-MM-dd'T'HH:mm:ss'Z'", DateFormatHint.YMDT),
          buildDateTimeParser("uuuu-MM", DateFormatHint.YM),
          buildDateTimeParser("uuuu", DateFormatHint.Y),
          buildDateTimeParser("yyyy年mm月dd日", DateFormatHint.ASIAN),
          buildDateTimeParser("yyyy年m月d日", DateFormatHint.ASIAN),
          buildDateTimeParser("yy年m月d日", DateFormatHint.ASIAN)
  };

  //Possibly ambiguous dates will record an error in case more than one pattern can be applied
  protected static InternalDateTimeParserAmbiguity[] POSSIBLY_AMBIGUOUS_PATTERNS = {

          buildPossibleAmbiguousDateTimeParserWithPreferred(
                  buildDateTimeParser("dd.MM.uuuu", DateFormatHint.DMY), //DE, DK, NO
                  buildDateTimeParser("MM.dd.uuuu", DateFormatHint.MDY)
          ),
          buildPossibleAmbiguousDateTimeParserWithPreferred(
                  buildDateTimeParser("dd.MM.uu", DateFormatHint.DMY), //DE, DK, NO
                  buildDateTimeParser("MM.dd.uu", DateFormatHint.MDY)
          ),

          // the followings are mostly derived of the difference between FR,GB,ES (DMY) format and US format (MDY)
          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("dd/MM/uuuu", DateFormatHint.DMY),
                  buildDateTimeParser("MM/dd/uuuu", DateFormatHint.MDY)
          ),
          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("dd/MM/uu", DateFormatHint.DMY),
                  buildDateTimeParser("MM/dd/uu", DateFormatHint.MDY)
          ),

          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("dd-MM-uuuu", DateFormatHint.DMY),
                  buildDateTimeParser("MM-dd-uuuu", DateFormatHint.MDY)
          ),
          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("dd-MM-uu", DateFormatHint.DMY),
                  buildDateTimeParser("MM-dd-uu", DateFormatHint.MDY)
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
                  buildDateTimeParser("dd\\MM\\uuuu", DateFormatHint.DMY),
                  buildDateTimeParser("MM\\dd\\uuuu", DateFormatHint.MDY)
          ),
          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("dd\\MM\\uu", DateFormatHint.DMY),
                  buildDateTimeParser("MM\\dd\\uu", DateFormatHint.MDY)
          ),

          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("dd_MM_uuuu", DateFormatHint.DMY),
                  buildDateTimeParser("MM_dd_uuuu", DateFormatHint.MDY)
          ),
          buildPossibleAmbiguousDateTimeParser(
                  buildDateTimeParser("dd_MM_uu", DateFormatHint.DMY),
                  buildDateTimeParser("MM_dd_uu", DateFormatHint.MDY)
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
    // for the moment we do not support FormatHint
    return parse(input, DateFormatHint.NONE);
  }

  /**
   *
   * @param input
   * @param hint
   * @return
   */
  public static ParseResult<TemporalAccessor> parse(String input, DateFormatHint hint) {

    if(StringUtils.isBlank(input)){
      return ParseResult.error();
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
        parsedTemporalAccessor = tryParse(input, parser.getFormatter());
        if(parsedTemporalAccessor != null){
          return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, parsedTemporalAccessor);
        }
      }
    }
    // if a format hint was provided we already tried all possible format
    if( hint != DateFormatHint.NONE){
      return ParseResult.error();
    }

    // could be rewritten in a more DRY way
    int numberOfPossiblyAmbiguousMatch = 0;
    TemporalAccessor lastParsedTemporalAccessorSuccess = null;
    InternalDateTimeParserAmbiguity lastParserAmbiguitySuccess = null;
    for(InternalDateTimeParserAmbiguity parserAmbiguity : POSSIBLY_AMBIGUOUS_PATTERNS){
      for(InternalDateTimeParser parser : parserAmbiguity.getAllParsers()){
        if(usesDelimiter || !usesDelimiter && !parser.isWithDelimiters()){
          parsedTemporalAccessor = tryParse(input, parser.getFormatter());
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

    return ParseResult.error();
  }

  /**
   * Utility private method to avoid throwing a runtime exception when the formatter can not parse the String.
   *
   * @param input
   * @param formatter
   * @return
   */
  private static TemporalAccessor tryParse(String input, DateTimeFormatter formatter){
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
   * Internal representation of a DateTimeFormatter.
   */
  private static class InternalDateTimeParser {

    private DateTimeFormatter formatter;
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
      this.formatter = formatter;
      this.pattern = pattern;
      this.hint = hint;

      this.withDelimiters = !StringUtils.isAlpha(pattern) || hint == DateFormatHint.ASIAN;
    }

    public static InternalDateTimeParser of(DateTimeFormatter formatter, String pattern, DateFormatHint hint){
      return new InternalDateTimeParser(formatter, pattern, hint);
    }

    public DateTimeFormatter getFormatter(){
      return formatter;
    }

    public String getPattern() {
      return pattern;
    }

    public DateFormatHint getHint() {
      return hint;
    }

    public boolean isWithDelimiters() {
      return withDelimiters;
    }

  }

}
