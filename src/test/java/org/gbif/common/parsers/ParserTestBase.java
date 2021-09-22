package org.gbif.common.parsers;

import org.apache.commons.lang3.StringUtils;
import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

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
    assertEquals("Expected "+input+" to fail but got "+parsed.getPayload()+" instead", ParseResult.STATUS.FAIL, parsed.getStatus());
  }

  protected void assertParseSuccess(T expected, String input) {
    assertParseSuccess(expected, ParseResult.CONFIDENCE.DEFINITE, input);
  }

  protected void assertParseSuccess(T expected, ParseResult.CONFIDENCE confidence, String input) {
    ParseResult<T> parsed = parser.parse(input);
    assertNotNull(parsed);
//    System.out.println(parsed);
    assertEquals("BAD PARSING OF: "+input, ParseResult.STATUS.SUCCESS, parsed.getStatus());
    assertNotNull(parsed.getPayload());
    assertEquals("BAD PARSING OF: "+input, expected, parsed.getPayload());
    if (confidence != null) {
      assertEquals(confidence, parsed.getConfidence());
    }
  }

  @Override
  public boolean processLine(String line) {
    if (StringUtils.isEmpty(line)) {
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

  public static <T> void readLines(BufferedReader reader, LineProcessor<T> processor) throws IOException {
    Objects.requireNonNull(reader);
    Objects.requireNonNull(processor);

    String line;
    while ((line = reader.readLine()) != null) {
      if (!processor.processLine(line)) {
        break;
      }
    }
  }
}
