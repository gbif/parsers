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
package org.gbif.common.parsers.date;

import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

/**
 * Internal (package private) normalizer used to support different separators for the same pattern.
 */
class DateTimeSeparatorNormalizer {

  private final String searchChar;
  private final String replacementChar;

  public DateTimeSeparatorNormalizer(String searchChar, String replacementChar) {
    this.searchChar = searchChar;
    this.replacementChar = replacementChar;
  }

  /**
   * Replaces all characters {@link DateTimeSeparatorNormalizer#searchChar} from {@code input} with
   * {@link DateTimeSeparatorNormalizer#replacementChar}.
   * (Replaces "alternative separators" with "separator")
   *
   * @param input date string
   * @return normalized date string with one separator
   */
  public String normalize(String input) {
    return input.chars()
        .flatMap(p -> {
          if (StringUtils.contains(searchChar, p))
            return replacementChar.chars();
          return IntStream.of(p);
        })
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }
}
