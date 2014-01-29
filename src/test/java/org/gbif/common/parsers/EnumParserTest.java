package org.gbif.common.parsers;

import org.gbif.common.parsers.core.EnumParser;
import org.gbif.common.parsers.core.ParseResult;

import java.io.IOException;

import com.google.common.base.Strings;
import com.google.common.io.LineProcessor;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EnumParserTest<T extends Enum<T>> implements LineProcessor<EnumParserTest.BatchParseResult>  {
  protected final EnumParser<T> parser;
  private BatchParseResult batchResult;

  public static class BatchParseResult {
    public int total;
    public int failed;
  }

  @Before
  public void setupBatch() {
    batchResult = new BatchParseResult();
  }

  public EnumParserTest(EnumParser<T> parser) {
    this.parser = parser;
  }

  protected void assertParseFailure(String input) {
    ParseResult<T> parsed = parser.parse(input);
    assertEquals(ParseResult.STATUS.FAIL, parsed.getStatus());
  }

  protected void assertParseSuccess(T expected, String input) {
    ParseResult<T> parsed = parser.parse(input);
    assertNotNull(parsed);
    assertEquals(ParseResult.STATUS.SUCCESS, parsed.getStatus());
    assertNotNull(parsed.getPayload());
    assertEquals(expected, parsed.getPayload());
    assertEquals(ParseResult.CONFIDENCE.DEFINITE, parsed.getConfidence());
  }

  @Override
  public boolean processLine(String line) throws IOException {
    if (Strings.isNullOrEmpty(line)) {
      return false;
    }
    ParseResult<T> parsed = parser.parse(line);
    batchResult.total++;
    if (parsed == null || !parsed.isSuccessful() || parsed.getPayload()==null) {
      batchResult.failed++;
    }
    return true;
  }

  @Override
  public EnumParserTest.BatchParseResult getResult() {
    return batchResult;
  }
}
