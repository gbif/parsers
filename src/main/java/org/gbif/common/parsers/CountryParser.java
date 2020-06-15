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

  private static final String ISO_3166_2_FORMAT = "ISO_3166-2:";
  private static final String ISO_3166_3_FORMAT = "ISO_3166-3:";

  private CountryParser() {
    super(Country.class, false);
    // also make sure we have all official iso countries mapped
    for (Country c : Country.OFFICIAL_COUNTRIES) {
      add(c.name(), c);
      add(c.getTitle(), c);
      add(c.getIso2LetterCode(), c);
      add(c.getIso3LetterCode(), c);
      add(ISO_3166_2_FORMAT+c.getIso2LetterCode(), c);
      add(ISO_3166_3_FORMAT+c.getIso3LetterCode(), c);
    }
    // and Kosovo (which is not an official code, but should be treated as such by GBIF)
    add(Country.KOSOVO.name(), Country.KOSOVO);
    add(Country.KOSOVO.getTitle(), Country.KOSOVO);
    add(Country.KOSOVO.getIso2LetterCode(), Country.KOSOVO);
    add(Country.KOSOVO.getIso3LetterCode(), Country.KOSOVO);
    // use dict file last
    init(CountryParser.class.getResourceAsStream("/dictionaries/parse/countryName.tsv"));
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
        c = VocabularyUtils.lookupEnum(value, Country.class);
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
