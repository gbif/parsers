package org.gbif.common.parsers;

import org.gbif.api.vocabulary.DistributionStatus;
import org.junit.Test;

public class DistributionStatusParserTest extends ParserTestBase<DistributionStatus> {

  public DistributionStatusParserTest() {
    super(DistributionStatusParser.getInstance());
  }

  @Test
  public void testParseAllEnumValues() {
    for (DistributionStatus c : DistributionStatus.values()) {
      assertParseSuccess(c, c.name());
      assertParseSuccess(c, c.name().toLowerCase());
    }
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Really great!");
    assertParseFailure("Padua");
  }

  @Test
  public void testParsing() {
    assertParseSuccess(DistributionStatus.PRESENT, "present");
    assertParseSuccess(DistributionStatus.PRESENT, "endemic!");
    assertParseSuccess(DistributionStatus.RARE, "Uncommon");
  }
}
