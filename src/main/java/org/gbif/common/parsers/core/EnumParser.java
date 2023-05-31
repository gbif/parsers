/*
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
package org.gbif.common.parsers.core;

import org.gbif.api.util.VocabularyUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Generic parser for enumerations making use of our vocabulary util to lookup an enum value from a string.
 * Also adds stronger normalization removing any non-ASCII-alphanumeric characters. It is still ok to use hyphens or
 * underscores in the enumeration values.
 */
public class EnumParser<T extends Enum<T>> extends FileBasedDictionaryParser<T> {

  private final Class<T> clazz;
  private final Pattern NORMALIZER;
  protected final ASCIIParser asciiParser = ASCIIParser.getInstance();

  // These become null, as after removing non-letters "N/A" might mean something like "Namibia".
  private final HashSet<String> notAvailable = new HashSet<>(
      Arrays.asList(
          "N/A", "N/a", "n/a", "n/A", "n.a.", // Not available
          "N/K", "N/k", "n/k", "n/K", "n.k.", // Not known
          "UNK.", "Unk.", "unk.", "UNK", "Unk", "unk", // Unknown
          "No data", "Not provided"
      ));

  protected EnumParser(Class<T> clazz, boolean allowDigits, final InputStream... inputs) {
    super(false);

    if (allowDigits) {
      NORMALIZER = Pattern.compile("[^\\p{IsAlphabetic}♀♂\\p{N}]+");
    } else {
      NORMALIZER = Pattern.compile("[^\\p{IsAlphabetic}♀♂]+");
    }
    this.clazz = clazz;
    // init dicts
    addEnumValues();

    if (inputs != null) {
      for (InputStream input : inputs) {
        init(input);
      }
    }
  }

  private void addEnumValues() {
    T[] values = clazz.getEnumConstants();
    if (values != null) {
      for (T val : values) {
        add(val.name(), val);
      }
    }
  }

  @Override
  protected String normalize(String value) {
    if (StringUtils.isEmpty(handleNotAvailable(value))) return null;

    // convert to ascii
    ParseResult<String> asci = asciiParser.parse(value);
    return NORMALIZER.matcher(asci.getPayload()).replaceAll("").toUpperCase();
  }

  /**
   * Treat "n/a" etc as null.
   * A separate method so it can be called before stripping slash characters etc.
   */
  protected String handleNotAvailable(String value) {
    return notAvailable.contains(value) ? null : value;
  }

  @Override
  protected T fromDictFile(String value) {
    try {
      return (T) VocabularyUtils.lookupEnum(value, clazz);
    } catch (RuntimeException e) {
      return null;
    }
  }

}
