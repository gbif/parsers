package org.gbif.common.parsers;

import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;

import java.io.IOException;

import com.google.common.base.Strings;
import com.google.common.io.LineProcessor;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class ParserTestBase<T> implements LineProcessor<ParserTestBase.BatchParseResult>  {
  protected final Parsable<T> parser;
  private BatchParseResult batchResult;

  public static class BatchParseResult {
    public int total;
    public int failed;
  }

  @Before
  public void setupBatch() {
    batchResult = new BatchParseResult();
  }

  public ParserTestBase(Parsable<T> parser) {
    this.parser = parser;
  }

  protected void assertParseFailure(String input) {
    ParseResult<T> parsed = parser.parse(input);
    assertEquals(ParseResult.STATUS.FAIL, parsed.getStatus());
  }

  protected void assertParseSuccess(T expected, String input) {
    assertParseSuccess(expected, ParseResult.CONFIDENCE.DEFINITE, input);
  }

  protected void assertParseSuccess(T expected, ParseResult.CONFIDENCE confidence, String input) {
    ParseResult<T> parsed = parser.parse(input);
    assertNotNull(parsed);
    assertEquals(ParseResult.STATUS.SUCCESS, parsed.getStatus());
    assertNotNull(parsed.getPayload());
    assertEquals(expected, parsed.getPayload());
    if (confidence != null) {
      assertEquals(confidence, parsed.getConfidence());
    }
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
  public ParserTestBase.BatchParseResult getResult() {
    return batchResult;
  }
}
