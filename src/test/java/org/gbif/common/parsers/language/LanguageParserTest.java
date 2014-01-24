package org.gbif.common.parsers.language;

import org.gbif.common.parsers.FileBasedDictionaryParserTest;

import org.junit.Test;

public class LanguageParserTest extends FileBasedDictionaryParserTest {

  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    LanguageParser parser = LanguageParser.getInstance();
    assertParseFailure(parser, "[West Indian Ocean]");
    assertParseSuccess(parser, "de", "German");
    assertParseSuccess(parser, "de", "Deutsch");
    assertParseSuccess(parser, "de", "deutsch");
    assertParseSuccess(parser, "de", "GER");
    assertParseSuccess(parser, "de", "DEU");
    assertParseSuccess(parser, "de", "de");
    assertParseSuccess(parser, "de", "De ");
    assertParseSuccess(parser, "en", "en_US");
    assertParseSuccess(parser, "en", "eng_US");
    assertParseSuccess(parser, "en", "english");
  }
}
