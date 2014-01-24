package org.gbif.common.parsers.rank;

import org.gbif.api.vocabulary.Rank;
import org.gbif.common.parsers.InterpretedParserTest;

import org.junit.Test;

public class InterpretedRankParserTest extends InterpretedParserTest<Rank> {

  public InterpretedRankParserTest() {
    super(InterpretedRankParser.getInstance());
  }

  /**
   * This ensures that ALL enum values are at least parsable by the name they
   * are created with.
   */
  @Test
  public void testCompleteness() {
    for (Rank t : Rank.values()) {
      System.out.println("Testing [" + t.name()
                         + "].  Failures below might indicate new ranks added to the Rank enum but not to the parse file");
      assertParseSuccess(parser, t, t.name());
    }
  }

  @Test
  public void testFailures() {
    assertParseFailure(parser, null);
    assertParseFailure(parser, "");
    assertParseFailure(parser, "Tim");
  }


  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(parser, Rank.SPECIES, "species");
    assertParseSuccess(parser, Rank.SPECIES, "SPECIES");
  }
}
