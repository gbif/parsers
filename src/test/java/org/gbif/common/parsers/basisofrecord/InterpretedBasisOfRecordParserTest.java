package org.gbif.common.parsers.basisofrecord;

import org.gbif.api.vocabulary.BasisOfRecord;
import org.gbif.common.parsers.InterpretedParserTest;

import org.junit.Test;

/**
 *
 */
public class InterpretedBasisOfRecordParserTest extends InterpretedParserTest<BasisOfRecord> {

  public InterpretedBasisOfRecordParserTest() {
    super(InterpretedBasisOfRecordParser.getInstance());
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
    assertParseSuccess(parser, BasisOfRecord.LIVING_SPECIMEN, "living organism");
    assertParseSuccess(parser, BasisOfRecord.PRESERVED_SPECIMEN, "SpeCImen");
    assertParseSuccess(parser, BasisOfRecord.OBSERVATION, "sound recording");
    assertParseSuccess(parser, BasisOfRecord.OBSERVATION, "Observation");
    assertParseSuccess(parser, BasisOfRecord.OBSERVATION, "O");
  }

}
