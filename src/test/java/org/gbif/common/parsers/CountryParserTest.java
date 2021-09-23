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

import org.gbif.api.vocabulary.Country;
import org.gbif.common.parsers.core.ParseResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CountryParserTest extends ParserTestBase<Country> {

  public CountryParserTest() {
    super(CountryParser.getInstance());
  }

  /**
   * Makes sure all Country enum values are parsed ok.
   */
  @Test
  public void testParseAllEnumValues() {
    for (Country c : Country.OFFICIAL_COUNTRIES) {
      assertParseSuccess(c, c.getIso2LetterCode());
      assertParseSuccess(c, c.getIso3LetterCode());
      assertParseSuccess(c, c.getTitle());
    }
  }

  /**
   * Parse all unique occurrence values found for countries and make sure parsing doesnt get worse.
   * If the test file is updated, values here need to be adjusted!
   */
  @Test
  public void testOccurrenceValues() throws IOException {
    final int CURRENT_COUNTRIES_PARSED = 251;
    final int CURRENT_TESTS_SUCCESSFUL = 5273;

    // remember number of successful parsed countries:
    Map<Country, AtomicInteger> stats = new HashMap<>();
    int values = 0;
    int failed = 0;
    int success = 0;
    BufferedReader r = new BufferedReader(
      new InputStreamReader(this.getClass().getResourceAsStream("/parse/countryname/occurrence_countries.txt"),
        StandardCharsets.UTF_8));
    String line;
    do {
      line = r.readLine();
      values++;
      ParseResult<Country> parsed = parser.parse(line);
      if (ParseResult.STATUS.SUCCESS == parsed.getStatus()) {
        Country c = parsed.getPayload();
        if (!stats.containsKey(c)) {
          stats.put(c, new AtomicInteger(0));
        }
        stats.get(c).incrementAndGet();
        success++;
      } else {
        failed++;
      }
    } while (line != null);

    System.out.println(values + " tested values");
    System.out.println(failed + " failed parse results");
    System.out.println(success + " successful parse results");
    System.out.println(stats.size() + " distinct countries parsed");
    assertTrue(failed < values);
    assertTrue(CURRENT_COUNTRIES_PARSED <= stats.size());
    assertTrue(CURRENT_TESTS_SUCCESSFUL <= success);
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Really great britain");
    assertParseFailure("Padua");
  }

  @Test
  public void testParseUnknown() {
    assertParseSuccess(Country.UNKNOWN, "U S S R");
    assertParseSuccess(Country.UNKNOWN, "USSR");
    assertParseSuccess(Country.UNKNOWN, "U S S R");
    assertParseSuccess(Country.UNKNOWN, "U S S R");
    assertParseSuccess(Country.UNKNOWN, "NETHERLANDS ANTILLES");
    assertParseSuccess(Country.UNKNOWN, "Borneo");
    assertParseSuccess(Country.UNKNOWN, "french congo");
    assertParseSuccess(Country.UNKNOWN, "CONGO AFRICA");
  }

  @Test
  public void testParseSuccess() {
    assertParseSuccess(Country.UNITED_KINGDOM, "Great Britain");
    assertParseSuccess(Country.UNITED_KINGDOM, "Great Britain!!!");
    assertParseSuccess(Country.UNITED_KINGDOM, "Great  Britain"); // Test collapsing of multiple whitespaces
    assertParseSuccess(Country.UNITED_KINGDOM, " Great\tBritain "); // Test collapsing of other kinds of whitespace and trimming
    assertParseSuccess(Country.DENMARK, "Denmark");
    assertParseSuccess(Country.GERMANY, " Germany "); // Test trimming
    assertParseSuccess(Country.AUSTRALIA, "off Australia");
    assertParseSuccess(Country.AUSTRALIA, " off (australia)\t! \u0312 "); // all together now...
    assertParseSuccess(Country.DENMARK, "DK");
    assertParseSuccess(Country.DENMARK, "OFF DENMARK");
    assertParseSuccess(Country.MONTENEGRO, "me");
    assertParseSuccess(Country.SOUTH_SUDAN, "ss");
    assertParseSuccess(Country.SOUTH_SUDAN, "SOUTH_SUDAN");
    assertParseSuccess(Country.SOUTH_SUDAN, "south sudan");
    assertParseSuccess(Country.MEXICO, "México");
    assertParseSuccess(Country.BOSNIA_HERZEGOVINA, "BOSNIA");
    assertParseSuccess(Country.GERMANY, "Germany");
    assertParseSuccess(Country.GERMANY, "deutschland");
    assertParseSuccess(Country.GERMANY, "De");

    assertParseSuccess(Country.FAROE_ISLANDS, "Faroe");
    assertParseSuccess(Country.FAROE_ISLANDS, "Farœ");
    assertParseSuccess(Country.FAROE_ISLANDS, "Faröe");

    assertParseSuccess(Country.BELIZE, "BRITISH HONDURAS");
    assertParseSuccess(Country.FRANCE, "CLIPPERTON ISLAND");
    assertParseSuccess(Country.FRANCE, "CLIPPERTON IS");
    assertParseSuccess(Country.FRANCE, "CLIPPERTON ID");
    assertParseSuccess(Country.FRANCE, "CLIPPERTON I");
    assertParseSuccess(Country.CONGO, "Republic of the Congo");
    assertParseSuccess(Country.CONGO, "République du Congo");
    assertParseSuccess(Country.CONGO, "People's Republic of the Congo");
    assertParseSuccess(Country.CONGO, "congo republic");
    assertParseSuccess(Country.CONGO, "Congo-Brazzaville");
    assertParseSuccess(Country.CONGO, "CONGO");
    assertParseSuccess(Country.CONGO_DEMOCRATIC_REPUBLIC, "Léopoldville");
    assertParseSuccess(Country.CONGO_DEMOCRATIC_REPUBLIC, "Zaïre");
    assertParseSuccess(Country.CONGO_DEMOCRATIC_REPUBLIC, "belgium congo");
    assertParseSuccess(Country.CONGO_DEMOCRATIC_REPUBLIC, "congo free state");
    assertParseSuccess(Country.CONGO_DEMOCRATIC_REPUBLIC, "République démocratique du Congo");
    assertParseSuccess(Country.CONGO_DEMOCRATIC_REPUBLIC, "DR Congo");
    assertParseSuccess(Country.CONGO_DEMOCRATIC_REPUBLIC, "DROC");
    assertParseSuccess(Country.CONGO_DEMOCRATIC_REPUBLIC, "Congo-Kinshasa");
    assertParseSuccess(Country.ANGOLA, "Portuguese Congo");
    assertParseSuccess(Country.CURAÇAO, "CURACAO");
    assertParseSuccess(Country.CURAÇAO, "CURACOS");
    assertParseSuccess(Country.ISLE_OF_MAN, "ISLE OF MAN");
    assertParseSuccess(Country.BONAIRE_SINT_EUSTATIUS_SABA, "ST EUSTATIUS ISLAND");
    assertParseSuccess(Country.BONAIRE_SINT_EUSTATIUS_SABA, "ST EUSTATIUS");
    assertParseSuccess(Country.BONAIRE_SINT_EUSTATIUS_SABA, "BONAIRE THE NETHERLANDS ANTILLES");
    assertParseSuccess(Country.BENIN, "B�nin");
    assertParseSuccess(Country.FINLAND, "Suomi (FI)");
    assertParseSuccess(Country.UNITED_KINGDOM, "United Kingdom");
    assertParseSuccess(Country.UNITED_STATES, "United States of Ameirca");
    assertParseSuccess(Country.KOREA_NORTH, "Korea, Democratic People's Republic");

    assertParseSuccess(Country.KOSOVO, "XK");
  }

  @Test
  public void testParseNa() {
    assertParseSuccess(Country.NAMIBIA, "NA");
    assertParseFailure("N/A");
    assertParseFailure("n/a");
    assertParseFailure("n/k");
  }
}
