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

import org.gbif.api.vocabulary.BasisOfRecord;
import org.gbif.common.parsers.core.EnumParser;

import java.io.InputStream;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/basisOfRecord.txt.
 */
public class BasisOfRecordParser extends EnumParser<BasisOfRecord> {

  private static BasisOfRecordParser singletonObject = null;

  private BasisOfRecordParser(InputStream... file) {
    super(BasisOfRecord.class, false, file);
  }

  public static BasisOfRecordParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (BasisOfRecordParser.class) {
      if (singletonObject == null) {
        singletonObject = new BasisOfRecordParser(BasisOfRecordParser.class.getResourceAsStream("/dictionaries/parse/basisOfRecord.tsv"));
      }
    }
    return singletonObject;
  }

}
