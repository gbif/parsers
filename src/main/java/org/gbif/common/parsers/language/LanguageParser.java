package org.gbif.common.parsers.language;

import org.gbif.api.vocabulary.Language;
import org.gbif.common.parsers.DictionaryBackedParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton implementation of the case insensitive iso 639-1 language dictionary
 * that uses the org.gbif.common.api.Language enumeration.
 * Parsing results will be upper cased 2 letter codes.
 * Understood inputs are 2 or 3 letter (both terminological and bibliographical) iso codes
 * and natural language names given in any of the iso languages.
 */
public class LanguageParser extends DictionaryBackedParser<String, String> {

  private static final Logger LOG = LoggerFactory.getLogger(LanguageParser.class);
  private static LanguageParser singletonObject = null;
  private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER.or(CharMatcher.WHITESPACE).precomputed();
  private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.WHITESPACE.precomputed();
  private static final Splitter TAB_SPLITTER = Splitter.on('\t').trimResults();
  private static final Splitter SEMICOLON_SPLITTER = Splitter.on(';').trimResults();


  private LanguageParser() {
    super(false);
    try {
      BufferedReader r = new BufferedReader(
        new InputStreamReader(LanguageParser.class.getResourceAsStream("/dictionaries/parse/iso-639-1.txt"),
          Charsets.UTF_8));
      String line;
      do {
        line = r.readLine();
        if (line != null) {
          Iterator<String> cols = TAB_SPLITTER.split(line).iterator();
          // iso 2 letter code
          String iso = cols.next().toLowerCase();
          // ignore iso 3 letter code - handled by enum already
          cols.next();
          // ignore english name - handled by enum already
          cols.next();
          // ignore native name - handled by enum already
          cols.next();
          // make use of all translations!
          for (String s : SEMICOLON_SPLITTER.split(cols.next())) {
            add(s, iso);
          }
        }
      } while (line != null);
    } catch (IOException e) {
      LOG.error("Cannot initiate language parser: {}", e.getMessage());
      throw new IllegalStateException("Cannot initiate language parser", e);
    }

    // also make sure we have all enum values mapped
    for (Language r : Language.values()) {
      add(r.name(), r.getIso2LetterCode());
      add(r.getTitleEnglish(), r.getIso2LetterCode());
      add(r.getTitleNative(), r.getIso2LetterCode());
      add(r.getIso2LetterCode(), r.getIso2LetterCode());
      add(r.getIso3LetterCode(), r.getIso2LetterCode());
    }
  }

  /**
   * A language string could come in as a locale like "en_US" or if it was constructed improperly "eng_US", so
   * extract only the part before the underscore". Only if it contains an "_" is parsing attempted.
   * Whether it actually represents an iso 369 language code is left for the language parser to determine.
   *
   * @param input language string (possibly representing a Locale)
   *
   * @return parsed string if it contained an underscore, unchanged string if it didn't, or null if it was empty or
   *         null
   *         to begin with
   */
  private String extractLanguageFromLocale(String input) {
    if (!Strings.isNullOrEmpty(input) && input.contains("_")) {
      int index = input.indexOf("_");
      return input.substring(0, index);
    }
    return input;
  }

  @Override
  protected void add(String key, String value) {
    if (isMappedToOtherValue(key, value)) {
      LOG.warn("Ignore value {} mapped to more than one language", key);
    }
    super.add(key, value);
  }

  @Override
  protected String normalize(String value) {
    if (value != null) {
      String cleaned = extractLanguageFromLocale(value);
      cleaned = LETTER_MATCHER.retainFrom(cleaned);
      cleaned = WHITESPACE_MATCHER.trimAndCollapseFrom(cleaned, ' ');
      cleaned = Strings.emptyToNull(cleaned);
      return super.normalize(cleaned);
    }
    return null;
  }

  public static LanguageParser getInstance() {
    synchronized (LanguageParser.class) {
      if (singletonObject == null) {
        singletonObject = new LanguageParser();
      }
    }
    return singletonObject;
  }
}
