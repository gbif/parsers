package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Rank;

import org.junit.Test;

public class RankParserTest extends ParserTestBase<Rank> {

  public RankParserTest() {
    super(RankParser.getInstance());
  }

  /**
   * This ensures that ALL enum values are at least parsable by the name they
   * are created with.
   */
  @Test
  public void testCompleteness() {
    for (Rank t : Rank.values()) {
      assertParseSuccess(t, t.name());
    }
  }

  @Test
  public void testFailures() {
    assertParseFailure(null);
    assertParseFailure("");
    assertParseFailure("Tim");
  }


  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(Rank.SPECIES, "species");
    assertParseSuccess(Rank.SPECIES, "SPECIES");
  }
}
