package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Language;
import org.gbif.common.parsers.core.EnumParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;

/**
 * Singleton implementation of the case insensitive iso 639-1 language dictionary
 * that uses the org.gbif.common.api.Language enumeration.
 * Parsing results will be upper cased 2 letter codes.
 * Understood inputs are 2 or 3 letter (both terminological and bibliographical) iso codes
 * and natural language names given in any of the iso languages.
 */
public class LanguageParser extends EnumParser<Language> {

  private static LanguageParser singletonObject = null;
  private static final Splitter TAB_SPLITTER = Splitter.on('\t').trimResults();
  private static final Splitter SEMICOLON_SPLITTER = Splitter.on(';').trimResults();


  private LanguageParser() {
    super(Language.class, false);
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
          Language lang = Language.fromIsoCode(iso);
          if (lang == null) {
            log.warn("ISO code {} not part of our language enumeration", iso);
            continue;
          }
          // ignore iso 3 letter code - handled by enum already
          cols.next();
          // ignore english name - handled by enum already
          cols.next();
          // ignore native name - handled by enum already
          cols.next();
          // make use of all translations!
          for (String s : SEMICOLON_SPLITTER.split(cols.next())) {
            add(s, lang);
          }
        }
      } while (line != null);
    } catch (IOException e) {
      log.error("Cannot initiate language parser: {}", e.getMessage());
      throw new IllegalStateException("Cannot initiate language parser", e);
    }

    // also make sure we have all enum values mapped
    for (Language r : Language.values()) {
      add(r.name(), r);
      add(r.getTitleEnglish(), r);
      add(r.getTitleNative(), r);
      add(r.getIso2LetterCode(), r);
      add(r.getIso3LetterCode(), r);
    }
  }

  @Override
  protected String normalize(String value) {
    if (value != null) {
      /**
       * A language string could come in as a locale like "en_US" or if it was constructed improperly "eng_US", so
       * extract only the part before the underscore". Only if it contains an "_" is parsing attempted.
       * Whether it actually represents an iso 369 language code is left for the language parser to determine.
       */
      if (value.contains("_")) {
        int index = value.indexOf("_");
        return super.normalize(value.substring(0, index));
      }
      return super.normalize(value);
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
