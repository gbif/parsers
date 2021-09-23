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
package org.gbif.common.parsers.geospatial;

import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.gbif.common.parsers.core.ParseResult;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MeterRangeParserTest {

  @Test
  public void testParseMeters() {
    assertFalse(MeterRangeParser.parseMeters("Somewhere between (1-2m) or (3-6ft)").isSuccessful());
  }

  @Test
  public void testParseElevation() {
    assertResult(MeterRangeParser.parseElevation("10", "20", null), true, 15d, 5d);
    assertResult(MeterRangeParser.parseElevation("10", "20", "1"), true, 15d, 6d);
    assertResult(MeterRangeParser.parseElevation("10", "10", "1"), true, 10d, 1d);
    assertResult(MeterRangeParser.parseElevation("10", null, null), true, 10d, null);
    assertResult(MeterRangeParser.parseElevation(null, "10", null), true, 10d, null);
    assertResult(MeterRangeParser.parseElevation("-800", null, null), true, -800d, null);
    assertResult(MeterRangeParser.parseElevation("-100", "-50", null), true, -75d, 25d);
    assertResult(MeterRangeParser.parseElevation("110", "115", null), true, 112.5d, 2.5d);
    assertResult(MeterRangeParser.parseElevation("10", "10", "1"), true, 10d, 1d);
    assertResult(MeterRangeParser.parseElevation("10.0", "12.5", "1"), true, 11.25d, 2.25d);
    // European commas are ambiguous, see issue 23.
    assertResult(MeterRangeParser.parseElevation("10,0", "12,5", "1"), true, 11.25d, 2.25d);
    assertResult(MeterRangeParser.parseElevation(null, "10000", "1"), true, 10000d, 1d);
    assertResult(
        MeterRangeParser.parseElevation("4061987", "4061987", null),
        false,
        null,
        null,
        OccurrenceIssue.ELEVATION_UNLIKELY);

    // check units are removed
    assertResult(
        MeterRangeParser.parseElevation("1000m", null, "1"),
        true,
        1000d,
        1d,
        OccurrenceIssue.ELEVATION_NON_NUMERIC);
    assertResult(
        MeterRangeParser.parseElevation("3280ft", null, "1"),
        true,
        999.74,
        1d,
        OccurrenceIssue.ELEVATION_NOT_METRIC,
        OccurrenceIssue.ELEVATION_NON_NUMERIC);

    // check out of range
    assertResult(
        MeterRangeParser.parseElevation("100000000000", null, "1"),
        false,
        null,
        1d,
        OccurrenceIssue.ELEVATION_UNLIKELY);

    // nonsense
    assertResult(
        MeterRangeParser.parseElevation("booya", "boom", "1"),
        false,
        null,
        1d,
        OccurrenceIssue.ELEVATION_NON_NUMERIC);
  }

  @Test
  public void testParseDepth() {
    assertResult(MeterRangeParser.parseDepth("10", "20", null), true, 15d, 5d);
    assertResult(MeterRangeParser.parseDepth("10", "20", "1"), true, 15d, 6d);
    assertResult(MeterRangeParser.parseDepth("10", "10", "1"), true, 10d, 1d);
    assertResult(MeterRangeParser.parseDepth("10", null, null), true, 10d, null);
    assertResult(MeterRangeParser.parseDepth(null, "10", null), true, 10d, null);
    assertResult(
        MeterRangeParser.parseDepth("-800", null, null),
        true,
        800d,
        null,
        OccurrenceIssue.DEPTH_UNLIKELY);
    assertResult(
        MeterRangeParser.parseDepth("-100", "-50", null),
        true,
        75d,
        25d,
        OccurrenceIssue.DEPTH_UNLIKELY);
    assertResult(MeterRangeParser.parseDepth("110", "115", null), true, 112.5d, 2.5d);
    assertResult(MeterRangeParser.parseDepth("10", "10", "1"), true, 10d, 1d);
    assertResult(MeterRangeParser.parseDepth("10", "10", "1"), true, 10d, 1d);
    assertResult(MeterRangeParser.parseDepth(null, "10000", "1"), true, 10000d, 1d);
    assertResult(
        MeterRangeParser.parseDepth("4061987", "4061987", null),
        false,
        null,
        null,
        OccurrenceIssue.DEPTH_UNLIKELY);

    // check units are removed
    assertResult(
        MeterRangeParser.parseDepth("1000m", null, "1"),
        true,
        1000d,
        1d,
        OccurrenceIssue.DEPTH_NON_NUMERIC);
    assertResult(
        MeterRangeParser.parseDepth("3280ft", null, "1"),
        true,
        999.74,
        1d,
        OccurrenceIssue.DEPTH_NOT_METRIC,
        OccurrenceIssue.DEPTH_NON_NUMERIC);
    assertResult(
        MeterRangeParser.parseDepth("3280feet", null, "1"),
        true,
        999.74,
        1d,
        OccurrenceIssue.DEPTH_NOT_METRIC,
        OccurrenceIssue.DEPTH_NON_NUMERIC);
    assertResult(
        MeterRangeParser.parseDepth("30 In", null, "1"),
        true,
        0.76,
        1d,
        OccurrenceIssue.DEPTH_NOT_METRIC,
        OccurrenceIssue.DEPTH_NON_NUMERIC);
    assertResult(
        MeterRangeParser.parseDepth("30 inches", null, "1"),
        true,
        0.76,
        1d,
        OccurrenceIssue.DEPTH_NOT_METRIC,
        OccurrenceIssue.DEPTH_NON_NUMERIC);
    assertResult(
        MeterRangeParser.parseDepth("3km", null, "1"),
        true,
        3000d,
        1d,
        OccurrenceIssue.DEPTH_NON_NUMERIC);
    assertResult(
        MeterRangeParser.parseDepth("3kilometres", null, "1"),
        true,
        3000d,
        1d,
        OccurrenceIssue.DEPTH_NON_NUMERIC);
    assertResult(
        MeterRangeParser.parseDepth("30 fm", null, "1 fm"),
        true,
        54.86,
        1.83d,
        OccurrenceIssue.DEPTH_NOT_METRIC,
        OccurrenceIssue.DEPTH_NON_NUMERIC);

    // check out of range
    assertResult(
        MeterRangeParser.parseDepth("100000000000", null, "1"),
        false,
        null,
        1d,
        OccurrenceIssue.DEPTH_UNLIKELY);

    // unknown unit assumed to be metres
    assertResult(
        MeterRangeParser.parseDepth("30 xyz", null, "1"),
        true,
        30.0,
        1d,
        OccurrenceIssue.DEPTH_NON_NUMERIC);

    // ambiguous unit (feet or fathoms)
    assertResult(
      MeterRangeParser.parseDepth("3280f", null, "1"),
      true,
      3280.0,
      1d,
      OccurrenceIssue.DEPTH_NON_NUMERIC);

    // nonsense
    assertResult(
        MeterRangeParser.parseDepth("booya", "boom", "1"),
        false,
        null,
        1d,
        OccurrenceIssue.DEPTH_NON_NUMERIC);
  }

  private void assertResult(
      OccurrenceParseResult<?> pr,
      boolean success,
      Double elevation,
      Double precision,
      OccurrenceIssue... issue) {
    if (success) {
      assertExpected(
          pr, new DoubleAccuracy(elevation, precision), ParseResult.CONFIDENCE.DEFINITE, issue);
    } else {
      assertFailed(pr);
    }
  }

  private void assertExpected(
      OccurrenceParseResult<?> pr,
      Object expected,
      ParseResult.CONFIDENCE c,
      OccurrenceIssue... issue) {
    assertNotNull(pr);
    assertEquals(ParseResult.STATUS.SUCCESS, pr.getStatus());
    assertEquals(c, pr.getConfidence());
    assertNotNull(pr.getPayload());
    assertEquals(expected, pr.getPayload());
    if (issue == null) {
      assertTrue(pr.getIssues().isEmpty());
    } else {
      assertEquals(
          issue.length, pr.getIssues().size(), "Issue mismatch, found issues: " + pr.getIssues());
      for (OccurrenceIssue iss : issue) {
        assertTrue(pr.getIssues().contains(iss));
      }
    }
  }

  private void assertFailed(ParseResult<?> pr) {
    assertNotNull(pr);
    assertEquals(ParseResult.STATUS.FAIL, pr.getStatus());
  }
}
