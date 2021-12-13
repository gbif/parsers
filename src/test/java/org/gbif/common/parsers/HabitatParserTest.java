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

import org.gbif.api.vocabulary.Habitat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HabitatParserTest extends ParserTestBase<Habitat> {

  public HabitatParserTest() {
    super(HabitatParser.getInstance());
  }

  /**
   * Makes sure all Country enum values are parsed ok.
   */
  @Test
  public void testParseAllEnumValues() {
    for (Habitat c : Habitat.values()) {
      assertParseSuccess(c, c.name());
      assertParseSuccess(c, c.name().toLowerCase());
    }
  }

  @Test
  public void testStemming() {
    assertEquals("FOREST", HabitatParser.getInstance().normalize("forests"));
    assertEquals("ESTUARIE", HabitatParser.getInstance().normalize("estuaries "));
    assertEquals("MISSES", HabitatParser.getInstance().normalize("missess"));
    assertEquals("ABBA", HabitatParser.getInstance().normalize("ABBA"));
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Really great britain");
    assertParseFailure("Padua");
  }

  @Test
  public void testParseSuccess() {
    assertParseSuccess(Habitat.MARINE, "marine23");
    assertParseSuccess(Habitat.MARINE, " marine");
    assertParseSuccess(Habitat.MARINE, "salzwasser");
    assertParseSuccess(Habitat.MARINE, "ocean");

    assertParseSuccess(Habitat.FRESHWATER, "brackish");
  }

}
