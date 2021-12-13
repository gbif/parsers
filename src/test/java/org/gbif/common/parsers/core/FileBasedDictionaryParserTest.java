/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.common.parsers.core;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileBasedDictionaryParserTest {

  private static final String COMMENT_MARKER = "#";
  private static final String TEST_DICTIONARY = "/parse/simpleDictionary.txt";

  class TestParser extends FileBasedDictionaryParser<String> {

    public TestParser(InputStream... inputs) {
      super(false);
      init(FileBasedDictionaryParserTest.class.getResourceAsStream(TEST_DICTIONARY));
    }

    @Override
    protected String fromDictFile(String value) {
      return value;
    }
  }

  /**
   * test implementation that supports commented lines
   *
   * @author cgendreau
   */
  class TestParserWithCommentSupport extends FileBasedDictionaryParser<String> {

    public TestParserWithCommentSupport(InputStream... inputs) {
      super(false);
      init(FileBasedDictionaryParserTest.class.getResourceAsStream(TEST_DICTIONARY), COMMENT_MARKER);
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
    assertParseSuccess(dbp, "31", COMMENT_MARKER + "carey");
    assertParseSuccess(dbp, "31", COMMENT_MARKER + "careY");

    assertParseFailure(dbp, "Lars");
  }

  @Test
  public void testParseFileWithComment() {
    Parsable<String> dbp = new TestParserWithCommentSupport();
    assertParseSuccess(dbp, "32", "Tim");
    assertParseFailure(dbp, COMMENT_MARKER + "carey");
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
