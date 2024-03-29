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

import org.gbif.api.vocabulary.EstablishmentMeans;
import org.gbif.common.parsers.core.EnumParser;

/**
 * @deprecated replaced by the vocabulary server <a href="https://github.com/gbif/vocabulary">https://github.com/gbif/vocabulary</a>.
 */
@Deprecated
public class EstablishmentMeansParser extends EnumParser<EstablishmentMeans> {

  private static EstablishmentMeansParser singletonObject = null;

  private EstablishmentMeansParser() {
    super(EstablishmentMeans.class, false);
    // also make sure we have all official iso countries mapped
    for (EstablishmentMeans c : EstablishmentMeans.values()) {
      add(c.name(), c);
    }
    // use dict file last
    init(EstablishmentMeansParser.class.getResourceAsStream("/dictionaries/parse/establishment_means.tsv"));
  }

  public static EstablishmentMeansParser getInstance() {
    synchronized (EstablishmentMeansParser.class) {
      if (singletonObject == null) {
        singletonObject = new EstablishmentMeansParser();
      }
    }
    return singletonObject;
  }

}
