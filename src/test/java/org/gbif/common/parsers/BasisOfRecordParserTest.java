package org.gbif.common.parsers;

import org.gbif.api.vocabulary.BasisOfRecord;
import org.gbif.common.parsers.core.ParseResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

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
    assertParseSuccess(BasisOfRecord.HUMAN_OBSERVATION, "HumanObservation");
    assertParseSuccess(BasisOfRecord.UNKNOWN, "unknown");
    assertParseSuccess(BasisOfRecord.OBSERVATION, "observation");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "specimen");
    assertParseSuccess(BasisOfRecord.LIVING_SPECIMEN, "living");
    assertParseSuccess(BasisOfRecord.LIVING_SPECIMEN, "germplasm");
    assertParseSuccess(BasisOfRecord.FOSSIL_SPECIMEN, "fossil");
    assertParseSuccess(BasisOfRecord.LITERATURE, "literature");

    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "50 specimens");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "1250 specimens");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "72 specimens");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "Espèce");
    assertParseSuccess(BasisOfRecord.LIVING_SPECIMEN, "living, growing plant");
    assertParseSuccess(BasisOfRecord.LIVING_SPECIMEN, "germplasm");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "Alcohol / Microscope");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "preservé 179600");
  }

  /**
   * Parse all unique basis of record values found in our index and make sure parsing doesn't get worse.
   * If the test file is updated, values here need to be adjusted!
   */
  @Test
  public void testOccurrenceValues() throws IOException {
    final int CURRENT_TESTS_SUCCESSFUL = 248;
    final int CURRENT_DISTINCT = 9;

    int failed = 0;
    int success = 0;
    Set<BasisOfRecord> values = new HashSet<>();

    BufferedReader r = new BufferedReader(new InputStreamReader(
      getClass().getResourceAsStream("/parse/basisofrecord/test_bor.txt"), StandardCharsets.UTF_8));
    String line;
    while ((line=r.readLine()) != null) {
      ParseResult<BasisOfRecord> parsed = parser.parse(line);
      if (parsed.isSuccessful()) {
        success++;
        values.add(parsed.getPayload());
      } else {
        System.out.println("Failed: " + line);
        failed++;
      }
    }

    System.out.println(failed + " failed parse results");
    System.out.println(success + " successful parse results");
    System.out.println(values.size() + " distinct basis of record values parsed");
    assertTrue(CURRENT_TESTS_SUCCESSFUL <= success);
    assertTrue(CURRENT_DISTINCT <= values.size());
  }
}
