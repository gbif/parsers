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

import org.gbif.api.vocabulary.Rank;
import org.gbif.common.parsers.core.EnumParser;

import java.io.InputStream;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/rank.txt.
 */
public class RankParser extends EnumParser<Rank> {

  private static RankParser singletonObject = null;

  private RankParser(InputStream... file) {
    super(Rank.class, false, file);
    // also make sure we have all enum values mapped
    for (Rank r : Rank.values()) {
      add(r.name(), r);
      add(r.getMarker(), r);
    }
  }

  public static RankParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (RankParser.class) {
      if (singletonObject == null) {
        singletonObject = new RankParser(RankParser.class.getResourceAsStream("/dictionaries/parse/rank.tsv"));
      }
    }
    return singletonObject;
  }


}
