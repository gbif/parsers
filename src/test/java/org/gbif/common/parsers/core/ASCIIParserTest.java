package org.gbif.common.parsers.core;

import org.gbif.common.parsers.ParserTestBase;

import org.junit.Test;

/**
 *
 */
public class ASCIIParserTest extends ParserTestBase<String> {

  public ASCIIParserTest() {
    super(ASCIIParser.getInstance());
  }

  @Test
  public void testParse() throws Exception {
    assertParseSuccess("Hello Bertocko", "Hello Bértöçkø");
    assertParseSuccess("Doring aeoe o aoaueaoiuuaUEc", "Döring æœ ø åöäüêâôîûúáÙÈç");
  }
}
