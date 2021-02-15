package org.gbif.common.parsers;

import org.gbif.api.vocabulary.ThreatStatus;

import org.junit.Test;

public class ThreatStatusParserTest extends ParserTestBase<ThreatStatus> {

  public ThreatStatusParserTest() {
    super(ThreatStatusParser.getInstance());
  }


  @Test
  public void testParseAllEnumValues() {
    for (ThreatStatus c : ThreatStatus.values()) {
      assertParseSuccess(c, c.name());
      assertParseSuccess(c, c.name().toLowerCase());
      assertParseSuccess(c, c.getCode().toLowerCase());
    }
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Not dead yet.");
    assertParseFailure("Padua");
  }

  @Test
  public void testParseSuccess() {
    assertParseSuccess(ThreatStatus.ENDANGERED, "ENDANGERED");
    assertParseSuccess(ThreatStatus.LEAST_CONCERN, "Least Concern");
    assertParseSuccess(ThreatStatus.LEAST_CONCERN, "Lower Risk/least concern");
    assertParseSuccess(ThreatStatus.EXTINCT, "EX");
    assertParseSuccess(ThreatStatus.EXTINCT_IN_THE_WILD, "EW");
  }
}
