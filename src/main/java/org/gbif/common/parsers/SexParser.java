package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Sex;
import org.gbif.common.parsers.core.EnumParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/sex.txt.
 */
public class SexParser extends EnumParser<Sex> {

  private static SexParser singletonObject = null;

  private SexParser() {
    super(Sex.class, false);
    // also make sure we have all enum values mapped
    for (Sex c : Sex.values()) {
      add(c.name(), c);
    }
    // use dict file last
    init(SexParser.class.getResourceAsStream("/dictionaries/parse/sex.txt"));
  }

  public static SexParser getInstance() {
    synchronized (SexParser.class) {
      if (singletonObject == null) {
        singletonObject = new SexParser();
      }
    }
    return singletonObject;
  }
}
