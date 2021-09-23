package org.gbif.common.parsers;

import org.gbif.api.vocabulary.EstablishmentMeans;

import org.junit.jupiter.api.Test;

public class EstablishmentMeansParserTest extends ParserTestBase<EstablishmentMeans> {

  public EstablishmentMeansParserTest() {
    super(EstablishmentMeansParser.getInstance());
  }

  /**
   * Makes sure all Country enum values are parsed ok.
   */
  @Test
  public void testParseAllEnumValues() {
    for (EstablishmentMeans c : EstablishmentMeans.values()) {
      assertParseSuccess(c, c.name());
      assertParseSuccess(c, c.name().toLowerCase());
    }
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Really great britain");
    assertParseFailure("Padua");
  }

  @Test
  public void testParseSuccess() {
    assertParseSuccess(EstablishmentMeans.NATIVE, "native!");
  }

}
