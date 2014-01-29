package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Rank;
import org.gbif.common.parsers.core.EnumParser;

import java.io.InputStream;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/rank.txt.
 */
public class RankParser extends EnumParser<Rank> {

  private static RankParser singletonObject = null;

  private RankParser(InputStream... file) {
    super(Rank.class, false, file);
    // also make sure we have all enum values mapped
    for (Rank r : Rank.values()) {
      add(r.name(), r);
      add(r.getMarker(), r);
    }
  }

  public static RankParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (RankParser.class) {
      if (singletonObject == null) {
        singletonObject = new RankParser(RankParser.class.getResourceAsStream("/dictionaries/parse/rank.txt"));
      }
    }
    return singletonObject;
  }


}
