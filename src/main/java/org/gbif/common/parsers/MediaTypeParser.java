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

import org.gbif.api.vocabulary.MediaType;
import org.gbif.common.parsers.core.EnumParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/media_type.txt.
 */
public class MediaTypeParser extends EnumParser<MediaType> {

  private static MediaTypeParser singletonObject = null;

  private MediaTypeParser() {
    super(MediaType.class, false);
    // make sure we have media_type from the enum
    for (MediaType c : MediaType.values()) {
      add(c.name(), c);
    }
    for (MediaType c : MediaType.values()) {
    }
    // use dict file last
    init(MediaTypeParser.class.getResourceAsStream("/dictionaries/parse/media_type.tsv"));
  }

  public static MediaTypeParser getInstance() {
    synchronized (MediaTypeParser.class) {
      if (singletonObject == null) {
        singletonObject = new MediaTypeParser();
      }
    }
    return singletonObject;
  }

}
