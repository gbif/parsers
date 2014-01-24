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
    assertParseSuccess(borp, "Living", "living organism");
    assertParseSuccess(borp, "Specimen", "SpeCImen");
    assertParseSuccess(borp, "Observation", "sound recording");
    assertParseSuccess(borp, "Observation", "Observation");
    assertParseSuccess(borp, "Observation", "O");
  }

  /**
   * This tests if all the names defined in the lookup_basis_of_record table are successfully parsed.
   */
  @Test
  public void testDefaultNames() {
    assertParseSuccess(borp, "Unknown", "unknown");
    assertParseSuccess(borp, "Observation", "observation");
    assertParseSuccess(borp, "Specimen", "specimen");
    assertParseSuccess(borp, "Living", "living");
    assertParseSuccess(borp, "Germplasm", "germplasm");
    assertParseSuccess(borp, "Fossil", "fossil");
    assertParseSuccess(borp, "Literature", "literature");
    assertParseSuccess(borp, "Regional Checklist", "regional_checklist");
    assertParseSuccess(borp, "Legislative List", "legislative_list");
  }

  @Test
  public void testFailures() {
    assertParseFailure(borp, null);
    assertParseFailure(borp, "");
    assertParseFailure(borp, "Tim");
  }
}
