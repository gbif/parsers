/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.common.parsers;

import org.gbif.api.vocabulary.BasisOfRecord;
import org.gbif.common.parsers.core.ParseResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasisOfRecordParserTest extends ParserTestBase<BasisOfRecord> {

  public BasisOfRecordParserTest() {
    super(BasisOfRecordParser.getInstance());
  }

  private static BasisOfRecord getValue(BasisOfRecord basisOfRecord) {
    return
      // Literature is replaced with Material Citation
      (BasisOfRecord.LITERATURE == basisOfRecord ? BasisOfRecord.MATERIAL_CITATION :
        // Unknown is replaced with Occurrence
        (BasisOfRecord.UNKNOWN == basisOfRecord? BasisOfRecord.OCCURRENCE  : basisOfRecord));
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

      assertParseSuccess(getValue(t),
                         getValue(t).name());
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
    assertParseSuccess(BasisOfRecord.OCCURRENCE, "unknown");
    assertParseSuccess(BasisOfRecord.OBSERVATION, "observation");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "specimen");
    assertParseSuccess(BasisOfRecord.LIVING_SPECIMEN, "living");
    assertParseSuccess(BasisOfRecord.LIVING_SPECIMEN, "germplasm");
    assertParseSuccess(BasisOfRecord.FOSSIL_SPECIMEN, "fossil");

    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "50 specimens");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "1250 specimens");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "72 specimens");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "Espèce");
    assertParseSuccess(BasisOfRecord.LIVING_SPECIMEN, "living, growing plant");
    assertParseSuccess(BasisOfRecord.LIVING_SPECIMEN, "germplasm");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "Alcohol / Microscope");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "preservé 179600");
    assertParseSuccess(BasisOfRecord.PRESERVED_SPECIMEN, "Pinned specimen");

    // Deprecated bases
    assertParseSuccess(BasisOfRecord.MATERIAL_CITATION, "Literature");
    assertParseSuccess(BasisOfRecord.OCCURRENCE, "Unknown");
  }

  /**
   * Parse all unique basis of record values found in our index and make sure parsing doesn't get worse.
   * If the test file is updated, values here need to be adjusted!
   */
  @Test
  public void testOccurrenceValues() throws IOException {
    final int CURRENT_TESTS_SUCCESSFUL = 248;
    final int CURRENT_DISTINCT = 8;

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
