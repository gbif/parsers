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

import org.gbif.api.vocabulary.ThreatStatus;

import org.junit.jupiter.api.Test;

public class ThreatStatusParserTest extends ParserTestBase<ThreatStatus> {

  public ThreatStatusParserTest() {
    super(ThreatStatusParser.getInstance());
  }


  @Test
  public void testParseAllEnumValues() {
    for (ThreatStatus c : ThreatStatus.values()) {
      assertParseSuccess(c, c.name());
      assertParseSuccess(c, c.name().toLowerCase());
      assertParseSuccess(c, c.getCode().toLowerCase());
    }
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Not dead yet.");
    assertParseFailure("Padua");
  }

  @Test
  public void testParseSuccess() {
    assertParseSuccess(ThreatStatus.ENDANGERED, "ENDANGERED");
    assertParseSuccess(ThreatStatus.LEAST_CONCERN, "Least Concern");
    assertParseSuccess(ThreatStatus.LEAST_CONCERN, "Lower Risk/least concern");
    assertParseSuccess(ThreatStatus.EXTINCT, "EX");
    assertParseSuccess(ThreatStatus.EXTINCT_IN_THE_WILD, "EW");
  }
}
