package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Sex;

import org.junit.Test;

/**
 *
 */
public class SexParserTest extends ParserTestBase<Sex> {

  public SexParserTest() {
    super(SexParser.getInstance());
  }

  /**
   * Makes sure all Country enum values are parsed ok.
   */
  @Test
  public void testParseAllEnumValues() {
    for (Sex c : Sex.values()) {
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
    assertParseSuccess(Sex.MALE, "männlich");
    assertParseSuccess(Sex.MALE, "man");
    assertParseSuccess(Sex.FEMALE, "women");
  }

}
