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

import org.gbif.api.vocabulary.Habitat;
import org.gbif.common.parsers.core.EnumParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/habitat.txt.
 */
public class HabitatParser extends EnumParser<Habitat> {

  private static HabitatParser singletonObject = null;

  private HabitatParser() {
    super(Habitat.class, false, HabitatParser.class.getResourceAsStream("/dictionaries/parse/habitat.tsv"));
  }

  public static HabitatParser getInstance() {
    synchronized (HabitatParser.class) {
      if (singletonObject == null) {
        singletonObject = new HabitatParser();
      }
    }
    return singletonObject;
  }

  /**
   * Strip of any s at the end often found in english plurals
   */
  @Override
  protected String normalize(String value) {
    String x = super.normalize(value);
    if (x != null && x.length() > 1) {
      if (x.endsWith("S")) {
        return x.substring(0, x.length()-1);
      }
    }
    return x;
  }
}
