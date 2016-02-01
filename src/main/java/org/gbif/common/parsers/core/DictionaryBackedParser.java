package org.gbif.common.parsers.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple parser that will initialise with source data, and will
 * use that as a lookup to replace values. This was written with
 * basis of record lookup and country names in mind.
 * Future improvements to this implementation might call a
 * dictionary web service for example, to achieve the same,
 * but allow the abstraction of the dictionary management to a
 * better project (separation of concerns)
 */
public class DictionaryBackedParser<V> implements Parsable<V> {
  protected final Logger log = LoggerFactory.getLogger(getClass());
  private final Map<String, V> dictionary = new HashMap<String, V>();
  private boolean caseSensitive = false;

  /**
   * @param caseSensitive If the dictionary should be case sensitive (only applicable to String keys)
   */
  public DictionaryBackedParser(boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }

  /**
   * @param source To build the dictionary from
   */
  @SuppressWarnings("unchecked")
  public void init(Iterator<KeyValue<String, V>> source) {
    while (source.hasNext()) {
      KeyValue<String, V> kvp = source.next();
      add(kvp.getKey(), kvp.getValue());
    }
  }

  final protected void add(String key, V value) {
    Preconditions.checkNotNull("Adding a null value to the parser dictionary is not allowed", value);
    String normedKey = normalize(key);
    if (!Strings.isNullOrEmpty(normedKey)) {
      V existingValue = dictionary.get(normedKey);
      if (existingValue != null && !existingValue.equals(value)) {
        log.warn("Ignoring mapping {}→{} as {} is already mapped to {}", key, value, key, existingValue);
      } else {
        dictionary.put(normedKey, value);
      }
    }
  }

  /**
   * Normalisation of a value used both by adding to the internal dictionary and parsing values.
   * The default does trim and uppercase the value for Strings, but leaves other types unaltered.
   * Override this method to provide specific normalisations for parsers.
   *
   * @param value the value to be normalised.
   *
   * @return the normalised value
   */
  protected String normalize(String value) {
    if (value != null) {
      if (!caseSensitive) {
        return StringUtils.trimToNull(value.toUpperCase());
      }
    }
    return value;
  }

  /**
   * Tries to parse the input data according to its backing dictionary.
   * If no entry in the dictionary can be found the result will be {@link ParseResult.STATUS FAIL} otherwise the
   * result will be a {@link org.gbif.common.parsers.core.ParseResult.CONFIDENCE DEFINITE} {@link ParseResult.STATUS
   * SUCCESS}.
   *
   * @param input To lookup in the dictionary
   *
   * @return the replacement from the dictionary
   */
  @Override
  public ParseResult<V> parse(String input) {
    String normed = normalize(input);
    V value = dictionary.get(normed);
    if (value == null) {
      return ParseResult.fail();
    } else {
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, value);
    }
  }
}
