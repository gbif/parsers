package org.gbif.common.parsers;

import org.gbif.api.util.VocabularyUtils;
import org.gbif.api.vocabulary.Sex;
import org.gbif.common.parsers.core.EnumParser;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/continents.txt.
 */
public class SexParser extends EnumParser<Sex> {

  private static SexParser singletonObject = null;

  private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER.or(CharMatcher.WHITESPACE).precomputed();
  private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.WHITESPACE.precomputed();


  private SexParser() {
    super(Sex.class, false);
    // also make sure we have all official iso countries mapped
    for (Sex c : Sex.values()) {
      add(c.name(), c);
    }
    // use dict file last
    init(SexParser.class.getResourceAsStream("/dictionaries/parse/sex.txt"));
  }

  @Override
  protected String normalize(String value) {
    if (value != null) {
      String cleaned = LETTER_MATCHER.retainFrom(value);
      cleaned = WHITESPACE_MATCHER.trimAndCollapseFrom(cleaned, ' ');
      cleaned = Strings.emptyToNull(cleaned);
      return super.normalize(cleaned);
    }
    return null;
  }

  @Override
  protected Sex fromDictFile(String value) {
    try {
      return (Sex) VocabularyUtils.lookupEnum(value, Sex.class);
    } catch (RuntimeException e) {
      return null;
    }
  }

  public static SexParser getInstance() {
    synchronized (SexParser.class) {
      if (singletonObject == null) {
        singletonObject = new SexParser();
      }
    }
    return singletonObject;
  }

}
