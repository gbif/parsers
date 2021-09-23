package org.gbif.common.parsers;


import org.gbif.api.vocabulary.Language;

import org.junit.jupiter.api.Test;

public class LanguageParserTest extends ParserTestBase<Language> {

  public LanguageParserTest() {
    super(LanguageParser.getInstance());
  }

  @Test
  public void testParseFail() {
    assertParseFailure("[West Indian Ocean]");
  }

  @Test
  public void testParseSuccess() {
    assertParseSuccess(Language.NORWEGIAN_BOKMAL, "NORWEGIAN_BOKMAL");
    assertParseSuccess(Language.NORWEGIAN_BOKMAL, "norwegian bokmal");
    assertParseSuccess(Language.NORWEGIAN_BOKMAL, "bokmal");
    assertParseSuccess(Language.GERMAN, "German");
    assertParseSuccess(Language.GERMAN, "deutsch");
    assertParseSuccess(Language.GERMAN, "de");
    assertParseSuccess(Language.GERMAN, "ger");
    assertParseSuccess(Language.BASQUE, "baq");
    assertParseSuccess(Language.GERMAN, "German");
    assertParseSuccess(Language.GERMAN, "Deutsch");
    assertParseSuccess(Language.GERMAN, "deutsch");
    assertParseSuccess(Language.GERMAN, "GER");
    assertParseSuccess(Language.GERMAN, "DEU");
    assertParseSuccess(Language.GERMAN, "de");
    assertParseSuccess(Language.GERMAN, "De ");
    assertParseSuccess(Language.ENGLISH, "en_US");
    assertParseSuccess(Language.ENGLISH, "eng_US");
    assertParseSuccess(Language.ENGLISH, "english");
    assertParseSuccess(Language.PORTUGUESE, "PORTUGUES");

  }

}
