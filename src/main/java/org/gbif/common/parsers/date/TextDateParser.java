package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.ParseResult;

import java.util.regex.Pattern;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * Main class to parse a date represented as a single String or as date parts into a {@link TemporalAccessor}.
 * If the String contains letters, the {@link TextualMonthDateTokenizer} and the {@link DatePartsNormalizer} will be used.
 * Otherwise, a {@link TemporalParser} configured for numerical dates will be used.
 *
 * This class is basically a decorator on top of default NumericalDateParser to handle months written in text.
 */
class TextDateParser implements TemporalParser {

  //private static final Pattern ISO_TIME_MARKER =  Pattern.compile("\\dT\\d");
  //private static final Pattern AT_LEAST_ONE_LETTER =  Pattern.compile("[a-zA-Z]+");
  //This regex is not complete and will NOT handle date when the time zone is provided as text GMT
  private static final Pattern NUMERICAL_DATE_PATTERN =  Pattern.compile("[^a-zA-Z]+[\\dT\\d]?[^a-zA-Z]+[Z]?$");
  private static final TextualMonthDateTokenizer TEXT_MONTH_TOKENIZER = TextualMonthDateTokenizer.newInstance();
  private static final TemporalParser NUMERICAL_DATE_PARSER = DateParsers.defaultNumericalDateParser();
  private static final DatePartsNormalizer DATE_PARTS_NORMALIZER = DatePartsNormalizer.newInstance();

  @Override
  public ParseResult<TemporalAccessor> parse(String input) {

    if(StringUtils.isBlank(input)){
      return ParseResult.fail();
    }

    // Check if the input text contains only punctuations and numbers
    // Also accept the T marker (e.g. 1978-12-21T02:12) from the ISO format
    // We could also simply try to parse it but it is probably not optimal
    //if(!AT_LEAST_ONE_LETTER.matcher(input).find() || ISO_TIME_MARKER.matcher(input).find()) {
    if(NUMERICAL_DATE_PATTERN.matcher(input).matches()) {
      return NUMERICAL_DATE_PARSER.parse(input, DateFormatHint.NONE);
    }

    TextualMonthDateTokenizer.DateTokens dt = TEXT_MONTH_TOKENIZER.tokenize(input);
    // for now we only handle cases where we can find year, month, day with confidence.
    if(!dt.containsDiscardedTokens() && dt.size() == 3){
      DatePartsNormalizer.NormalizedYearMonthDay normalizedYearMonthDay = DATE_PARTS_NORMALIZER.normalize(
              dt.getToken(TextualMonthDateTokenizer.TokenType.INT_4).getToken(),
              dt.getToken(TextualMonthDateTokenizer.TokenType.TEXT).getToken(),
              dt.getToken(TextualMonthDateTokenizer.TokenType.INT_2).getToken());

      //no handling for partial dates with textual month for now
      if(normalizedYearMonthDay.getYear() != null &&
              normalizedYearMonthDay.getMonth() != null &&
              normalizedYearMonthDay.getDay() != null){
        try {
          return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE,
                  (TemporalAccessor)LocalDate.of(normalizedYearMonthDay.getYear(),
                          normalizedYearMonthDay.getMonth(), normalizedYearMonthDay.getDay()));
        }
        catch (DateTimeException ignore){
          //simply ignore bad dates
        }
      }
    }
    return ParseResult.fail();
  }

  /**
   * For now this is directly delegated the NumericalDateParser.
   *
   * @param input
   * @param hint help to speed up the parsing and possibly return a better confidence
   * @return
   */
  @Override
  public ParseResult<TemporalAccessor> parse(String input, @Nullable DateFormatHint hint) {
    return NUMERICAL_DATE_PARSER.parse(input, hint);
  }

  /**
   * Parse date parts into a TemporalAccessor.
   * The {@link DatePartsNormalizer} will be applied on raw data.
   *
   * @param year
   * @param month
   * @param day
   * @return
   */
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

}
