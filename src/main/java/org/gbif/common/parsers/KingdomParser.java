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

import org.gbif.api.vocabulary.Kingdom;
import org.gbif.common.parsers.core.EnumParser;

public class KingdomParser extends EnumParser<Kingdom> {
    private static KingdomParser singletonObject = null;

    private KingdomParser() {
        super(Kingdom.class, false);
        // make sure we have all continents from the enum
        for (Kingdom k : Kingdom.values()) {
            add(k.name(), k);
        }
        // use dict file last
        init(KingdomParser.class.getResourceAsStream("/dictionaries/parse/kingdoms.tsv"));
    }

    public static KingdomParser getInstance() {
        synchronized (KingdomParser.class) {
            if (singletonObject == null) {
                singletonObject = new KingdomParser();
            }
        }
        return singletonObject;
    }
}
