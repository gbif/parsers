package org.gbif.common.parsers;

import org.gbif.api.util.VocabularyUtils;
import org.gbif.api.vocabulary.Sex;
import org.gbif.common.parsers.core.EnumParser;
import org.gbif.common.parsers.core.FileBasedDictionaryParser;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.sun.org.apache.xpath.internal.operations.Bool;

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
