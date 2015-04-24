package org.gbif.common.parsers;

import org.gbif.api.vocabulary.LifeStage;
import org.gbif.common.parsers.core.EnumParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/life_stage.txt.
 */
public class LifeStageParser extends EnumParser<LifeStage> {

  private static LifeStageParser singletonObject = null;

  private LifeStageParser() {
    super(LifeStage.class, false);
    // make sure we have all life_stage from the enum
    for (LifeStage c : LifeStage.values()) {
      add(c.name(), c);
    }
    // use dict file last
    init(LifeStageParser.class.getResourceAsStream("/dictionaries/parse/life_stage.txt"));
  }

  public static LifeStageParser getInstance() {
    synchronized (LifeStageParser.class) {
      if (singletonObject == null) {
        singletonObject = new LifeStageParser();
      }
    }
    return singletonObject;
  }

}
