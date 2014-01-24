package org.gbif.common.parsers.rank;

import org.gbif.api.vocabulary.Rank;
import org.gbif.common.parsers.FileBasedDictionaryParser;

import java.io.InputStream;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/rank.txt.
 */
public class RankParser extends FileBasedDictionaryParser {

  private static RankParser singletonObject = null;

  private RankParser(boolean caseSensitive, InputStream... file) {
    super(caseSensitive, file);
    // also make sure we have all enum values mapped
    for (Rank r : Rank.values()) {
      add(r.name(), r.name());
      add(r.getMarker(), r.name());
    }
  }

  public static RankParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (RankParser.class) {
      if (singletonObject == null) {
        singletonObject = new RankParser(false, RankParser.class.getResourceAsStream("/dictionaries/parse/rank.txt"));
      }
    }
    return singletonObject;
  }


}
