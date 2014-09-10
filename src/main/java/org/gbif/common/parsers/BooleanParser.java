package org.gbif.common.parsers;

import org.gbif.common.parsers.core.FileBasedDictionaryParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/continents.txt.
 */
public class BooleanParser extends FileBasedDictionaryParser<Boolean> {

  private static BooleanParser singletonObject = null;

  private BooleanParser() {
    super(false);
    // use dict file last
    init(BooleanParser.class.getResourceAsStream("/dictionaries/parse/boolean.txt"));
  }

  public static BooleanParser getInstance() {
    synchronized (BooleanParser.class) {
      if (singletonObject == null) {
        singletonObject = new BooleanParser();
      }
    }
    return singletonObject;
  }

  @Override
  protected Boolean fromDictFile(String value) {
    return Boolean.parseBoolean(value);
  }
}
