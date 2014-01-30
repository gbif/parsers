package org.gbif.common.parsers;

import org.gbif.api.vocabulary.BasisOfRecord;

import org.junit.Test;

/**
 *
 */
public class BasisOfRecordParserTest extends ParserTestBase<BasisOfRecord> {

  public BasisOfRecordParserTest() {
    super(BasisOfRecordParser.getInstance());
  }

  /**
   * This ensures that ALL enum values are at least parsable by the name they
   * are created with.
   */
  @Test
  public void testCompleteness() {
    for (BasisOfRecord t : BasisOfRecord.values()) {
      System.out.println("Testing [" + t.name()
                         + "].  Failures below might indicate new BasisOfRecord added to the BasisOfRecord enum but not to the parse file");
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
    assertParseSuccess(BasisOfRecord.LIVING_SPECIMEN, "living organism");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "SpeCImen");
    assertParseSuccess(BasisOfRecord.OBSERVATION, "sound recording");
    assertParseSuccess(BasisOfRecord.OBSERVATION, "Observation");
    assertParseSuccess(BasisOfRecord.OBSERVATION, "O");
    assertParseSuccess(BasisOfRecord.UNKNOWN, "unknown");
    assertParseSuccess(BasisOfRecord.OBSERVATION, "observation");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "specimen");
    assertParseSuccess(BasisOfRecord.LIVING_SPECIMEN, "living");
    assertParseSuccess(BasisOfRecord.LIVING_SPECIMEN, "germplasm");
    assertParseSuccess(BasisOfRecord.FOSSIL_SPECIMEN, "fossil");
    assertParseSuccess(BasisOfRecord.LITERATURE, "literature");
  }
}
