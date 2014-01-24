package org.gbif.common.parsers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FileBasedDictionaryParserTest {

  @Test
  public void testParse() {
    Parsable<String, String> dbp =
      new FileBasedDictionaryParser(false, getClass().getResourceAsStream("/parse/simpleDictionary.txt"));
    assertParseSuccess(dbp, "32", "Tim");
    assertParseSuccess(dbp, "32", "TIM");
    assertParseSuccess(dbp, "32", "tIm");
    assertParseSuccess(dbp, "38", "Markus");
    assertParseSuccess(dbp, "38", "MarKUS");
    assertParseSuccess(dbp, "28", "Jose");

    assertParseFailure(dbp, "Lars");
  }

  protected static void assertParseSuccess(Parsable<String, String> dbp, String expected, String input) {
    ParseResult<String> parsed = dbp.parse(input);
    assertNotNull(parsed);
    assertEquals(expected, parsed.getPayload());
    assertEquals(ParseResult.STATUS.SUCCESS, parsed.getStatus());
    assertEquals(ParseResult.CONFIDENCE.DEFINITE, parsed.getConfidence());
  }

  protected static void assertParseFailure(Parsable<String, String> dbp, String input) {
    ParseResult<String> parsed = dbp.parse(input);
    assertEquals(ParseResult.STATUS.FAIL, parsed.getStatus());
  }
}
