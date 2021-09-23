/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    init(LifeStageParser.class.getResourceAsStream("/dictionaries/parse/life_stage.tsv"));
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
