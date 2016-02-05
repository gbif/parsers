package org.gbif.common.parsers;

import org.gbif.api.util.VocabularyUtils;
import org.gbif.api.vocabulary.Country;
import org.gbif.common.parsers.core.EnumParser;

import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/countryName.txt.
 */
public class CountryParser extends EnumParser<Country> {

  private static CountryParser singletonObject = null;

  private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER.or(CharMatcher.WHITESPACE).precomputed();
  private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.WHITESPACE.precomputed();
  // "off Australia"
  private static final Pattern REMOVE_OFF_PATTERN = Pattern.compile("off ", Pattern.CASE_INSENSITIVE);


  private CountryParser() {
    super(Country.class, false);
    // also make sure we have all official iso countries mapped
    for (Country c : Country.OFFICIAL_COUNTRIES) {
      add(c.name(), c);
      add(c.getTitle(), c);
      add(c.getIso2LetterCode(), c);
      add(c.getIso3LetterCode(), c);
    }
    // use dict file last
    init(CountryParser.class.getResourceAsStream("/dictionaries/parse/countryName.txt"));
  }

  @Override
  protected String normalize(String value) {
    value = handleNotAvailable(value);
    if (value != null) {
      String cleanedCountry = LETTER_MATCHER.retainFrom(value);
      cleanedCountry = REMOVE_OFF_PATTERN.matcher(cleanedCountry).replaceFirst("");
      cleanedCountry = WHITESPACE_MATCHER.trimAndCollapseFrom(cleanedCountry, ' ');
      cleanedCountry = Strings.emptyToNull(cleanedCountry);
      return super.normalize(cleanedCountry);
    }
    return null;
  }

  @Override
  protected Country fromDictFile(String value) {
    Country c = Country.fromIsoCode(value);
    if (c == null) {
      try {
        c = (Country) VocabularyUtils.lookupEnum(value, Country.class);
      } catch (RuntimeException e) {
      }
    }
    return c;
  }

  public static CountryParser getInstance() {
    synchronized (CountryParser.class) {
      if (singletonObject == null) {
        singletonObject = new CountryParser();
      }
    }
    return singletonObject;
  }

}
