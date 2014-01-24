package org.gbif.common.parsers.rank;


import org.gbif.common.parsers.FileBasedDictionaryParserTest;

import org.junit.Before;
import org.junit.Test;

public class RankParserTest extends FileBasedDictionaryParserTest {

  private RankParser rp = null;

  @Before
  public void setupRankParser() {
    rp = RankParser.getInstance();
  }

  @Test
  public void testFailures() {
    assertParseFailure(rp, null);
    assertParseFailure(rp, "");
    assertParseFailure(rp, "Tim");
  }

  @Override
  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(rp, "SPECIES", "species");
    assertParseSuccess(rp, "SPECIES", "SPECIES");
  }
}
