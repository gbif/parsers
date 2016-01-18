package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Year;
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
  public enum DateParsingHint {YMDT, YMD, DMY, MDY, YM, Y, ASIAN, NONE};

  private final static Map<DateParsingHint, List<InternalDateTimeParser>> FORMATTERS_HINT = Maps.newHashMap();

  //the letter 'u' in all the patterns refers to YEAR as opposed to 'y' who refers to YEAR_OF_ERA
  private final static String YEAR_2_DIGITS_PATTERN_SUFFIX = "uu";
  private final static String IS_YEAR_2_DIGITS_PATTERN = "^.+[^u]"+YEAR_2_DIGITS_PATTERN_SUFFIX+"$";

  protected static InternalDateTimeParser[] PATTERNS = {
          buildDateTimeParser("uuuu-MM-dd", DateParsingHint.YMD),
          buildDateTimeParser("uuuu-MM-dd HH:mm:ss", DateParsingHint.YMDT),
          buildDateTimeParser("uuuu-MM-dd'T'HH", DateParsingHint.YMDT),
          buildDateTimeParser("uuuu-MM-dd'T'HH:mm", DateParsingHint.YMDT),
          buildDateTimeParser("uuuu-MM-dd'T'HH:mm:ss", DateParsingHint.YMDT),
          buildDateTimeParser("uuuu-MM-dd'T'HHmm", DateParsingHint.YMDT),
          buildDateTimeParser("uuuu-MM-dd'T'HHmmss", DateParsingHint.YMDT),
          buildDateTimeParser("uuuu-MM-dd'T'HH:mm:ssZ", DateParsingHint.YMDT),
          buildDateTimeParser("uuuu-MM-dd'T'HH:mm:ssxxx", DateParsingHint.YMDT), //covers 1978-12-21T02:12:43+01:00
          buildDateTimeParser("uuuu-MM-dd'T'HH:mm:ss'Z'", DateParsingHint.YMDT),
          buildDateTimeParser("yyyy-MM", DateParsingHint.YM),
          buildDateTimeParser("yyyy", DateParsingHint.Y),
          buildDateTimeParser("yy", DateParsingHint.Y),

          //possibly ambiguous patterns
          buildPossiblyAmbiguousDateTimeParser("dd/MM/uu", DateParsingHint.DMY),
          buildPossiblyAmbiguousDateTimeParser("dd/MM/uuuu", DateParsingHint.DMY),
          buildPossiblyAmbiguousDateTimeParser("dd\\MM\\uuuu", DateParsingHint.DMY),
          buildPossiblyAmbiguousDateTimeParser("dd-MM-uuuu", DateParsingHint.DMY),
          buildPossiblyAmbiguousDateTimeParser("dd_MM_uuuu", DateParsingHint.DMY),

          buildPossiblyAmbiguousDateTimeParser("ddMMuuuu", DateParsingHint.DMY),
          buildPossiblyAmbiguousDateTimeParser("dd.MM.uuuu", DateParsingHint.DMY),

          buildPossiblyAmbiguousDateTimeParser("MM/dd/uuuu", DateParsingHint.MDY),
          buildPossiblyAmbiguousDateTimeParser("MMdduuuu", DateParsingHint.MDY),
          buildPossiblyAmbiguousDateTimeParser("MM\\dd\\uuuu", DateParsingHint.MDY),
          buildPossiblyAmbiguousDateTimeParser("MM.dd.uuuu", DateParsingHint.MDY),
          buildPossiblyAmbiguousDateTimeParser("MM-dd-uuuu", DateParsingHint.MDY),
          buildPossiblyAmbiguousDateTimeParser("MM_dd_uuuu", DateParsingHint.MDY),

          //2 digit year patterns
          //TODO test carefully
          buildPossiblyAmbiguousDateTimeParser("ddMMuu", DateParsingHint.DMY),
          buildPossiblyAmbiguousDateTimeParser("dd.MM.uu", DateParsingHint.DMY),
          buildPossiblyAmbiguousDateTimeParser("dd_MM_uu", DateParsingHint.DMY),
          buildPossiblyAmbiguousDateTimeParser("dd-MM-uu", DateParsingHint.DMY),
          buildPossiblyAmbiguousDateTimeParser("dd\\MM\\uu", DateParsingHint.DMY),
          buildPossiblyAmbiguousDateTimeParser("MM/dd/uu", DateParsingHint.MDY),
          buildPossiblyAmbiguousDateTimeParser("MMdduu", DateParsingHint.MDY),
          buildPossiblyAmbiguousDateTimeParser("MM\\dd\\uu", DateParsingHint.MDY),
          buildPossiblyAmbiguousDateTimeParser("MM.dd.uu", DateParsingHint.MDY),
          buildPossiblyAmbiguousDateTimeParser("MM-dd-uu", DateParsingHint.MDY),
          buildPossiblyAmbiguousDateTimeParser("MM_dd_uu", DateParsingHint.MDY),

          buildDateTimeParser("yyyy年mm月dd日", DateParsingHint.ASIAN),
          buildDateTimeParser("yyyy年m月d日", DateParsingHint.ASIAN),
          buildDateTimeParser("yy年m月d日", DateParsingHint.ASIAN)
  };

  private static final List<InternalDateTimeParser> DATE_FORMATTERS;

  static{
    DATE_FORMATTERS = Lists.newArrayList();

    for(InternalDateTimeParser parser : PATTERNS){
      DATE_FORMATTERS.add(parser);
      //keep a reference to the formatter by mapping it with his hint key
      if(!FORMATTERS_HINT.containsKey(parser.getHint())){
        FORMATTERS_HINT.put(parser.getHint(), new ArrayList<InternalDateTimeParser>());
      }
      FORMATTERS_HINT.get(parser.getHint()).add(parser);
    }
  }

  private static InternalDateTimeParser buildDateTimeParser(String pattern, DateParsingHint hint){

    return InternalDateTimeParser.of(DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT),
            pattern, hint);
  }

  private static InternalDateTimeParser buildPossiblyAmbiguousDateTimeParser(String pattern, DateParsingHint hint){
    DateTimeFormatter dateTimeFormatter = null;

    if(pattern.matches(IS_YEAR_2_DIGITS_PATTERN)){
      dateTimeFormatter = buildWith2DigitYear(StringUtils.removeEnd(pattern, YEAR_2_DIGITS_PATTERN_SUFFIX));
    }
    else{
      dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    }
    return InternalDateTimeParser.ofPossiblyAmbiguous(dateTimeFormatter, pattern, hint);
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
    return parse(input, DateParsingHint.NONE);
  }

  /**
   *
   * @param input
   * @param hint
   * @return
   */
  public static ParseResult<TemporalAccessor> parse(String input, DateParsingHint hint) {

    if(StringUtils.isBlank(input)){
      return ParseResult.error();
    }

    //if there is no separator we can ignore all InternalDateTimeParser that use separator
    boolean usesDelimiter = !StringUtils.isNumeric(input);

    List<InternalDateTimeParser> parserList = FORMATTERS_HINT.containsKey(hint) ? FORMATTERS_HINT.get(hint) : DATE_FORMATTERS;

    int numberOfPossiblyAmbiguousMatch = 0;
    TemporalAccessor lastParsedTemporalAccessorSuccess = null;
    TemporalAccessor parsedTemporalAccessor = null;
    for(InternalDateTimeParser parser : parserList){
      if(usesDelimiter || !usesDelimiter && !parser.isWithDelimiters()){
        parsedTemporalAccessor = tryParse(input, parser.getFormatter());
        if(parsedTemporalAccessor != null){
          if(parser.isPossiblyAmbiguous()){
            numberOfPossiblyAmbiguousMatch++;
            lastParsedTemporalAccessorSuccess = parsedTemporalAccessor;
          }
          else {
            return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, parsedTemporalAccessor);
          }
        }
      }
    }

    //if there is only one pattern that can be applied it is not ambiguous
    if(numberOfPossiblyAmbiguousMatch == 1){
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, lastParsedTemporalAccessorSuccess);
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
      return formatter.parseBest(input, LocalDateTime.FROM, LocalDate.FROM);
    }
    catch (DateTimeParseException dpe){}
    return null;
  }


  private static class InternalDateTimeParser {

    private DateTimeFormatter formatter;
    private String pattern;
    private DateParsingHint hint;

    private boolean possiblyAmbiguous;
    private boolean withDelimiters;

    /**
     *
     * @param formatter
     * @param pattern
     * @param hint
     */
    private InternalDateTimeParser(DateTimeFormatter formatter, String pattern, DateParsingHint hint){
      this(formatter, pattern,  hint, false);
    }

    private InternalDateTimeParser(DateTimeFormatter formatter, String pattern, DateParsingHint hint, boolean possiblyAmbiguous){
      this.formatter = formatter;
      this.pattern = pattern;
      this.hint = hint;
      this.possiblyAmbiguous = possiblyAmbiguous;

      this.withDelimiters = !StringUtils.isAlpha(pattern) || hint == DateParsingHint.ASIAN;
    }

    public static InternalDateTimeParser of(DateTimeFormatter formatter, String pattern, DateParsingHint hint){
      return new InternalDateTimeParser(formatter, pattern, hint);
    }

    public static InternalDateTimeParser ofPossiblyAmbiguous(DateTimeFormatter formatter, String pattern, DateParsingHint hint){
      return new InternalDateTimeParser(formatter, pattern, hint, true);
    }

    public DateTimeFormatter getFormatter(){
      return formatter;
    }

    public String getPattern() {
      return pattern;
    }

    public DateParsingHint getHint() {
      return hint;
    }

    public boolean isPossiblyAmbiguous() {
      return possiblyAmbiguous;
    }

    public boolean isWithDelimiters() {
      return withDelimiters;
    }

  }

}
