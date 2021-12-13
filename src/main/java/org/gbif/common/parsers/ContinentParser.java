/*
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

import org.gbif.api.vocabulary.Continent;
import org.gbif.common.parsers.core.EnumParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/continents.txt.
 */
public class ContinentParser extends EnumParser<Continent> {

  private static ContinentParser singletonObject = null;

  private ContinentParser() {
    super(Continent.class, false);
    // make sure we have all continents from the enum
    for (Continent c : Continent.values()) {
      add(c.name(), c);
      add(c.getTitle(), c);
    }
    // use dict file last
    init(ContinentParser.class.getResourceAsStream("/dictionaries/parse/continents.tsv"));
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
