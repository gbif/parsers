package org.gbif.common.parsers.core;

import org.gbif.api.util.VocabularyUtils;

import java.io.InputStream;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

/**
 * Generic parser for enumerations making use of our vocabulary util to lookup an enum value from a string.
 * Also adds stronger normalization removing any non alphanumeric characters. It is still ok to use hyphens or
 * underscores in the enumeration values.
 */
public class EnumParser<T extends Enum<T>> extends FileBasedDictionaryParser<T> {

  private final Class<T> clazz;
  private final Pattern NORMALIZER;
  private final ASCIIParser ascii = ASCIIParser.getInstance();

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
    if (Strings.isNullOrEmpty(value)) return null;
    // convert to ascii
    ParseResult<String> asci = ascii.parse(value);
    return NORMALIZER.matcher(asci.getPayload()).replaceAll("").toUpperCase();
  }

  @Override
  protected T fromDictFile(String value) {
    try {
      return (T) VocabularyUtils.lookupEnum(value, clazz);

    } catch (RuntimeException e) {

    }
    return null;
  }
}
