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

import org.gbif.api.vocabulary.Continent;

import org.junit.jupiter.api.Test;

public class ContinentParserTest extends ParserTestBase<Continent> {

  public ContinentParserTest() {
    super(ContinentParser.getInstance());
  }

  /**
   * Makes sure all Country enum values are parsed ok.
   */
  @Test
  public void testParseAllEnumValues() {
    for (Continent c : Continent.values()) {
      assertParseSuccess(c, c.name());
      assertParseSuccess(c, c.name().toLowerCase());
      assertParseSuccess(c, c.getTitle());
    }
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Really great britain");
    assertParseFailure("Padua");
    assertParseFailure("Southern Ocean");
  }

  @Test
  public void testParseSuccess() {
    assertParseSuccess(Continent.SOUTH_AMERICA, "SA");
    assertParseSuccess(Continent.NORTH_AMERICA, "Mesoamerica");
    assertParseSuccess(Continent.NORTH_AMERICA, "Caribbean");
    assertParseSuccess(Continent.NORTH_AMERICA, "North america");
    assertParseSuccess(Continent.EUROPE, "Europa");
    assertParseSuccess(Continent.AFRICA, "Afrique");
  }

}
