package org.gbif.common.parsers;

import org.gbif.api.vocabulary.LifeStage;

import org.junit.jupiter.api.Test;

public class LifeStageParserTest extends ParserTestBase<LifeStage> {

  public LifeStageParserTest() {
    super(LifeStageParser.getInstance());
  }

  /**
   * Makes sure all Country enum values are parsed ok.
   */
  @Test
  public void testParseAllEnumValues() {
    for (LifeStage c : LifeStage.values()) {
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
    assertParseSuccess(LifeStage.ADULT, "adult!");
  }

}
