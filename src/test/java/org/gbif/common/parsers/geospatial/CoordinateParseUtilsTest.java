package org.gbif.common.parsers.geospatial;

import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.gbif.common.parsers.core.ParseResult;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    assertExpected(CoordinateParseUtils.parseLatLng("2.123450678", "-8.123450678"), new LatLng(2.123451, -8.123451), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.COORDINATE_ROUNDED);
    assertExpected(CoordinateParseUtils.parseLatLng("2.123451", "-8.123450678"), new LatLng(2.123451, -8.123451), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.COORDINATE_ROUNDED);
    assertExpected(CoordinateParseUtils.parseLatLng("2.123451", "-8.123451"), new LatLng(2.123451, -8.123451), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("2.12345100", "-8.1234510"), new LatLng(2.123451, -8.123451), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(CoordinateParseUtils.parseLatLng("2.123", "-8.1234506"), new LatLng(2.123, -8.123451), ParseResult.CONFIDENCE.DEFINITE, OccurrenceIssue.COORDINATE_ROUNDED);

    // degree minutes seconds
    assertExpected(CoordinateParseUtils.parseLatLng("02° 49' 52\" N", "131° 47' 03\" E"), new LatLng(2.831111d, 131.784167d), ParseResult.CONFIDENCE.DEFINITE);

    // check swapped coordinates
    assertExpected(CoordinateParseUtils.parseLatLng("100", "40"), new LatLng(40, 100), ParseResult.CONFIDENCE.PROBABLE, OccurrenceIssue.PRESUMED_SWAPPED_COORDINATE);
    assertExpected(CoordinateParseUtils.parseLatLng("-100", "90"), new LatLng(90, -100), ParseResult.CONFIDENCE.PROBABLE, OccurrenceIssue.PRESUMED_SWAPPED_COORDINATE);

    // check errors
    assertFailed(CoordinateParseUtils.parseLatLng("", "30"));
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("tim", "tom"), OccurrenceIssue.COORDINATE_INVALID);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("20,432,12", "13,4"), OccurrenceIssue.COORDINATE_INVALID);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("200", "200"), OccurrenceIssue.COORDINATE_OUT_OF_RANGE);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("-200", "30"), OccurrenceIssue.COORDINATE_OUT_OF_RANGE);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("200", "30"), OccurrenceIssue.COORDINATE_OUT_OF_RANGE);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("20.432,12", "13,4"), OccurrenceIssue.COORDINATE_OUT_OF_RANGE);
    assertFailedWithIssues(CoordinateParseUtils.parseLatLng("20,432,12", "13,4"), OccurrenceIssue.COORDINATE_INVALID);
  }

  @Test
  public void testParseDMS() {
    assertDMS("2°49'N", "131°47'E", 2.816667d, 131.783333d);

    assertDMS("02° 49' 52\" N", "131° 47' 03\" E", 2.831111d, 131.784167d);
    assertDMS("2°49'52\"S", "131°47'03\" W", -2.831111d, -131.784167d);
    assertDMS("2°49'52\"  n", "131°47'03\"  O", 2.831111d, 131.784167d);
    assertDMS("002°49'52\"N", "131°47'03\"E", 2.831111d, 131.784167d);
    assertDMS("2°49'N", "131°47'E", 2.816667d, 131.783333d);
    assertDMS("002°49'52''N", "131°47'03''E", 2.831111d, 131.784167d);
    // even if its out of range thats expected here - we validate elsewhere, this is an internal method only!
    assertDMS("122°49'52\"N", "131°47'03\"E", 122.831111d, 131.784167d);
    assertDMS("122°49'52.39\"N", "131°47'03.23\"E", 122.83121944444444d, 131.78423055555555d);
    assertDMS("122d49m52.39sN", "131d47m03.23sE", 122.83121944444444d, 131.78423055555555d);
    assertDMS("122 49 52.39", "131 47 03.23", 122.83121944444444d, 131.78423055555555d);
    assertDMS("N122°49'52.39\"", "E131°47'03.23\"", 122.83121944444444d, 131.78423055555555d);
    assertDMS("N 122° 49' 52.39\"", "E 131° 47' 03.23\"", 122.83121944444444d, 131.78423055555555d);

    // truly failing
    assertIllegalArg("12344", true);
    assertIllegalArg("432", false);
    assertIllegalArg(" ", true);
    assertIllegalArg(" ", false);
    assertIllegalArg("131°47'132\"", false);
    assertIllegalArg("131 47 132", false);
    assertIllegalArg("131°47'132\"", false);
  }

  private void assertDMS(String lat, String lon, double eLat, double eLon){
    assertEquals(eLat, CoordinateParseUtils.parseDMS(lat, true), 0.000001);
    assertEquals(eLon, CoordinateParseUtils.parseDMS(lon, false), 0.000001);
  }

  private void assertIllegalArg(String coord, boolean lat){
    try {
      CoordinateParseUtils.parseDMS(coord, lat);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
      // expected!
    }
  }

  @Test
  public void testParseVerbatimCoordinates() {
    assertExpected( CoordinateParseUtils.parseVerbatimCoordinates("02° 49' 52\" N 131° 47' 03\" E"), new LatLng(2.831111d, 131.784167d), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected( CoordinateParseUtils.parseVerbatimCoordinates("02° 49' 52\" N, 131° 47' 03\" E"), new LatLng(2.831111d, 131.784167d), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected( CoordinateParseUtils.parseVerbatimCoordinates("02°49'52\"N; 131°47'03\"O"), new LatLng(2.831111d, 131.784167d), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected( CoordinateParseUtils.parseVerbatimCoordinates("17d 33m 5s N/99d 30m 3s W"), new LatLng(17.551389d, -99.500833d), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected( CoordinateParseUtils.parseVerbatimCoordinates("14.93333/-91.9"), new LatLng(14.93333d, -91.9d), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected( CoordinateParseUtils.parseVerbatimCoordinates("63d 41m 39s N 170d 28m 44s W"), new LatLng(63.694167d, -170.478889d), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected( CoordinateParseUtils.parseVerbatimCoordinates("37° 28' N, 122° 6' W"), new LatLng(37.466667d, -122.1d), ParseResult.CONFIDENCE.DEFINITE);
    assertExpected( CoordinateParseUtils.parseVerbatimCoordinates("2°49'52\"N, 131°47'03\""), new LatLng(2.831111d, 131.784167d), ParseResult.CONFIDENCE.DEFINITE);
    // failed
    assertFailed(CoordinateParseUtils.parseVerbatimCoordinates(""));
    assertFailedWithIssues(CoordinateParseUtils.parseVerbatimCoordinates("12344"), OccurrenceIssue.COORDINATE_INVALID);
    assertFailedWithIssues(CoordinateParseUtils.parseVerbatimCoordinates(" "), OccurrenceIssue.COORDINATE_INVALID);
    assertFailedWithIssues(CoordinateParseUtils.parseVerbatimCoordinates(",11.12"), OccurrenceIssue.COORDINATE_INVALID);
    assertFailedWithIssues(CoordinateParseUtils.parseVerbatimCoordinates("122°49'52\"N, 131°47'03\"E"), OccurrenceIssue.COORDINATE_OUT_OF_RANGE);
  }

  private void assertExpected(LatLng result, Double lat, Double lon) {
    assertNotNull(result);
    assertEquals("Latitudedifferent", lat, result.getLat());
    assertEquals("Longitude different", lon, result.getLng());
  }

  private void assertExpected(OccurrenceParseResult<?> pr, Object expected, ParseResult.CONFIDENCE c, OccurrenceIssue ... issue) {
    assertNotNull(pr);
    assertEquals(ParseResult.STATUS.SUCCESS, pr.getStatus());
    assertEquals(c, pr.getConfidence());
    assertNotNull(pr.getPayload());
    assertEquals(expected, pr.getPayload());
    //System.out.println(Lists.newArrayList(issue));
    if (issue == null) {
      assertTrue(pr.getIssues().isEmpty());
    } else {
      assertEquals("Wrong number of issues. Found " + pr.getIssues() + ", expected " + Lists.newArrayList(issue), issue.length, pr.getIssues().size());
      for (OccurrenceIssue iss : issue) {
        assertTrue(pr.getIssues().contains(iss));
      }
    }
  }

  private void assertFailed(ParseResult<?> pr) {
    assertNotNull(pr);
    assertEquals(ParseResult.STATUS.FAIL, pr.getStatus());
  }

  private void assertFailedWithIssues(OccurrenceParseResult<LatLng> pr, OccurrenceIssue ... issue) {
    assertFailed(pr);
    assertEquals(issue.length, pr.getIssues().size());
    for (OccurrenceIssue iss : issue){
      assertTrue(pr.getIssues().contains(iss));
    }
  }
}
