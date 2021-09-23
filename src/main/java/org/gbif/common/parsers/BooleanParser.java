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

import org.gbif.common.parsers.core.FileBasedDictionaryParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/boolean.txt.
 */
public class BooleanParser extends FileBasedDictionaryParser<Boolean> {

  private static BooleanParser singletonObject = null;

  private BooleanParser() {
    super(false);
    // use dict file last
    init(BooleanParser.class.getResourceAsStream("/dictionaries/parse/boolean.tsv"));
  }

  public static BooleanParser getInstance() {
    synchronized (BooleanParser.class) {
      if (singletonObject == null) {
        singletonObject = new BooleanParser();
      }
    }
    return singletonObject;
  }

  @Override
  protected Boolean fromDictFile(String value) {
    return Boolean.parseBoolean(value);
  }
}
