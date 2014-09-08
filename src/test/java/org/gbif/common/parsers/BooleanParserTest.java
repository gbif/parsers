package org.gbif.common.parsers;

import org.gbif.api.vocabulary.BasisOfRecord;
import org.gbif.common.parsers.core.Parsable;

import org.junit.Test;

import static org.junit.Assert.*;

public class BooleanParserTest extends ParserTestBase<Boolean>  {

  public BooleanParserTest() {
    super(BooleanParser.getInstance());
  }


  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(true, "t");
    assertParseSuccess(true, "1");
    assertParseSuccess(true, "true");
    assertParseSuccess(true, "True");
    assertParseSuccess(true, "wahr");
    assertParseSuccess(true, "T");

    assertParseSuccess(Boolean.FALSE, "False");
    assertParseSuccess(false, "falsch");
    assertParseSuccess(false, "0");
    assertParseSuccess(false, "-1");
    assertParseSuccess(false, "no");
  }
}