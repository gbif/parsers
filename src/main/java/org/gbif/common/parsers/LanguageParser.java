package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Language;
import org.gbif.common.parsers.core.EnumParser;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

/**
 * Singleton implementation of the case insensitive iso 639-1 language dictionary
 * that uses the org.gbif.common.api.Language enumeration.
 * Parsing results will be upper cased 2 letter codes.
 * Understood inputs are 2 or 3 letter (both terminological and bibliographical) iso codes
 * and natural language names given in any of the iso languages.
 */
public class LanguageParser extends EnumParser<Language> {

  private static LanguageParser singletonObject = null;
  private static final Pattern LOCALE = Pattern.compile("^[a-zA-Z]{2,3}_[a-zA-Z]");
  private static final List<Pattern> REMOVE_FROM_NAME_PATTERNS = ImmutableList.of(
      // remove brackets
      Pattern.compile("\\(.\\)"),
      // remove french ", langues"
      Pattern.compile(", ?langues"),
      // remove english " languages"
      Pattern.compile(" languages")
  );


  private LanguageParser() {
    super(Language.class, false, LanguageParser.class.getResourceAsStream("/dictionaries/parse/language.txt"));

    // make sure we have all enum values mapped
    for (Language r : Language.values()) {
      add(r.name(), r);
      add(r.getTitleEnglish(), r);
      add(r.getTitleNative(), r);
      add(r.getIso2LetterCode(), r);
      add(r.getIso3LetterCode(), r);
    }

    // make sure we have all enum values mapped
    for (Locale l : Locale.getAvailableLocales()) {
      Language lang = Language.fromIsoCode(l.getISO3Language());
      if (lang == null) {
        log.warn("ISO code {} not part of our language enumeration", lang);
        continue;
      }
      add(l.getISO3Language(), lang);
      add(l.getDisplayLanguage(), lang);
      add(l.getLanguage(), lang);
      for (Locale l2 : Locale.getAvailableLocales()) {
        add(l.getDisplayLanguage(l2), lang);
      }
    }

    // OFFICIAL LIST, downloaded from
    // http://www.loc.gov/standards/iso639-2/ascii_8bits.html

    //  An alpha-3 (bibliographic) code
    // an alpha-3 (terminologic) code (when given)
    // an alpha-2 code (when given)
    // an English name
    // a French name
    try {
      CSVReader r = CSVReaderFactory.build(LanguageParser.class.getResourceAsStream("/dictionaries/parse/ISO-639-2_utf-8.txt"), "UTF8", "|", null, 0);
      while(r.hasNext()) {
        String[] row = r.next();
        if (row.length>2) {
          // iso 2 letter code
          String alpha2 = row[2];
          if (!StringUtils.isBlank(alpha2)) {
            Language lang = Language.fromIsoCode(alpha2);
            if (lang == null || lang == Language.UNKNOWN) {
              log.warn("ISO code {} not part of our language enumeration", alpha2);
              continue;
            }
            // alpha-3 (bibliographic)
            add(row[0], lang);
            // alpha-3 (terminologic)
            add(row[1], lang);
            // english
            for (String l : mutateLanguageName(row[3])) {
              add(l, lang);
            }
            // french
            for (String l : mutateLanguageName(row[4])) {
              add(l, lang);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Cannot initiate language parser: {}", e.getMessage());
      throw new IllegalStateException("Cannot initiate language parser", e);
    }


    // ISO 639-3 list from SIL:
    // http://www-01.sil.org/iso639-3/download.asp
    try {
      // Id	Part2B	Part2T	Part1	Scope	Language_Type	Ref_Name	Comment
      CSVReader r = CSVReaderFactory.buildTabReader(LanguageParser.class.getResourceAsStream("/dictionaries/parse/iso-639-3-sil.tab"), "UTF8", 1);
      while(r.hasNext()) {
        String[] row = r.next();
        if (row.length>2) {
          String alpha2 = row[3];
          if (!StringUtils.isBlank(alpha2)) {
            Language lang = Language.fromIsoCode(alpha2);
            if (lang == null || lang == Language.UNKNOWN) {
              log.warn("ISO code {} not part of our language enumeration", alpha2);
              continue;
            }
            // 3-letter code
            add(row[0], lang);
            // 3-letter code part2B
            add(row[1], lang);
            // 3-letter code part2T
            add(row[2], lang);
            // name
            add(row[6], lang);
          }
        }
      }
    } catch (Exception e) {
      log.error("Cannot initiate language parser: {}", e.getMessage());
      throw new IllegalStateException("Cannot initiate language parser", e);
    }
  }

  private Set<String> mutateLanguageName(String lang) {
    Set<String> langs = Sets.newHashSet();
    for (String l : lang.split(";")) {
      langs.add(l);
      // also remove common patterns
      for (Pattern p : REMOVE_FROM_NAME_PATTERNS) {
        langs.add(p.matcher(l).replaceAll(""));
      }
    }
    return langs;
  }

  @Override
  protected String normalize(String value) {
    if (value != null) {
      /**
       * A language string could come in as a locale like "en_US" or if it was constructed improperly "eng_US", so
       * extract only the part before the underscore". Only if it contains an "_" is parsing attempted.
       * Whether it actually represents an iso 369 language code is left for the language parser to determine.
       */
      if (LOCALE.matcher(value).find()) {
        int index = value.indexOf("_");
        // only allow underscore
        if (index > 1 && index < 4 ) {
          return super.normalize(value.substring(0, index));
        }
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
