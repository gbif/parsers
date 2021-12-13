/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.common.parsers.date;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;

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

  private static final Map<TokenType, Pattern> PATTERNS_BY_TYPE;

  static {
    Map<TokenType, Pattern> patternsByType = new HashMap<>();
    patternsByType.put(TokenType.INT_2, Pattern.compile("[0-9]{1,2}"));
    patternsByType.put(TokenType.INT_4, Pattern.compile("[0-9]{4}"));
    patternsByType.put(TokenType.TEXT, Pattern.compile("[A-Za-z.]{1,10}"));
    PATTERNS_BY_TYPE = Collections.unmodifiableMap(patternsByType);
  }

  /**
   * Private constructor use static method {@link #newInstance()}
   */
  private TextualMonthDateTokenizer() {}

  public static TextualMonthDateTokenizer newInstance(){
    return new TextualMonthDateTokenizer();
  }

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
    private final Map<TokenType, DateToken> tokens = new HashMap<>(3);
    private List<DateToken> discardedTokens = null;

    private void addToken(DateToken dateToken){
      DateToken prev = tokens.put(dateToken.type, dateToken);
      if(prev != null){
        addDiscardedToken(prev);
      }
    }

    private void addDiscardedToken(DateToken dateToken){
      if(discardedTokens == null){
        discardedTokens = new ArrayList<>();
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
      return Collections.unmodifiableList(discardedTokens);
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", DateTokens.class.getSimpleName() + "[", "]")
          .add("tokens=" + tokens)
          .add("discardedTokens=" + discardedTokens)
          .toString();
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
    public String toString() {
      return new StringJoiner(", ", DateToken.class.getSimpleName() + "[", "]")
          .add("token='" + token + "'")
          .add("type=" + type)
          .toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DateToken dateToken = (DateToken) o;
      return Objects.equals(token, dateToken.token) && type == dateToken.type;
    }

    @Override
    public int hashCode() {
      return Objects.hash(token, type);
    }
  }
}
