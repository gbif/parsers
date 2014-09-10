package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Continent;
import org.gbif.common.parsers.core.EnumParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/continents.txt.
 */
public class ContinentParser extends EnumParser<Continent> {

  private static ContinentParser singletonObject = null;

  private ContinentParser() {
    super(Continent.class, false);
    // also make sure we have all official iso countries mapped
    for (Continent c : Continent.values()) {
      add(c.name(), c);
      add(c.getTitle(), c);
    }
    // use dict file last
    init(ContinentParser.class.getResourceAsStream("/dictionaries/parse/continents.txt"));
  }

  public static ContinentParser getInstance() {
    synchronized (ContinentParser.class) {
      if (singletonObject == null) {
        singletonObject = new ContinentParser();
      }
    }
    return singletonObject;
  }

}
