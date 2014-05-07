package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Continent;

import org.junit.Test;

/**
 *
 */
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
