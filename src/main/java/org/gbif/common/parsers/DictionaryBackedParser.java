package org.gbif.common.parsers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * A simple parser that will initialise with source data, and will
 * use that as a lookup to replace values. This was written with
 * basis of record lookup and country names in mind.
 * Future improvements to this implementation might call a
 * dictionary web service for example, to achieve the same,
 * but allow the abstraction of the dictionary management to a
 * better project (separation of concerns)
 */
public class DictionaryBackedParser<K, V> implements Parsable<K, V> {

  private final Map<K, V> dictionary = new HashMap<K, V>();
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
  public void init(Iterator<KeyValue<K, V>> source) {
    while (source.hasNext()) {
      KeyValue<K, V> kvp = source.next();
      add(kvp.getKey(), kvp.getValue());
    }
  }

  protected void add(K key, V value) {
    if (key != null) {
      dictionary.put(normalize(key), value);
    }
  }

  /**
   * @param key   the key to check
   * @param value the value to compare with
   *
   * @return true if the given key is already mapped to a value different from the input
   */
  protected boolean isMappedToOtherValue(K key, V value) {
    K normedKey = normalize(key);
    V existingValue = dictionary.get(normedKey);
    if (existingValue != null && !existingValue.equals(value)) {
      return true;
    }
    return false;
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
  protected K normalize(K value) {
    if (value != null) {
      if (!caseSensitive && value instanceof String) {
        return (K) StringUtils.trimToNull(((String) value).toUpperCase());
      }
    }
    return value;
  }

  /**
   * Tries to parse the input data according to its backing dictionary.
   * If no entry in the dictionary can be found the result will be {@link ParseResult.STATUS FAIL} otherwise the
   * result will be a {@link org.gbif.common.parsers.ParseResult.CONFIDENCE DEFINITE} {@link ParseResult.STATUS
   * SUCCESS}.
   *
   * @param input To lookup in the dictionary
   *
   * @return the replacement from the dictionary
   */
  @Override
  public ParseResult<V> parse(K input) {
    K normed = normalize(input);
    V value = dictionary.get(normed);
    if (value == null) {
      return ParseResult.fail();
    } else {
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, value);
    }
  }
}
