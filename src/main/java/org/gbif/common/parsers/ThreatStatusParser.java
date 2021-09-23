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

import org.gbif.api.vocabulary.ThreatStatus;
import org.gbif.common.parsers.core.EnumParser;

public class ThreatStatusParser extends EnumParser<ThreatStatus> {

  private static ThreatStatusParser singletonObject = null;

  private ThreatStatusParser() {
    super(ThreatStatus.class, false);
    // also make sure we have all enum values mapped
    for (ThreatStatus c : ThreatStatus.values()) {
      add(c.name(), c);
      add(c.getCode(), c);
    }
    // use dict file last
    init(ThreatStatusParser.class.getResourceAsStream("/dictionaries/parse/threat_status.tsv"));
  }

  public static ThreatStatusParser getInstance() {
    synchronized (ThreatStatusParser.class) {
      if (singletonObject == null) {
        singletonObject = new ThreatStatusParser();
      }
    }
    return singletonObject;
  }

}
