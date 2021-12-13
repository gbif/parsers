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

import org.gbif.api.vocabulary.MaintenanceUpdateFrequency;
import org.gbif.common.parsers.core.EnumParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/maintenanceUpdateFrequency.txt.
 */
public class MaintenanceUpdateFrequencyParser extends EnumParser<MaintenanceUpdateFrequency> {

  private static MaintenanceUpdateFrequencyParser singletonObject = null;

  private MaintenanceUpdateFrequencyParser() {
    super(MaintenanceUpdateFrequency.class, false);
    // make sure we have all values from the enum
    for (MaintenanceUpdateFrequency m : MaintenanceUpdateFrequency.values()) {
      add(m.name(), m);
    }
    // use dict file last
    init(
      MaintenanceUpdateFrequencyParser.class.getResourceAsStream("/dictionaries/parse/maintenanceUpdateFrequency.tsv"));
  }

  public static MaintenanceUpdateFrequencyParser getInstance() {
    synchronized (MaintenanceUpdateFrequencyParser.class) {
      if (singletonObject == null) {
        singletonObject = new MaintenanceUpdateFrequencyParser();
      }
    }
    return singletonObject;
  }
}
