package org.gbif.common.parsers.geospatial;

import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.ParseResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CoordinateParseUtilsTest {

  @Test
  public void testParseLatLng() {
    assertExpected(CoordinateParseUtils.parseLatLng("-46,33", "51,8717"), new LatLng(-46.33, 51.8717), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("10.3", "99.99"), new LatLng(10.3, 99.99), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("10", "10"), new LatLng(10, 10), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("90", "180"), new LatLng(90, 180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("-90", "180"), new LatLng(-90, 180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("90", "-180"), new LatLng(90, -180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("-90", "-180"), new LatLng(-90, -180), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("0", "0"), new LatLng(0, 0), ParseResult.CONFIDENCE.POSSIBLE, OccurrenceIssue.ZERO_COORDINATE);

    // rounding
    assertExpected(CoordinateParseUtils.parseLatLng("2.123450678", "-8.123450678"), new LatLng(2.12345, -8.12345), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.COORDINATE_ROUNDED);
    assertExpected(CoordinateParseUtils.parseLatLng("2.12345", "-8.123450678"), new LatLng(2.12345, -8.12345), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.COORDINATE_ROUNDED);
    assertExpected(CoordinateParseUtils.parseLatLng("2.12345", "-8.12345"), new LatLng(2.12345, -8.12345), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("2.12345000", "-8.123450"), new LatLng(2.12345, -8.12345), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("2.123", "-8.1234506"), new LatLng(2.123, -8.12345), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.COORDINATE_ROUNDED);

    // degree minutes seconds
    assertExpected(CoordinateParseUtils.parseLatLng("02° 49' 52\" N", "131° 47' 03\" E"), new LatLng(2.83111d, 131.78417d), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.COORDINATE_ROUNDED);

    // check swapped coords
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("100", "40"), OccurrenceIssue.PRESUMED_SWAPPED_COORDINATE);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("-100", "90"), OccurrenceIssue.PRESUMED_SWAPPED_COORDINATE);

    // check errors
    assertFailed(CoordinateParseUtils.parseLatLng("", "30"));
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("tim", "tom"), OccurrenceIssue.COORDINATE_INVALID);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("20,432,12", "13,4"), OccurrenceIssue.COORDINATE_INVALID);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("200", "200"), OccurrenceIssue.COORDINATE_OUT_OF_RANGE);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("-200", "30"), OccurrenceIssue.COORDINATE_OUT_OF_RANGE);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("200", "30"), OccurrenceIssue.COORDINATE_OUT_OF_RANGE);
  }

  @Test
  public void testParseDMS() {
    assertExpected( CoordinateParseUtils.parseDMS("02° 49' 52\" N", "131° 47' 03\" E"), 2.831111111111111d, 131.78416666666666d);
    assertExpected( CoordinateParseUtils.parseDMS("2°49'52\"S", "131°47'03\" W"), -2.831111111111111d, -131.78416666666666d);
    assertExpected( CoordinateParseUtils.parseDMS("2°49'52\"  n", "131°47'03\"  O"), 2.831111111111111d, 131.78416666666666d);
    // failed
    assertNull(CoordinateParseUtils.parseDMS("12344", "432"));
    assertNull(CoordinateParseUtils.parseDMS(" ", " "));
    assertNull(CoordinateParseUtils.parseDMS("2°49'52\"N", "131°47'03\""));
    assertNull(CoordinateParseUtils.parseDMS("122°49'52\"N", "131°47'03\"E"));
  }

  private void assertExpected(LatLng result, double lat, double lon) {
    assertNotNull(result);
    assertEquals(0, result.getLat().compareTo(lat));
    assertEquals(0, result.getLng().compareTo(lon));
  }

  private void assertExpected(ParseResult<?> pr, Object expected, ParseResult.CONFIDENCE c, OccurrenceIssue ... issue) {
    assertNotNull(pr);
    assertEquals(ParseResult.STATUS.SUCCESS, pr.getStatus());
    assertEquals(c, pr.getConfidence());
    assertNotNull(pr.getPayload());
    assertEquals(expected, pr.getPayload());
    //System.out.println(Lists.newArrayList(issue));
    if (issue == null) {
      assertTrue(pr.getIssues().isEmpty());
    } else {
      assertEquals(issue.length, pr.getIssues().size());
      for (OccurrenceIssue iss : issue) {
        assertTrue(pr.getIssues().contains(iss));
      }
    }
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
