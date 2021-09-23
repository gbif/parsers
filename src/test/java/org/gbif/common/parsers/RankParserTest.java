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

import org.gbif.api.vocabulary.Rank;

import org.junit.jupiter.api.Test;

public class RankParserTest extends ParserTestBase<Rank> {

  public RankParserTest() {
    super(RankParser.getInstance());
  }

  /**
   * This ensures that ALL enum values are at least parsable by the name they
   * are created with.
   */
  @Test
  public void testCompleteness() {
    for (Rank t : Rank.values()) {
      assertParseSuccess(t, t.name());
    }
  }

  @Test
  public void testFailures() {
    assertParseFailure(null);
    assertParseFailure("");
    assertParseFailure("Tim");
  }


  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(Rank.SPECIES, "species");
    assertParseSuccess(Rank.SPECIES, "SPECIES");
    assertParseSuccess(Rank.SPECIES, "spécies");
    assertParseSuccess(Rank.SPECIES, "speçies");
    assertParseSuccess(Rank.TRIBE, "TRIBO");
    assertParseSuccess(Rank.UNRANKED, "Species Hypothesis");
    assertParseSuccess(Rank.SPECIES_AGGREGATE, "speçies complex");
    assertParseSuccess(Rank.SPECIES_AGGREGATE, "species aggr");
    assertParseSuccess(Rank.SPECIES_AGGREGATE, "agg");
    assertParseSuccess(Rank.SPECIES_AGGREGATE, "superspecies");
  }
}
