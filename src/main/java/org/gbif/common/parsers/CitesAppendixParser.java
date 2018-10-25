package org.gbif.common.parsers;

import org.gbif.api.vocabulary.CitesAppendix;
import org.gbif.common.parsers.core.EnumParser;

public class CitesAppendixParser extends EnumParser<CitesAppendix> {

  private static CitesAppendixParser singletonObject = null;

  private CitesAppendixParser() {
    super(CitesAppendix.class, true);
    // also make sure we have all official iso countries mapped
    for (CitesAppendix c : CitesAppendix.values()) {
      add(c.name(), c);
    }
    // use dict file last
    init(CitesAppendixParser.class.getResourceAsStream("/dictionaries/parse/cites.tsv"));
  }

  public static CitesAppendixParser getInstance() {
    synchronized (CitesAppendixParser.class) {
      if (singletonObject == null) {
        singletonObject = new CitesAppendixParser();
      }
    }
    return singletonObject;
  }

}
