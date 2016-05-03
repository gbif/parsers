package org.gbif.common.parsers.date.threeten;

import com.google.common.base.CharMatcher;

/**
 * Internal (package protected) normalizer used to support different separators for the same pattern.
 */
class DateTimeSeparatorNormalizer {

  private CharMatcher charMatcher;
  private String replacementChar;

  public DateTimeSeparatorNormalizer(CharMatcher charMatcher, String replacementChar){
    this.charMatcher = charMatcher;
    this.replacementChar = replacementChar;
  }

  public String normalize(String input){
    return charMatcher.replaceFrom(input, replacementChar);
  }
}
