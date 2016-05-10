package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.common.parsers.date.threeten.ThreeTenNumericalDateParser;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * Main class to parse a date represented as a single String or date parts as String.
 * If the String contains letters, the {@link TextualMonthDateTokenizer} and the {@link DatePartsNormalizer} will be used.
 * Otherwise, {@link ThreeTenNumericalDateParser} will be used.
 */
public class TextDateParser implements Parsable<TemporalAccessor> {

  private static final Pattern AT_LEAST_ONE_LETTER =  Pattern.compile("[a-zA-Z]+");
  private static final TextualMonthDateTokenizer TEXT_MONTH_TOKENIZER = new TextualMonthDateTokenizer();
  private static final ThreeTenNumericalDateParser THREETEN_NUMERICAL_PARSER = ThreeTenNumericalDateParser.getParser();

  @Override
  public ParseResult<TemporalAccessor> parse(String input) {

    if(StringUtils.isBlank(input)){
      return ParseResult.fail();
    }

    //check if the input text contains only punctuations and numbers
    if(!AT_LEAST_ONE_LETTER.matcher(input).find()) {
      return THREETEN_NUMERICAL_PARSER.parse(input, DateFormatHint.NONE);
    }

    TextualMonthDateTokenizer.DateTokens dt = TEXT_MONTH_TOKENIZER.tokenize(input);
    // for now we only handle cases where we can find year, month, day with confidence.
    if(!dt.containsDiscardedTokens() && dt.size() == 3){
      DatePartsNormalizer.NormalizedYearMonthDay normalizedYearMonthDay = DatePartsNormalizer.normalize(
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
   * Parse date parts into a TemporalAccessor.
   * The {@link DatePartsNormalizer} will be applied on raw data.
   *
   * @param year
   * @param month
   * @param day
   * @return
   */
  public ParseResult<TemporalAccessor> parse(String year, String month, String day) {
    DatePartsNormalizer.NormalizedYearMonthDay normalizedYearMonthDay = DatePartsNormalizer.normalize(
            year, month, day);

    ParseResult<TemporalAccessor> parseResult = ThreeTenNumericalDateParser.parse(normalizedYearMonthDay.getYear(),
            normalizedYearMonthDay.getMonth(), normalizedYearMonthDay.getDay());

    //If we got a successful parsing BUT a part of the date was discarded we reduce confidence.
    if(parseResult.isSuccessful() && normalizedYearMonthDay.containsDiscardedPart()){
      return ParseResult.success(ParseResult.CONFIDENCE.PROBABLE, parseResult.getPayload());
    }

    return parseResult;
  }

}
