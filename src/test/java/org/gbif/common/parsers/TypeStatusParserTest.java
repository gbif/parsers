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

import org.gbif.api.vocabulary.TypeStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeStatusParserTest extends ParserTestBase<TypeStatus> {

  public TypeStatusParserTest() {
    super(TypeStatusParser.getInstance());
  }

  /**
   * This ensures that ALL enum values are at least parsable by the name they
   * are created with.
   */
  @Test
  public void testCompleteness() {
    for (TypeStatus t : TypeStatus.values()) {
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
    assertParseSuccess(TypeStatus.ALLOTYPE, "allotype");
    assertParseSuccess(TypeStatus.HOLOTYPE, "Holotype of Abies alba");
    assertParseSuccess(TypeStatus.HOLOTYPE, "Holotype for Abies alba");
    assertParseSuccess(TypeStatus.PARATYPE, "paratype(s)");
    assertParseSuccess(TypeStatus.PARATYPE, "Parátipo");
  }

  @Test
  public void testFileCoverage() throws IOException {
    // parses all values in our test file (generated from real occurrence data) and verifies we never get worse at parsing
    URL fileUrl = this.getClass().getClassLoader().getResource("parse/typestatus/type_status.txt");
    assertNotNull(fileUrl);

    try (BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()))) {
      readLines(in, this);
    }

    BatchParseResult result = getResult();
    System.out.printf("%s out of %s lines failed to parse%n", result.failed, result.total);
    assertTrue(result.failed > 0);
    assertTrue(result.failed <= 26813);
  }
}
