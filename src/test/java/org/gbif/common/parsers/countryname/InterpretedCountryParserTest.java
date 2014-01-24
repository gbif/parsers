package org.gbif.common.parsers.countryname;

import org.gbif.api.model.common.InterpretedEnum;
import org.gbif.api.vocabulary.Country;
import org.gbif.common.parsers.ParseResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import junit.framework.TestCase;
import org.junit.Test;

public class InterpretedCountryParserTest extends TestCase {

  private InterpretedCountryParser parser = InterpretedCountryParser.getInstance();

  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertEquals(Country.GERMANY, parser.parse("Germany").getPayload().getInterpreted());
    assertEquals(Country.GERMANY, parser.parse("deutschland").getPayload().getInterpreted());
    assertEquals(Country.GERMANY, parser.parse("De").getPayload().getInterpreted());
  }

  /**
   * Makes sure all Country enum values are parsed ok.
   */
  @Test
  public void testParseAllEnumValues() {

    // make sure all
    for (Country c : Country.OFFICIAL_COUNTRIES) {
      assertEquals(c, parser.parse(c.getIso2LetterCode()).getPayload().getInterpreted());
      assertEquals(c, parser.parse(c.getIso3LetterCode()).getPayload().getInterpreted());
      assertEquals(c, parser.parse(c.getTitle()).getPayload().getInterpreted());
    }
  }

  /**
   * Parse all unique occurrence values found for countries and make sure parsing doesnt get worse.
   * If the test file is updated, values here need to be adjusted!
   */
  @Test
  public void testOccurrenceValues() throws IOException {
    final int CURRENT_COUNTRIES_PARSED = 246;
    final int CURRENT_TESTS_SUCCESSFUL = 2976;


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
      ParseResult<InterpretedEnum<String, Country>> parsed = parser.parse(line);
      if (ParseResult.STATUS.SUCCESS == parsed.getStatus()) {
        Country c = parsed.getPayload().getInterpreted();
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
}
