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

import org.gbif.api.vocabulary.NomenclaturalCode;
import org.gbif.common.parsers.core.EnumParser;

import java.io.InputStream;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/nomCode.txt.
 */
public class NomCodeParser extends EnumParser<NomenclaturalCode> {

    private static NomCodeParser singletonObject = null;

    private NomCodeParser(InputStream... file) {
        super(NomenclaturalCode.class, false, file);
        // also make sure we have all enum knowledge mapped
        for (NomenclaturalCode c : NomenclaturalCode.values()) {
            add(c.getAcronym(), c);
            add(c.getTitle(), c);
        }
    }

    public static NomCodeParser getInstance()
        throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
        synchronized (NomCodeParser.class) {
            if (singletonObject == null) {
                singletonObject = new NomCodeParser(NomCodeParser.class.getResourceAsStream("/dictionaries/parse/nomCode.tsv"));
            }
        }
        return singletonObject;
    }


}
