package org.gbif.common.parsers.date;

import org.apache.commons.lang3.StringUtils;

import java.util.stream.IntStream;

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
