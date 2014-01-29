package org.gbif.common.parsers.core;

import java.io.InputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FileBasedDictionaryParserTest {

  class TestParser extends FileBasedDictionaryParser<String> {

    public TestParser(InputStream... inputs) {
      super(false);
      init(FileBasedDictionaryParserTest.class.getResourceAsStream("/parse/simpleDictionary.txt"));
    }

    @Override
    protected String fromDictFile(String value) {
      return value;
    }
  }

  @Test
  public void testParse() {
    Parsable<String> dbp = new TestParser();
    assertParseSuccess(dbp, "32", "Tim");
    assertParseSuccess(dbp, "32", "TIM");
    assertParseSuccess(dbp, "32", "tIm");
    assertParseSuccess(dbp, "38", "Markus");
    assertParseSuccess(dbp, "38", "MarKUS");
    assertParseSuccess(dbp, "28", "Jose");

    assertParseFailure(dbp, "Lars");
  }

  protected static void assertParseSuccess(Parsable<String> dbp, String expected, String input) {
    ParseResult<String> parsed = dbp.parse(input);
    assertNotNull(parsed);
    assertEquals(expected, parsed.getPayload());
    assertEquals(ParseResult.STATUS.SUCCESS, parsed.getStatus());
    assertEquals(ParseResult.CONFIDENCE.DEFINITE, parsed.getConfidence());
  }

  protected static void assertParseFailure(Parsable<String> dbp, String input) {
    ParseResult<String> parsed = dbp.parse(input);
    assertEquals(ParseResult.STATUS.FAIL, parsed.getStatus());
  }
}
