package org.gbif.common.parsers.date;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

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
