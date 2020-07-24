package org.gbif.common.parsers.geospatial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.gbif.common.parsers.core.ParseResult;
import org.junit.Test;

public class MeterRangeParserTest {

  @Test
  public void testParseMeterRange() throws Exception {}

  @Test
  public void testParseElevation() throws Exception {
    assertResult(MeterRangeParser.parseElevation("10", "20", null), true, 15d, 5d);
    assertResult(MeterRangeParser.parseElevation("10", "20", "1"), true, 15d, 6d);
    assertResult(MeterRangeParser.parseElevation("10", "10", "1"), true, 10d, 1d);
    assertResult(MeterRangeParser.parseElevation("10", null, null), true, 10d, null);
    assertResult(MeterRangeParser.parseElevation(null, "10", null), true, 10d, null);
    assertResult(MeterRangeParser.parseElevation("-800", null, null), true, -800d, null);
    assertResult(MeterRangeParser.parseElevation("-100", "-50", null), true, -75d, 25d);
    assertResult(MeterRangeParser.parseElevation("110", "115", null), true, 112.5d, 2.5d);
    assertResult(MeterRangeParser.parseElevation("10", "10", "1"), true, 10d, 1d);
    assertResult(MeterRangeParser.parseElevation("10", "10", "1"), true, 10d, 1d);
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
        MeterRangeParser.parseDepth("3280f", null, "1"),
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
        OccurrenceIssue.DEPTH_NOT_METRIC,
        OccurrenceIssue.DEPTH_NON_NUMERIC);
    assertResult(
        MeterRangeParser.parseDepth("3kilometres", null, "1"),
        true,
        3000d,
        1d,
        OccurrenceIssue.DEPTH_NOT_METRIC,
        OccurrenceIssue.DEPTH_NON_NUMERIC);

    // check out of range
    assertResult(
        MeterRangeParser.parseDepth("100000000000", null, "1"),
        false,
        null,
        1d,
        OccurrenceIssue.DEPTH_UNLIKELY);

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
          "Issue mismatch, found issues: " + pr.getIssues(), issue.length, pr.getIssues().size());
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
