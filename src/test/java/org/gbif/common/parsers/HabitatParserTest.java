package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Habitat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
