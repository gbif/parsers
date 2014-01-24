package org.gbif.common.parsers.countryname;

import org.gbif.common.parsers.FileBasedDictionaryParserTest;

import org.junit.Test;

public class CountryNameParserTest extends FileBasedDictionaryParserTest {

  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    CountryNameParser cnp = CountryNameParser.getInstance();
    assertParseFailure(cnp, "[West Indian Ocean]");
    assertParseSuccess(cnp, "GB", "Great Britain");
    assertParseSuccess(cnp, "GB", "Great Britain!!!");
    assertParseFailure(cnp, "Really great britain");
    assertParseSuccess(cnp, "GB", "Great  Britain"); // Test collapsing of multiple whitespaces
    assertParseSuccess(cnp, "GB", " Great\tBritain "); // Test collapsing of other kinds of whitespace and trimming
    assertParseSuccess(cnp, "DK", "Denmark");
    assertParseSuccess(cnp, "DE", " Germany "); // Test trimming
    assertParseSuccess(cnp, "AU", "off Australia");
    assertParseSuccess(cnp, "AU", " off (australia)\t! \u0312 "); // all together now...
    assertParseSuccess(cnp, "DK", "DK");
    assertParseSuccess(cnp, "DK", "OFF DENMARK");
    assertParseSuccess(cnp, "ME", "me");
    assertParseSuccess(cnp, "SS", "ss");
    assertParseSuccess(cnp, "SS", "SOUTH_SUDAN");
    assertParseSuccess(cnp, "SS", "south sudan");
    assertParseSuccess(cnp, "MX", "México");

    assertParseFailure(cnp, "Borneo");
    assertParseSuccess(cnp, "BA", "BOSNIA");

  }

}
