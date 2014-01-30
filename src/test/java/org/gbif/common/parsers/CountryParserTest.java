package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Country;
import org.gbif.common.parsers.core.ParseResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

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
    final int CURRENT_COUNTRIES_PARSED = 251; //246;
    final int CURRENT_TESTS_SUCCESSFUL = 3311;// 2976;


    // remember number of successful parsed countries:
    Map<Country, AtomicInteger> stats = Maps.newHashMap();
    int values = 0;
    int failed = 0;
    int success = 0;
    BufferedReader r = new BufferedReader(
      new InputStreamReader(this.getClass().getResourceAsStream("/parse/countryname/occurrence_countries.txt"),
        Charsets.UTF_8));
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
    assertParseFailure("[West Indian Ocean]");
    assertParseFailure("Really great britain");
    assertParseFailure("Borneo");
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
  }
}
