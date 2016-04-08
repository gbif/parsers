package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * Main class to parse date represented as a single String.
 * If the String contains letters, the {@link TextualMonthDateTokenizer} and the {@link DateNormalizer} will be used.
 * Otherwise, {@link ThreeTenNumericalDateParser} will be used.
 */
public class TextDateParser implements Parsable<TemporalAccessor> {

  private static final Pattern AT_LEAST_ONE_LETTER =  Pattern.compile("[a-zA-Z]+");
  private static final TextualMonthDateTokenizer TEXT_MONTH_TOKENIZER = new TextualMonthDateTokenizer();

  @Override
  public ParseResult<TemporalAccessor> parse(String input) {

    if(StringUtils.isBlank(input)){
      return ParseResult.fail();
    }

    //check if the input text contains only punctuations and numbers
    if(!AT_LEAST_ONE_LETTER.matcher(input).find()) {
      return ThreeTenNumericalDateParser.parse(input, ThreeTenNumericalDateParser.DateFormatHint.NONE);
    }

    TextualMonthDateTokenizer.DateTokens dt = TEXT_MONTH_TOKENIZER.tokenize(input);
    if(!dt.containsDiscardedTokens() && dt.size() == 3){
      DateNormalizer.NormalizedYearMonthDay normalizedYearMonthDay = DateNormalizer.normalize(
              dt.getToken(TextualMonthDateTokenizer.TokenType.INT_4).getToken(),
              dt.getToken(TextualMonthDateTokenizer.TokenType.TEXT).getToken(),
              dt.getToken(TextualMonthDateTokenizer.TokenType.INT_2).getToken());

      if(normalizedYearMonthDay.getYear() != null &&
              normalizedYearMonthDay.getMonth() != null &&
              normalizedYearMonthDay.getDay() != null){
        try {
          return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE,
                  (TemporalAccessor)LocalDate.of(normalizedYearMonthDay.getYear(), normalizedYearMonthDay.getMonth(),
                  normalizedYearMonthDay.getDay()));
        }
        catch (DateTimeException ignore){
        }
      }
    }
    return ParseResult.fail();
  }

}
