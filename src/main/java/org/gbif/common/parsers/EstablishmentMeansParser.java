package org.gbif.common.parsers;

import org.gbif.api.util.VocabularyUtils;
import org.gbif.api.vocabulary.EstablishmentMeans;
import org.gbif.common.parsers.core.EnumParser;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

public class EstablishmentMeansParser extends EnumParser<EstablishmentMeans> {

  private static EstablishmentMeansParser singletonObject = null;

  private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER.or(CharMatcher.WHITESPACE).precomputed();
  private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.WHITESPACE.precomputed();


  private EstablishmentMeansParser() {
    super(EstablishmentMeans.class, false);
    // also make sure we have all official iso countries mapped
    for (EstablishmentMeans c : EstablishmentMeans.values()) {
      add(c.name(), c);
    }
    // use dict file last
    init(EstablishmentMeansParser.class.getResourceAsStream("/dictionaries/parse/establishment_means.txt"));
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
  protected EstablishmentMeans fromDictFile(String value) {
    try {
      return (EstablishmentMeans) VocabularyUtils.lookupEnum(value, EstablishmentMeans.class);
    } catch (RuntimeException e) {
      return null;
    }
  }

  public static EstablishmentMeansParser getInstance() {
    synchronized (EstablishmentMeansParser.class) {
      if (singletonObject == null) {
        singletonObject = new EstablishmentMeansParser();
      }
    }
    return singletonObject;
  }

}
