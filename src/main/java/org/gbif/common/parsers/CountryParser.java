/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.common.parsers;

import org.gbif.api.util.VocabularyUtils;
import org.gbif.api.vocabulary.Country;
import org.gbif.common.parsers.core.EnumParser;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/countryName.txt.
 */
public class CountryParser extends EnumParser<Country> {

  private static CountryParser singletonObject = null;

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
      // step 1: remove all non-letter and not-whitespace characters
      String cleanedCountry = value.chars()
          .filter(p -> Character.isLetter((char) p) || Character.isWhitespace(p))
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();
      // step 2: remove 'off '
      cleanedCountry = REMOVE_OFF_PATTERN.matcher(cleanedCountry).replaceFirst("");
      // step 3: normalize whitespaces
      cleanedCountry = StringUtils.normalizeSpace(cleanedCountry);
      // step 4: trim to null
      cleanedCountry = StringUtils.trimToNull(cleanedCountry);
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
