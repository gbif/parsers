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

import org.gbif.api.vocabulary.OccurrenceStatus;
import org.gbif.common.parsers.core.EnumParser;

public class OccurrenceStatusParser extends EnumParser<OccurrenceStatus> {

  private static OccurrenceStatusParser singletonObject = null;

  private OccurrenceStatusParser() {
    super(OccurrenceStatus.class, false);
    // make sure we have all occurrence_status enum mapped
    for (OccurrenceStatus c : OccurrenceStatus.values()) {
      add(c.name(), c);
    }
    // use dict file last
    init(OccurrenceStatusParser.class.getResourceAsStream("/dictionaries/parse/occurrence_status.tsv"));
  }

  public static OccurrenceStatusParser getInstance() {
    synchronized (OccurrenceStatusParser.class) {
      if (singletonObject == null) {
        singletonObject = new OccurrenceStatusParser();
      }
    }
    return singletonObject;
  }

}
