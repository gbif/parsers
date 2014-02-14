package org.gbif.common.parsers.geospatial;


import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.ParseResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CoordinateParseUtilsTest {

  @Test
  public void testParseLatLng() {
    assertExpected(CoordinateParseUtils.parseLatLng("10.3", "99.99"), new LatLng(10.3, 99.99), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("10", "10"), new LatLng(10, 10), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("90", "180"), new LatLng(90, 180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("-90", "180"), new LatLng(-90, 180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("90", "-180"), new LatLng(90, -180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("-90", "-180"), new LatLng(-90, -180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("0", "0"), new LatLng(0, 0), ParseResult.CONFIDENCE.POSSIBLE, OccurrenceIssue.ZERO_COORDINATE);

    // check swapped coords
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("100", "40"), OccurrenceIssue.PRESUMED_SWAPPED_COORDINATE);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("-100", "90"), OccurrenceIssue.PRESUMED_SWAPPED_COORDINATE);

    // check errors
    assertErrored(CoordinateParseUtils.parseLatLng("tim", "tom"));
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("200", "200"), OccurrenceIssue.COORDINATES_OUT_OF_RANGE);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("-200", "30"), OccurrenceIssue.COORDINATES_OUT_OF_RANGE);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("200", "30"), OccurrenceIssue.COORDINATES_OUT_OF_RANGE);
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
