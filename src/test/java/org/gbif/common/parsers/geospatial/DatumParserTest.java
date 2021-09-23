package org.gbif.common.parsers.geospatial;

import org.gbif.common.parsers.ParserTestBase;
import org.gbif.common.parsers.core.ParseResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatumParserTest extends ParserTestBase<Integer> {

  public DatumParserTest() {
    super(DatumParser.getInstance());
  }

  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(4269, "EPSG::4269");
    assertParseSuccess(6317, " EPSG::6317");
    assertParseSuccess(4269, "EPSG:4269");
    assertParseSuccess(4269, " EPSG :: 4269 ");
    assertParseSuccess(4269, "ESPG:4269");
    // known srs names
    assertParseSuccess(6269, "NAD83");
    assertParseSuccess(4326, "WGS84");
    assertParseSuccess(6269, "nad83");
    assertParseSuccess(6301, "tokYo!");
    assertParseSuccess(4326, "WGS_1984");
    assertParseSuccess(6202, "AGD66");

    assertParseSuccess(7405, "OSGB36");
    assertParseSuccess(7405, "Ordnance_Survey");
    assertParseSuccess(7405, "Ordnance_Survey_36");
    assertParseSuccess(7405, "OSGB36");
    assertParseSuccess(6209, "ARC 1950");
    assertParseSuccess(4222, "CAPE");
    assertParseSuccess(6131, "indian");
    assertParseSuccess(6760, "WGS66");
    assertParseSuccess(6760, "World Geodetic System 1966");
  }

  /**
   * Parse all unique datum values found and make sure parsing doesn't get worse.
   * If the test file is updated, values here need to be adjusted!
   */
  @Test
  public void testOccurrenceValues() throws IOException {
    final int CURRENT_TESTS_SUCCESSFUL = 14;

    int failed = 0;
    int success = 0;
    Set<Integer> codes = new HashSet<>();

    BufferedReader r = new BufferedReader(new InputStreamReader(
          getClass().getResourceAsStream("/parse/test_datum.txt"), StandardCharsets.UTF_8));
    String line;
    while ((line=r.readLine()) != null) {
      ParseResult<Integer> parsed = parser.parse(line);
      if (parsed.isSuccessful()) {
        success++;
        codes.add(parsed.getPayload());
      } else {
        System.out.println("Failed: " + line);
        failed++;
      }
    }

    System.out.println(failed + " failed parse results");
    System.out.println(success + " successful parse results");
    System.out.println(codes.size() + " distinct datums parsed");
    assertTrue(CURRENT_TESTS_SUCCESSFUL <= success);
  }
}
