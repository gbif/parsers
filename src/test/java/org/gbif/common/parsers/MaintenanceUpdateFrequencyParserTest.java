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

import org.gbif.api.vocabulary.MaintenanceUpdateFrequency;

import org.junit.jupiter.api.Test;

public class MaintenanceUpdateFrequencyParserTest extends ParserTestBase<MaintenanceUpdateFrequency> {

  public MaintenanceUpdateFrequencyParserTest() {
    super(MaintenanceUpdateFrequencyParser.getInstance());
  }

  /**
   * Makes sure all License enum values are parsed ok.
   */
  @Test
  public void testParseAllEnumValues() {
    for (MaintenanceUpdateFrequency m : MaintenanceUpdateFrequency.values()) {
      assertParseSuccess(m, m.name());
    }
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Never ever again");
    assertParseFailure("N/A");
  }

  @Test
  public void testParse() {
    assertParseSuccess(MaintenanceUpdateFrequency.ANNUALLY, "annually");
    assertParseSuccess(MaintenanceUpdateFrequency.ANNUALLY, "annual");
    assertParseSuccess(MaintenanceUpdateFrequency.ANNUALLY, "ANNUEL");
    assertParseSuccess(MaintenanceUpdateFrequency.ANNUALLY, "Atualmente");

    assertParseSuccess(MaintenanceUpdateFrequency.OTHER_MAINTENANCE_PERIOD, "Other maintenance period");
    assertParseSuccess(MaintenanceUpdateFrequency.OTHER_MAINTENANCE_PERIOD, "Otro periodo de mantenimiento");
    assertParseSuccess(MaintenanceUpdateFrequency.OTHER_MAINTENANCE_PERIOD, "Autre période de maintenance");
    assertParseSuccess(MaintenanceUpdateFrequency.OTHER_MAINTENANCE_PERIOD, "Outro período de manutenção");
  }
}
