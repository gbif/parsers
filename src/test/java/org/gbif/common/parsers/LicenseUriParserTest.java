/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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

import org.gbif.common.parsers.core.ParseResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LicenseUriParserTest extends ParserTestBase<URI> {

  public LicenseUriParserTest() {
    super(LicenseUriParser.getInstance());
  }

  @Test
  public void testFailures() {
    assertParseFailure(null);
    assertParseFailure("");
    assertParseFailure("Matt");
  }

  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(URI.create("http://creativecommons.org/licenses/by-sa/3.0/"), "CC BY-SA 3.0");
    assertParseSuccess(URI.create("http://creativecommons.org/licenses/by/4.0/"), "cc-by");
    assertParseSuccess(URI.create("http://creativecommons.org/licenses/by-nc/4.0/"), "http://creativecommons.org/licenses/by-nc/4.0/");
    assertParseSuccess(URI.create("http://creativecommons.org/licenses/by/4.0/"), "http://creativecommons.org/licenses/by/4.0/");
    assertParseSuccess(URI.create("http://creativecommons.org/licenses/by/4.0/"), "https://creativecommons.org/licenses/by/4.0/");
    assertParseSuccess(URI.create("http://creativecommons.org/licenses/by/4.0/"), "http://creativecommons.org/licenses/by/4.0/legalcode");
    assertParseSuccess(URI.create("http://creativecommons.org/licenses/by/4.0/"), "https://creativecommons.org/licenses/by/4.0/legalcode#languages");
  }

  /**
   * Parse all unique multimedia licenses values found in our index and make sure parsing doesn't get worse.
   * If the test file is updated, values here need to be adjusted!
   */
  @Test
  public void testOccurrenceValues() throws IOException {
    final int CURRENT_TESTS_SUCCESSFUL = 75;
    final int CURRENT_DISTINCT = 15;

    int failed = 0;
    int success = 0;
    Set<URI> values = new HashSet<>();

    BufferedReader r = new BufferedReader(
        new InputStreamReader(getClass().getResourceAsStream("/parse/license_uri.txt"), StandardCharsets.UTF_8));
    String line;
    while ((line=r.readLine()) != null) {
      ParseResult<URI> parsed = parser.parse(line);
      if (parsed.isSuccessful()) {
        // System.out.println("Worked: " + line + " → " + parsed.getPayload());
        success++;
        values.add(parsed.getPayload());
      } else {
        System.out.println("Failed: " + line);
        failed++;
      }
    }

    System.out.println(failed + " failed parse results");
    System.out.println(success + " successful parse results");
    System.out.println(values.size() + " distinct license values parsed");
    assertTrue(CURRENT_TESTS_SUCCESSFUL <= success);
    assertTrue(CURRENT_DISTINCT <= values.size());
  }
}
