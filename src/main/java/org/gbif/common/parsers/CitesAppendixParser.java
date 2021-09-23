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
