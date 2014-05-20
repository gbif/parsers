package org.gbif.common.parsers.geospatial;

import org.gbif.common.parsers.ParserTestBase;

import org.junit.Test;

/**
 *
 */
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

  }

}
