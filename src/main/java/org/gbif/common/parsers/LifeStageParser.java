package org.gbif.common.parsers;

import org.gbif.api.util.VocabularyUtils;
import org.gbif.api.vocabulary.LifeStage;
import org.gbif.common.parsers.core.EnumParser;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/continents.txt.
 */
public class LifeStageParser extends EnumParser<LifeStage> {

  private static LifeStageParser singletonObject = null;

  private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER.or(CharMatcher.WHITESPACE).precomputed();
  private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.WHITESPACE.precomputed();


  private LifeStageParser() {
    super(LifeStage.class, false);
    // also make sure we have all official iso countries mapped
    for (LifeStage c : LifeStage.values()) {
      add(c.name(), c);
    }
    // use dict file last
    init(LifeStageParser.class.getResourceAsStream("/dictionaries/parse/life_stage.txt"));
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
  protected LifeStage fromDictFile(String value) {
    try {
      return (LifeStage) VocabularyUtils.lookupEnum(value, LifeStage.class);
    } catch (RuntimeException e) {
      return null;
    }
  }

  public static LifeStageParser getInstance() {
    synchronized (LifeStageParser.class) {
      if (singletonObject == null) {
        singletonObject = new LifeStageParser();
      }
    }
    return singletonObject;
  }

}
