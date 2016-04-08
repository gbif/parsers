package org.gbif.common.parsers.date;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;

/**
 * The contract of the {@link TextualMonthDateTokenizer} is to break a string representing a date with a textual
 * month representation into a list of {@link DateToken}. If no text can be found (e.g. ISO date) be aware that the
 * month will be returned as POSSIBLE_DAY token since no validation is performed on the value of the token.
 *
 * A {@link DateToken} should be interpreted as a possible/candidate for date part. No validation will be performed
 * by the {@link TextualMonthDateTokenizer}, simply patterns matching.
 *
 * This class is Thread-Safe
 */
public class TextualMonthDateTokenizer {

  public enum TokenType {
    /** Matches 4 integers */
    POSSIBLE_YEAR,
    /** Matches between 1 and 10 letters including the dot (.) */
    POSSIBLE_TEXT_MONTH,
    /** Matches 1 or 2 integer(s) */
    POSSIBLE_DAY }

  private static final String SEPARATOR_REGEX =  "[^A-Za-z0-9.]+";
  private static final Pattern DAY_SUFFIXES_PATTERN =  Pattern.compile("(?<=[0-9])st|nd|rd|th");

  private static final Map<TokenType, Pattern> PATTERNS_BY_TYPE = ImmutableMap.of(
          TokenType.POSSIBLE_DAY, Pattern.compile("[0-9]{1,2}"),
          TokenType.POSSIBLE_YEAR, Pattern.compile("[0-9]{4}"),
          TokenType.POSSIBLE_TEXT_MONTH, Pattern.compile("[A-Za-z.]{1,10}"));

  private static Function<DateToken, TokenType> DATE_TOKEN_TO_MAP_FUNCTION = new Function<DateToken, TokenType>() {
    @Override
    public TokenType apply(DateToken token) {
      return token.type;
    }
  };

  /**
   * Tokenize a string into a list of {@link DateToken}.
   *
   * @param str
   * @return list of {@link DateToken}, if none were found, an empty list, never null
   */
  public List<DateToken> tokenize(String str){
    str = DAY_SUFFIXES_PATTERN.matcher(str).replaceAll("");
    List<DateToken> tokens = Lists.newArrayList();
    String[] parts = str.split(SEPARATOR_REGEX);

    for(String part : parts){
      for(TokenType tokenType : PATTERNS_BY_TYPE.keySet()){
        if(PATTERNS_BY_TYPE.get(tokenType).matcher(part).matches()){
          tokens.add(new DateToken(part, tokenType));
        }
      }
    }
    return tokens;
  }

  /**
   * Test if the list of DateToken contains at least one object for all TokenType.
   *
   * @param tokens
   * @return
   */
  public static boolean allTokenTypesPresent(List<DateToken> tokens){
    return allTokenTypesPresent(tokens, false);
  }

  /**
   * Test if the list of DateToken contains one object for all TokenType.
   *
   * @param tokens
   * @param onlyOnce each TokenType should only appear once in the list
   * @return
   */
  public static boolean allTokenTypesPresent(List<DateToken> tokens, boolean onlyOnce){
    if(tokens.size() < TokenType.values().length || (onlyOnce && tokens.size() > TokenType.values().length)){
      return false;
    }

    boolean[] tokenTypesPresence = new boolean[TokenType.values().length];
    for(DateToken dt : tokens){
      tokenTypesPresence[dt.type.ordinal()] = true;
    }
    return !ArrayUtils.contains(tokenTypesPresence, false);
  }

  /**
   * Transform the dateTokens List into a Map using TokenType as key.
   * Make sure all TokenType are unique in the list see {@link #allTokenTypesPresent(List, boolean)} or token(s)
   * will be lost in the transformation.
   *
   * @param dateTokens
   * @return
   */
  public static Map<TokenType, DateToken> transformDateTokensToMap(List<DateToken> dateTokens){
    return Maps.uniqueIndex(dateTokens, DATE_TOKEN_TO_MAP_FUNCTION);
  }

  /**
   * Represents a possible candidate for date part. The value of the token represents what was provided and
   * may or may not be valid.
   */
  public static class DateToken {
    private final String token;
    private final TokenType type;

    DateToken(String token, TokenType tokenType){
      this.token = token;
      this.type = tokenType;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(token, type);
    }

    @Override
    public boolean equals(Object object){
      if (object instanceof DateToken) {
        DateToken that = (DateToken) object;
        return Objects.equal(this.token, that.token)
                && Objects.equal(this.type, that.type);
      }
      return false;
    }

    @Override
    public String toString(){
      return MoreObjects.toStringHelper(this)
              .add("token", token)
              .add("type", type).toString();
    }
  }
}
