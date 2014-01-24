package org.gbif.common.parsers.countryname;

import org.gbif.api.vocabulary.Country;
import org.gbif.common.parsers.FileBasedDictionaryParser;

import java.io.InputStream;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/countryName.txt.
 */
public class CountryNameParser extends FileBasedDictionaryParser {

  private static CountryNameParser singletonObject = null;

  private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER.or(CharMatcher.WHITESPACE).precomputed();
  private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.WHITESPACE.precomputed();
  // "off Australia"
  private static final Pattern REMOVE_OFF_PATTERN = Pattern.compile("off ", Pattern.CASE_INSENSITIVE);


  private CountryNameParser(boolean caseSensitive, InputStream... file) {
    super(caseSensitive, file);
    // also make sure we have all official iso countries mapped
    for (Country c : Country.OFFICIAL_COUNTRIES) {
      add(c.name(), c.getIso2LetterCode());
      add(c.getTitle(), c.getIso2LetterCode());
      add(c.getIso2LetterCode(), c.getIso2LetterCode());
      add(c.getIso3LetterCode(), c.getIso2LetterCode());
    }
  }

  @Override
  protected String normalize(String value) {
    if (value != null) {
      String cleanedCountry = LETTER_MATCHER.retainFrom(value);
      cleanedCountry = REMOVE_OFF_PATTERN.matcher(cleanedCountry).replaceFirst("");
      cleanedCountry = WHITESPACE_MATCHER.trimAndCollapseFrom(cleanedCountry, ' ');
      cleanedCountry = Strings.emptyToNull(cleanedCountry);
      return super.normalize(cleanedCountry);
    }
    return null;
  }

  public static CountryNameParser getInstance() {
    synchronized (CountryNameParser.class) {
      if (singletonObject == null) {
        singletonObject = new CountryNameParser(false,
          CountryNameParser.class.getResourceAsStream("/dictionaries/parse/countryName.txt"));
      }
    }
    return singletonObject;
  }

}
