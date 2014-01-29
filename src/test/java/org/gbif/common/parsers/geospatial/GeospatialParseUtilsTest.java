package org.gbif.common.parsers.geospatial;


import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.ParseResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GeospatialParseUtilsTest {

  @Test
  public void testParseLatLng() {
    assertExpected(GeospatialParseUtils.parseLatLng("10.3", "99.99"), new LatLng(10.3, 99.99), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("10", "10"), new LatLng(10, 10), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("90", "180"), new LatLng(90, 180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("-90", "180"), new LatLng(-90, 180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("90", "-180"), new LatLng(90, -180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("-90", "-180"), new LatLng(-90, -180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("0", "0"), new LatLng(0, 0), ParseResult.CONFIDENCE.POSSIBLE, OccurrenceIssue.ZERO_COORDINATE);

    // check swapped coords
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("100", "40"), OccurrenceIssue.PRESUMED_SWAPPED_COORDINATE);
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("-100", "90"), OccurrenceIssue.PRESUMED_SWAPPED_COORDINATE);

    // check errors
    assertErrored(GeospatialParseUtils.parseLatLng("tim", "tom"));
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("200", "200"), OccurrenceIssue.COORDINATES_OUT_OF_RANGE);
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("-200", "30"), OccurrenceIssue.COORDINATES_OUT_OF_RANGE);
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("200", "30"), OccurrenceIssue.COORDINATES_OUT_OF_RANGE);
  }

  @Test
  public void testParseDepth() {
    assertExpected(GeospatialParseUtils.parseDepth("10", "20", null), new IntPrecision(15, null), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseDepth("10", "20", "1"), new IntPrecision(15, 1), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseDepth("10", "10", "1"), new IntPrecision(10, 1), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseDepth("10", null, "1"), new IntPrecision(10, 1), ParseResult.CONFIDENCE.DEFINITE);

    // check units are removed
    assertExpected(GeospatialParseUtils.parseDepth("10m", null, "1"),
      new IntPrecision(10, 1), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.DEPTH_NON_NUMERIC);
    assertExpected(GeospatialParseUtils.parseDepth("3.27ft", null, "1"),
      new IntPrecision(1, 1), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.DEPTH_PRESUMED_IN_FEET, OccurrenceIssue.DEPTH_NON_NUMERIC);

    // check out of range
    assertExpected(GeospatialParseUtils.parseDepth("100000000", null, "1"),
      new IntPrecision(null, null), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.DEPTH_OUT_OF_RANGE);

    // nonsense
    assertFailed(GeospatialParseUtils.parseDepth("booya", "boom", "1"));
  }

  @Test
  public void testParseAltitude() {
    assertExpected(GeospatialParseUtils.parseAltitude("10", "20", null), new IntPrecision(15, null),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("10", "20", "1"), new IntPrecision(15, 1),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("10", "10", "1"), new IntPrecision(10, 1),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("10", null, "1"), new IntPrecision(10, 1),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude(null, "10000", "1"), new IntPrecision(10000, 1),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("4061987", "4061987", null),
      new IntPrecision(null, null), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.ALTITUDE_OUT_OF_RANGE);

    // check units are removed
    assertExpected(GeospatialParseUtils.parseAltitude("1000m", null, "1"),
      new IntPrecision(1000, 1),ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.ALTITUDE_NON_NUMERIC);
    assertExpected(GeospatialParseUtils.parseAltitude("3280ft", null, "1"),
      new IntPrecision(1000, 1), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.ALTITUDE_PRESUMED_IN_FEET, OccurrenceIssue.ALTITUDE_NON_NUMERIC);

    // check out of range
    assertExpected(GeospatialParseUtils.parseAltitude("100000000000", null, "1"),
                   new IntPrecision(null, 1), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.ALTITUDE_OUT_OF_RANGE);

    // nonsense
    assertFailed(GeospatialParseUtils.parseAltitude("booya", "boom", "1"));
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
      assertEquals(issue.length, pr.getIssues().size());
      for (OccurrenceIssue iss : issue) {
        assertTrue(pr.getIssues().contains(iss));
      }
    }
  }

  private void assertErrored(ParseResult<?> pr) {
    assertNotNull(pr);
    assertEquals(ParseResult.STATUS.ERROR, pr.getStatus());
  }

  private void assertFailed(ParseResult<?> pr) {
    assertNotNull(pr);
    assertEquals(ParseResult.STATUS.FAIL, pr.getStatus());
  }

  private void assertFailedWithIssues(ParseResult<LatLng> pr, OccurrenceIssue issue) {
    assertFailed(pr);
    assertEquals(1, pr.getIssues().size());
    assertTrue(pr.getIssues().contains(issue));
  }
}
