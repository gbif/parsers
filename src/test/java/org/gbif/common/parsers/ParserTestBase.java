/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.common.parsers;

import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class ParserTestBase<T> implements LineProcessor<ParserTestBase.BatchParseResult>  {
  protected final Parsable<T> parser;
  private BatchParseResult batchResult;

  public static class BatchParseResult {
    public int total;
    public int failed;
  }

  @BeforeEach
  public void setupBatch() {
    batchResult = new BatchParseResult();
  }

  public ParserTestBase(Parsable<T> parser) {
    this.parser = parser;
  }

  protected void assertParseFailure(String input) {
    ParseResult<T> parsed = parser.parse(input);
    assertEquals(ParseResult.STATUS.FAIL, parsed.getStatus(), "Expected " + input + " to fail but got " + parsed.getPayload() + " instead");
  }

  protected void assertParseSuccess(T expected, String input) {
    assertParseSuccess(expected, ParseResult.CONFIDENCE.DEFINITE, input);
  }

  protected void assertParseSuccess(T expected, ParseResult.CONFIDENCE confidence, String input) {
    ParseResult<T> parsed = parser.parse(input);
    assertNotNull(parsed);
//    System.out.println(parsed);
    assertEquals(ParseResult.STATUS.SUCCESS, parsed.getStatus(), "BAD PARSING OF: " + input);
    assertNotNull(parsed.getPayload());
    assertEquals(expected, parsed.getPayload(), "BAD PARSING OF: " + input);
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
