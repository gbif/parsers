package org.gbif.common.parsers;

import org.gbif.api.vocabulary.CitesAppendix;

import org.junit.jupiter.api.Test;

public class CitesAppendixParserTest extends ParserTestBase<CitesAppendix> {

  public CitesAppendixParserTest() {
    super(CitesAppendixParser.getInstance());
  }


  @Test
  public void testParseAllEnumValues() {
    for (CitesAppendix c : CitesAppendix.values()) {
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
    assertParseSuccess(CitesAppendix.II, "2");
    assertParseSuccess(CitesAppendix.II, "two!");
    assertParseSuccess(CitesAppendix.II, "ii.");
  }

}
