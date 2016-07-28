package org.gbif.common.parsers;

import org.gbif.api.vocabulary.MaintenanceUpdateFrequency;

import org.junit.Test;

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
