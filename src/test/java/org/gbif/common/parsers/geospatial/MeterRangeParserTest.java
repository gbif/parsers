package org.gbif.common.parsers.geospatial;

import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.ParseResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MeterRangeParserTest {

  @Test
  public void testParseMeterRange() throws Exception {

  }

  @Test
  public void testParseElevation() throws Exception {
    assertResult(MeterRangeParser.parseElevation("10", "20", null), true, 15, 5);
    assertResult(MeterRangeParser.parseElevation("10", "20", "1"), true, 15, 6);
    assertResult(MeterRangeParser.parseElevation("10", "10", "1"), true, 10, 1);
    assertResult(MeterRangeParser.parseElevation("10", null, null), true, 10, null);
    assertResult(MeterRangeParser.parseElevation(null, "10", null), true, 10, null);
    assertResult(MeterRangeParser.parseElevation("-800", null, null), true, -800, null);
    assertResult(MeterRangeParser.parseElevation("-100", "-50", null), true, -75, 25);
    assertResult(MeterRangeParser.parseElevation("110", "115", null), true, 113, 3);
    assertResult(MeterRangeParser.parseElevation("10", "10", "1"), true, 10, 1);
    assertResult(MeterRangeParser.parseElevation("10", "10", "1"), true, 10, 1);
    assertResult(MeterRangeParser.parseElevation(null, "10000", "1"), true, 10000, 1);
    assertResult(MeterRangeParser.parseElevation("4061987", "4061987", null), false, null, null,
                 OccurrenceIssue.ELEVATION_UNLIKELY);

    // check units are removed
    assertResult(MeterRangeParser.parseElevation("1000m", null, "1"), true, 1000, 1,
                 OccurrenceIssue.ELEVATION_NON_NUMERIC);
    assertResult(MeterRangeParser.parseElevation("3280ft", null, "1"), true, 1000, 1,
                  OccurrenceIssue.ELEVATION_NOT_METRIC, OccurrenceIssue.ELEVATION_NON_NUMERIC);

    // check out of range
    assertResult(MeterRangeParser.parseElevation("100000000000", null, "1"), false, null, 1,
                 OccurrenceIssue.ELEVATION_UNLIKELY);

    // nonsense
    assertResult(MeterRangeParser.parseElevation("booya", "boom", "1"), false, null, 1,
                 OccurrenceIssue.ELEVATION_NON_NUMERIC);
  }

  @Test
  public void testParseDepth() {
    assertResult(MeterRangeParser.parseDepth("10", "20", null), true, 15, 5);
    assertResult(MeterRangeParser.parseDepth("10", "20", "1"), true, 15, 6);
    assertResult(MeterRangeParser.parseDepth("10", "10", "1"), true, 10, 1);
    assertResult(MeterRangeParser.parseDepth("10", null, null), true, 10, null);
    assertResult(MeterRangeParser.parseDepth(null, "10", null), true, 10, null);
    assertResult(MeterRangeParser.parseDepth("-800", null, null), true, 800, null, OccurrenceIssue.DEPTH_UNLIKELY);
    assertResult(MeterRangeParser.parseDepth("-100", "-50", null), true, 75, 25, OccurrenceIssue.DEPTH_UNLIKELY);
    assertResult(MeterRangeParser.parseDepth("110", "115", null), true, 113, 3);
    assertResult(MeterRangeParser.parseDepth("10", "10", "1"), true, 10, 1);
    assertResult(MeterRangeParser.parseDepth("10", "10", "1"), true, 10, 1);
    assertResult(MeterRangeParser.parseDepth(null, "10000", "1"), true, 10000, 1);
    assertResult(MeterRangeParser.parseDepth("4061987", "4061987", null), false, null, null,
                 OccurrenceIssue.DEPTH_UNLIKELY);

    // check units are removed
    assertResult(MeterRangeParser.parseDepth("1000m", null, "1"), true, 1000, 1,
                 OccurrenceIssue.DEPTH_NON_NUMERIC);
    assertResult(MeterRangeParser.parseDepth("3280ft", null, "1"), true, 1000, 1,
                 OccurrenceIssue.DEPTH_NOT_METRIC, OccurrenceIssue.DEPTH_NON_NUMERIC);

    // check out of range
    assertResult(MeterRangeParser.parseDepth("100000000000", null, "1"), false, null, 1,
                 OccurrenceIssue.DEPTH_UNLIKELY);

    // nonsense
    assertResult(MeterRangeParser.parseDepth("booya", "boom", "1"), false, null, 1,
                 OccurrenceIssue.DEPTH_NON_NUMERIC);
  }


  @Test
  public void testParseDistance() {
    assertResult(MeterRangeParser.parseSurfaceDistance("10", "20", null), true, 15, 5);
    assertResult(MeterRangeParser.parseSurfaceDistance("10", "20", "1"), true, 15, 6);
    assertResult(MeterRangeParser.parseSurfaceDistance("10", "10", "1"), true, 10, 1);
    assertResult(MeterRangeParser.parseSurfaceDistance("10", null, null), true, 10, null);
    assertResult(MeterRangeParser.parseSurfaceDistance(null, "10", null), true, 10, null);
    assertResult(MeterRangeParser.parseSurfaceDistance("-800", null, null), true, -800, null);
    assertResult(MeterRangeParser.parseSurfaceDistance("-100", "-50", null), true, -75, 25);
    assertResult(MeterRangeParser.parseSurfaceDistance("110", "115", null), true, 113, 3);
    assertResult(MeterRangeParser.parseSurfaceDistance("10", "10", "1"), true, 10, 1);
    assertResult(MeterRangeParser.parseSurfaceDistance("10", "10", "1"), true, 10, 1);
    assertResult(MeterRangeParser.parseSurfaceDistance(null, "10000", "1"), true, 10000, 1);
    assertResult(MeterRangeParser.parseSurfaceDistance("4061987", "4061987", null), false, null, null,
                 OccurrenceIssue.SURFACE_DISTANCE_UNLIKELY);

    // check units are removed
    assertResult(MeterRangeParser.parseSurfaceDistance("1000m", null, "1"), true, 1000, 1,
                 OccurrenceIssue.SURFACE_DISTANCE_NON_NUMERIC);
    assertResult(MeterRangeParser.parseSurfaceDistance("3280ft", null, "1"), true, 1000, 1,
                 OccurrenceIssue.SURFACE_DISTANCE_NOT_METRIC, OccurrenceIssue.SURFACE_DISTANCE_NON_NUMERIC);

    // check out of range
    assertResult(MeterRangeParser.parseSurfaceDistance("100000000000", null, "1"), false, null, 1,
                 OccurrenceIssue.SURFACE_DISTANCE_UNLIKELY);

    // nonsense
    assertResult(MeterRangeParser.parseSurfaceDistance("booya", "boom", "1"), false, null, 1,
                 OccurrenceIssue.SURFACE_DISTANCE_NON_NUMERIC);
  }


  private void assertResult(ParseResult<?> pr, boolean success, Integer elevation, Integer precision, OccurrenceIssue ... issue) {
    if (success) {
      assertExpected(pr, new IntAccuracy(elevation, precision), ParseResult.CONFIDENCE.DEFINITE, issue);
    } else {
      assertFailed(pr);
    }
  }

  private void assertExpected(ParseResult<?> pr, Object expected, ParseResult.CONFIDENCE c, OccurrenceIssue ... issue) {
    assertNotNull(pr);
    assertEquals(ParseResult.STATUS.SUCCESS, pr.getStatus());
    assertEquals(c, pr.getConfidence());
    assertNotNull(pr.getPayload());
    assertEquals(expected, pr.getPayload());
    if (issue == null) {
      assertTrue(pr.getIssues().isEmpty());
    } else {
      assertEquals("Issue mismatch, found issues: " + pr.getIssues(), issue.length, pr.getIssues().size());
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
