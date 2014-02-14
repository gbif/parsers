package org.gbif.common.parsers;

import org.gbif.api.util.VocabularyUtils;
import org.gbif.api.vocabulary.Continent;
import org.gbif.common.parsers.core.EnumParser;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/continents.txt.
 */
public class ContinentParser extends EnumParser<Continent> {

  private static ContinentParser singletonObject = null;

  private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER.or(CharMatcher.WHITESPACE).precomputed();
  private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.WHITESPACE.precomputed();


  private ContinentParser() {
    super(Continent.class, false);
    // also make sure we have all official iso countries mapped
    for (Continent c : Continent.values()) {
      add(c.name(), c);
      add(c.getTitle(), c);
    }
    // use dict file last
    init(ContinentParser.class.getResourceAsStream("/dictionaries/parse/continents.txt"));
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
  protected Continent fromDictFile(String value) {
    try {
      return (Continent) VocabularyUtils.lookupEnum(value, Continent.class);
    } catch (RuntimeException e) {
      return null;
    }
  }

  public static ContinentParser getInstance() {
    synchronized (ContinentParser.class) {
      if (singletonObject == null) {
        singletonObject = new ContinentParser();
      }
    }
    return singletonObject;
  }

}
