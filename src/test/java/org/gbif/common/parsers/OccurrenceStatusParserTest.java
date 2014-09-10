package org.gbif.common.parsers;

import org.gbif.api.vocabulary.OccurrenceStatus;

import org.junit.Test;

public class OccurrenceStatusParserTest extends ParserTestBase<OccurrenceStatus> {

  public OccurrenceStatusParserTest() {
    super(OccurrenceStatusParser.getInstance());
  }


  @Test
  public void testParseAllEnumValues() {
    for (OccurrenceStatus c : OccurrenceStatus.values()) {
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
    assertParseSuccess(OccurrenceStatus.PRESENT, "present");
    assertParseSuccess(OccurrenceStatus.PRESENT, "endemic!");
    assertParseSuccess(OccurrenceStatus.RARE, "Uncommon");
  }

}