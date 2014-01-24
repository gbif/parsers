package org.gbif.common.parsers;

import org.gbif.api.model.common.InterpretedEnum;

import java.io.IOException;

import com.google.common.base.Strings;
import com.google.common.io.LineProcessor;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InterpretedParserTest<T extends Enum<T>> implements LineProcessor<InterpretedParserTest.BatchParseResult>  {
  protected final InterpretedEnumParser<T> parser;
  private BatchParseResult batchResult;

  public static class BatchParseResult {
    public int total;
    public int failed;
  }

  @Before
  public void setupBatch() {
    batchResult = new BatchParseResult();
  }

  public InterpretedParserTest(InterpretedEnumParser<T> parser) {
    this.parser = parser;
  }

  protected void assertParseFailure(Parsable<String, InterpretedEnum<String, T>> dbp, String input) {
    ParseResult<InterpretedEnum<String, T>> parsed = dbp.parse(input);
    assertEquals(ParseResult.STATUS.FAIL, parsed.getStatus());
  }

  protected void assertParseSuccess(Parsable<String, InterpretedEnum<String, T>> dbp, T expected, String input) {
    ParseResult<InterpretedEnum<String, T>> parsed = dbp.parse(input);
    assertNotNull(parsed);
    assertEquals(ParseResult.STATUS.SUCCESS, parsed.getStatus());
    assertNotNull(parsed.getPayload());
    assertEquals(expected, parsed.getPayload().getInterpreted());
    assertEquals(input, parsed.getPayload().getVerbatim());
    assertEquals(ParseResult.CONFIDENCE.DEFINITE, parsed.getConfidence());
  }
  @Override
  public boolean processLine(String line) throws IOException {
    if (Strings.isNullOrEmpty(line)) {
      return false;
    }
    ParseResult<InterpretedEnum<String, T>> parsed = parser.parse(line);
    batchResult.total++;
    if (parsed == null || !parsed.isSuccessful() || parsed.getPayload()==null) {
      batchResult.failed++;
    }
    return true;
  }

  @Override
  public InterpretedParserTest.BatchParseResult getResult() {
    return batchResult;
  }
}
