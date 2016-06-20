package org.gbif.common.parsers.date;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

/**
 * The contract of the {@link TextualMonthDateTokenizer} is to break a string representing a date with a textual
 * month representation into a list of {@link DateToken}.
 *
 * A {@link DateToken} should be interpreted as a possible/candidate for date part. No validation will be performed
 * by the {@link TextualMonthDateTokenizer}, simply patterns matching.
 *
 * The class it-self is Thread-Safe, see nested classes for specific details about them.
 */
public class TextualMonthDateTokenizer {

  public enum TokenType {
    /** Matches 1 or 2 integer(s) (possibly a day) */
    INT_2,
    /** Matches 4 integers (possibly a year) */
    INT_4,
    /** Matches between 1 and 10 letters including the dot (.) */
    TEXT
  }

  private static final Pattern SEPARATOR_PATTERN =  Pattern.compile("[^A-Za-z0-9.]+");
  private static final Pattern DAY_SUFFIXES_PATTERN =  Pattern.compile("(?<=[0-9]{1,2})(st|nd|rd|th|\\.)",
          Pattern.CASE_INSENSITIVE);

  private static final Map<TokenType, Pattern> PATTERNS_BY_TYPE = ImmutableMap.of(
          TokenType.INT_2, Pattern.compile("[0-9]{1,2}"),
          TokenType.INT_4, Pattern.compile("[0-9]{4}"),
          TokenType.TEXT, Pattern.compile("[A-Za-z.]{1,10}"));

  /**
   * Tokenize a string into a {@link DateTokens}.
   *
   * @param str
   * @return {@link DateTokens} instance, or null if str is null or empty
   */
  public DateTokens tokenize(String str) {
    if (StringUtils.isBlank(str)) {
      return null;
    }

    str = DAY_SUFFIXES_PATTERN.matcher(str).replaceAll("");
    DateTokens tokens = new DateTokens();

    String[] parts = SEPARATOR_PATTERN.split(str);
    for (String part : parts) {
      for (TokenType tokenType : PATTERNS_BY_TYPE.keySet()) {
        if (PATTERNS_BY_TYPE.get(tokenType).matcher(part).matches()) {
          tokens.addToken(new DateToken(part, tokenType));
          //should always match only on pattern
          break;
        }
      }
    }
    return tokens;
  }

  /**
   * Contains the result of the tokenization.
   * DateToken are stored by TokenType on a 1 to 1 assumption.
   * If a DateToken already exists for the same TokenType it will be replaced and the previous one will be moved to the
   * discardedTokens list.
   *
   * This class is NOT Thread-Safe
   *
   */
  public static class DateTokens {
    private final Map<TokenType, DateToken> tokens = Maps.newHashMapWithExpectedSize(3);
    private List<DateToken> discardedTokens = null;

    private void addToken(DateToken dateToken){
      DateToken prev = tokens.put(dateToken.type, dateToken);
      if(prev != null){
        addDiscardedToken(prev);
      }
    }

    private void addDiscardedToken(DateToken dateToken){
      if(discardedTokens == null){
        discardedTokens = Lists.newArrayList();
      }
      discardedTokens.add(dateToken);
    }

    /**
     * Checks if some DateToken were discarded during the tokenization.
     *
     * @return
     */
    public boolean containsDiscardedTokens(){
      return discardedTokens != null;
    }

    /**
     * Size does NOT include discarded token(s).
     *
     * @return
     */
    public int size(){
      return tokens.size();
    }

    public DateToken getToken(TokenType tokenType){
      return tokens.get(tokenType);
    }

    public List<DateToken> getDiscardedTokens() {
      return ImmutableList.copyOf(discardedTokens);
    }

    @Override
    public String toString(){
      return MoreObjects.toStringHelper(this)
              .add("tokens", tokens)
              .add("discardedTokens", discardedTokens).toString();
    }

  }

  /**
   * Represents a possible candidate for date part. The value of the token represents what was provided and
   * may or may not be valid.
   *
   * This class is Thread-Safe.
   */
  public static class DateToken {
    private final String token;
    private final TokenType type;

    DateToken(String token, TokenType tokenType){
      this.token = token;
      this.type = tokenType;
    }

    public String getToken() {
      return token;
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
