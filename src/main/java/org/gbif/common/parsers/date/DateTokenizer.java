package org.gbif.common.parsers.date;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * The contract of the tokenizer is to break a string representing a date into a list of {@link DateToken}.
 * A {@link DateToken} should be interpreted as a possible/candidate for date part. No validation will be performed
 * by the {@link DateTokenizer}, simply patterns matching.
 */
public class DateTokenizer {

  public enum TokenType {POSSIBLE_YEAR, POSSIBLE_TEXT_MONTH, POSSIBLE_DAY }

  String separatorRegex = "[^A-Za-z0-9.]+";
  String daySuffixes = "(?<=[0-9])st|nd|rd|th";

  Map<TokenType, Pattern> PATTERNS_BY_TYPE = ImmutableMap.of(
          TokenType.POSSIBLE_DAY, Pattern.compile("[0-9]{1,2}"),
          TokenType.POSSIBLE_YEAR, Pattern.compile("[0-9]{4}"),
          TokenType.POSSIBLE_TEXT_MONTH, Pattern.compile("[A-Za-z.]{1,10}"));

  /**
   * Tokenize a string into {@link DateToken}
   *
   * @param str
   * @return
   */
  public List<DateToken> tokenize(String str){

    str = str.replaceAll(daySuffixes, "");
    List<DateToken> tokens = Lists.newArrayList();
    String[] parts = str.split(separatorRegex);

    for(String part : parts){
      for(TokenType tokenType : PATTERNS_BY_TYPE.keySet()){
        if(PATTERNS_BY_TYPE.get(tokenType).matcher(part).matches()){
          tokens.add(new DateToken(part, tokenType));
        }
      }
    }

    return tokens;
  }

  public static class DateToken {
    private String token;
    private TokenType type;

    public DateToken(String token, TokenType tokenType){
      this.token = token;
      this.type = tokenType;
    }

    @Override
    public int hashCode() {
      return Objects.hash(token, type);
    }

    @Override
    public boolean equals(Object object){
      if (object instanceof DateToken) {
        DateToken that = (DateToken) object;
        return Objects.equals(this.token, that.token)
                && Objects.equals(this.type, that.type);
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
