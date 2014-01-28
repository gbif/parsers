package org.gbif.common.parsers.basisofrecord;


import org.gbif.common.parsers.FileBasedDictionaryParserTest;

import org.junit.Before;
import org.junit.Test;

public class BasisOfRecordParserTest extends FileBasedDictionaryParserTest {

  private BasisOfRecordParser borp = null;

  @Before
  public void setupBasisOfRecordParser() {
    borp = BasisOfRecordParser.getInstance();
  }

  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(borp, "LIVING_SPECIMEN", "living organism");
    assertParseSuccess(borp, "PRESERVED_SPECIMEN", "SpeCImen");
    assertParseSuccess(borp, "OBSERVATION", "sound recording");
    assertParseSuccess(borp, "OBSERVATION", "Observation");
    assertParseSuccess(borp, "OBSERVATION", "O");
  }

  /**
   * This tests if all the names defined in the lookup_basis_of_record table are successfully parsed.
   */
  @Test
  public void testDefaultNames() {
    assertParseSuccess(borp, "UNKNOWN", "unknown");
    assertParseSuccess(borp, "OBSERVATION", "observation");
    assertParseSuccess(borp, "PRESERVED_SPECIMEN", "specimen");
    assertParseSuccess(borp, "LIVING_SPECIMEN", "living");
    assertParseSuccess(borp, "LIVING_SPECIMEN", "germplasm");
    assertParseSuccess(borp, "FOSSIL_SPECIMEN", "fossil");
    assertParseSuccess(borp, "LITERATURE", "literature");
  }

  @Test
  public void testFailures() {
    assertParseFailure(borp, null);
    assertParseFailure(borp, "");
    assertParseFailure(borp, "Tim");
    assertParseFailure(borp, "regional_checklist");
    assertParseFailure(borp, "legislative_list");
  }
}
