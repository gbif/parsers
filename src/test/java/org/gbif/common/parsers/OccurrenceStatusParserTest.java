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
package org.gbif.common.parsers;

import org.gbif.api.vocabulary.OccurrenceStatus;

import org.junit.jupiter.api.Test;

public class OccurrenceStatusParserTest extends ParserTestBase<OccurrenceStatus> {

  public OccurrenceStatusParserTest() {
    super(OccurrenceStatusParser.getInstance());
  }

  @Test
  public void testParseAllEnumValues() {
    // Deprecated PRESENT/ABSENT still parse by name, but normalize to DETECTED/NOT_DETECTED.
    for (OccurrenceStatus c : OccurrenceStatus.values()) {
      assertParseSuccess(c.normalize(), c.name());
      assertParseSuccess(c.normalize(), c.name().toLowerCase());
    }
  }

  @Test
  public void testParsing() {
    // IUCN Red List statuses seem inappropriate to accept.  They are likely to be saying the taxon is rare, rather
    // than whether the occurrence was present or absent.
    assertParseFailure("EX");
    assertParseFailure("EW");
    assertParseFailure("CR");
    assertParseFailure("EN");
    assertParseFailure("VU");
    assertParseFailure("NT");
    assertParseFailure("LC");
    assertParseFailure("DD");
    assertParseFailure("NE");
    assertParseFailure("LR/cd");
    assertParseFailure("LR");
    assertParseFailure("cd");
    assertParseFailure("LR/nt");
    assertParseFailure("LR/lc");
    assertParseFailure("PE");

    // Top 100 verbatim occurrence status values seen on 2020-05-06.
    // (Duplicates due to different case and punctuation removed.)
    assertParseFailure("1");
    assertParseFailure("Damaged");
    assertParseFailure("Defoliated");
    assertParseFailure("doubtful");
    assertParseFailure("Dudoso");
    assertParseFailure("E");
    assertParseFailure("incomplet");
    assertParseFailure("NA");
    assertParseFailure("Ne Sait Pas");
    assertParseFailure("Q");
    assertParseFailure("Sanitation fellings");
    assertParseFailure("unknown");
    assertParseSuccess(OccurrenceStatus.NOT_DETECTED, "Absent");
    assertParseSuccess(OccurrenceStatus.NOT_DETECTED, "Ausente");
    assertParseSuccess(OccurrenceStatus.NOT_DETECTED, "Non observé");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Песня, голос");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Визуально");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Летящие");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Abundant.");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Abundant (10% <= p < 20%)");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Abundant 20-99");
    assertParseSuccess(OccurrenceStatus.DETECTED, "abundante");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Average Cover: 1-5% Maximum Cover: 1-5%");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Average Cover: 26-50% Maximum Cover: 26-50%");
    assertParseSuccess(OccurrenceStatus.DETECTED, "collected");
    assertParseSuccess(OccurrenceStatus.DETECTED, "common");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Common 5-19");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Common (5% <= p < 10%)");
    assertParseSuccess(OccurrenceStatus.DETECTED, "común");
    assertParseSuccess(OccurrenceStatus.DETECTED, "complet");
    assertParseSuccess(OccurrenceStatus.DETECTED, "confirmed breeding");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Confirmed Present");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Dominant (20% <= p)");
    assertParseSuccess(OccurrenceStatus.DETECTED, "established");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Frequent");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Infested");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Irregular");
    assertParseSuccess(OccurrenceStatus.DETECTED, "locally established");
    assertParseSuccess(OccurrenceStatus.DETECTED, "muito abundante");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Muy común");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Observed in Breeding Season");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Occasional.");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Occasional");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Outbreak");
    assertParseSuccess(OccurrenceStatus.DETECTED, "P");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Poco común");
    assertParseSuccess(OccurrenceStatus.DETECTED, "possible breeding");
    assertParseSuccess(OccurrenceStatus.DETECTED, "possibly breeding");
    assertParseSuccess(OccurrenceStatus.DETECTED, "presence");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Present");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Present (1% <= p < 5%)");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Presente");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Presente en el area");
    assertParseSuccess(OccurrenceStatus.DETECTED, "probable breeding");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Probably Breeding");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Présent");
    assertParseSuccess(OccurrenceStatus.DETECTED, "rare");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Rare 1-4");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Rare (p < 1%)");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Raro");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Reported");
    assertParseSuccess(OccurrenceStatus.DETECTED, "stocked");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Treated");
    assertParseSuccess(OccurrenceStatus.DETECTED, "unclear breeding certaint");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Uncommon");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Very abundant 100-499");
    assertParseSuccess(OccurrenceStatus.DETECTED, "Very very abundant > 500");
  }
}
