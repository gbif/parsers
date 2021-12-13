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

import org.gbif.api.vocabulary.Sex;
import org.gbif.common.parsers.core.ParseResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SexParserTest extends ParserTestBase<Sex> {

  public SexParserTest() {
    super(SexParser.getInstance());
  }

  /**
   * Makes sure all Sex enum values are parsed ok.
   */
  @Test
  public void testParseAllEnumValues() {
    for (Sex c : Sex.values()) {
      assertParseSuccess(c, c.name());
      assertParseSuccess(c, c.name().toLowerCase());
    }
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Really great");
    assertParseFailure("Padua");
  }

  @Test
  public void testParseSuccess() {
    assertParseSuccess(Sex.MALE, "männlich");
    assertParseSuccess(Sex.MALE, "man");
    assertParseSuccess(Sex.FEMALE, "women");
  }

  /**
   * Parse all unique sex values found in our index and make sure parsing doesn't get worse.
   * If the test file is updated, values here need to be adjusted!
   */
  @Test
  public void testSexValues() throws IOException {
    final int CURRENT_TESTS_SUCCESSFUL = 111;
    final int CURRENT_DISTINCT = 3;

    int failed = 0;
    int success = 0;
    Set<Sex> values = new HashSet<>();

    BufferedReader r = new BufferedReader(new InputStreamReader(
      getClass().getResourceAsStream("/parse/sexes.txt"), StandardCharsets.UTF_8));
    String line;
    while ((line=r.readLine()) != null) {
      ParseResult<Sex> parsed = parser.parse(line);
      if (parsed.isSuccessful()) {
        //System.out.println("Succeeded: " + line + " → " + parsed.getPayload().name());
        success++;
        values.add(parsed.getPayload());
      } else {
        //System.out.println("Failed:    " + line);
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
