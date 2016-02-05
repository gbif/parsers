package org.gbif.common.parsers.core;

import com.google.common.collect.Sets;
import org.gbif.api.util.VocabularyUtils;

import java.io.InputStream;
import java.util.HashSet;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

/**
 * Generic parser for enumerations making use of our vocabulary util to lookup an enum value from a string.
 * Also adds stronger normalization removing any non-ASCII-alphanumeric characters. It is still ok to use hyphens or
 * underscores in the enumeration values.
 */
public class EnumParser<T extends Enum<T>> extends FileBasedDictionaryParser<T> {

  private final Class<T> clazz;
  private final Pattern NORMALIZER;
  private final ASCIIParser ascii = ASCIIParser.getInstance();

  // These become null, as after removing non-letters "N/A" might mean something like "Namibia".
  private final HashSet<String> notAvailable = Sets.newHashSet(
          "N/A", "N/a", "n/a", "n/A", "n.a.", // Not available
          "N/K", "N/k", "n/k", "n/K", "n.k.", // Not known
          "UNK.", "Unk.", "unk.", "UNK", "Unk", "unk" // Unknown
  );

  protected EnumParser(Class<T> clazz, boolean allowDigits, final InputStream... inputs) {
    super(false);

    if (allowDigits) {
      NORMALIZER = Pattern.compile("\\W+");
    } else {
      NORMALIZER = Pattern.compile("[^a-zA-Z]+");
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
    if (Strings.isNullOrEmpty(handleNotAvailable(value))) return null;

    // convert to ascii
    ParseResult<String> asci = ascii.parse(value);
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
