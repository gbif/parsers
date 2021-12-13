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
package org.gbif.common.parsers.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DictionaryBackedParserTest {

  @Test
  public void testParse() {
    List<KeyValue<String, Integer>> source = new ArrayList<KeyValue<String, Integer>>();
    source.add(new KeyValue<String, Integer>("Tim", 32));
    source.add(new KeyValue<String, Integer>("Markus", 38));
    source.add(new KeyValue<String, Integer>("Jose", 28));
    DictionaryBackedParser<Integer> dbp = new DictionaryBackedParser<Integer>(false);
    dbp.init(source.iterator());

    assertParsed(dbp, "Tim", 32);
    assertParsed(dbp, "TIM", 32);
    assertParsed(dbp, "tIm", 32);
    assertParsed(dbp, " \t Markus \t ", 38);
    assertParsed(dbp, "MarKUS", 38);
    assertParsed(dbp, "Jose", 28);
    assertEquals(ParseResult.STATUS.FAIL, dbp.parse("Lars").getStatus());
  }

  @Test
  public void testSensitiveParse() {
    List<KeyValue<String, Integer>> source = new ArrayList<KeyValue<String, Integer>>();
    source.add(new KeyValue<String, Integer>("Matt", 29));
    DictionaryBackedParser<Integer> dbp = new DictionaryBackedParser<Integer>(true);
    dbp.init(source.iterator());

    assertParsed(dbp, "Matt", 29);
    assertParsed(dbp, " \t Matt \n ", 29);
    assertEquals(ParseResult.STATUS.FAIL, dbp.parse("MATT").getStatus());
  }

  private void assertParsed(DictionaryBackedParser<Integer> dbp, String input, Integer payload) {
    assertNotNull(dbp.parse(input));
    assertEquals(ParseResult.STATUS.SUCCESS, dbp.parse(input).getStatus());
    assertEquals(ParseResult.CONFIDENCE.DEFINITE, dbp.parse(input).getConfidence());
    assertEquals(payload, dbp.parse(input).getPayload());
  }

}
