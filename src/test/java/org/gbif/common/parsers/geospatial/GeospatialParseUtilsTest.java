package org.gbif.common.parsers.geospatial;


import org.gbif.common.parsers.LongPrecisionStatus;
import org.gbif.common.parsers.ParseResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GeospatialParseUtilsTest {

  @Test
  public void testParseLatLng() {
    assertExpected(GeospatialParseUtils.parseLatLng("10.3", "99.99"), new LatLngStatus(10.3, 99.99),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("10", "10"), new LatLngStatus(10, 10),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("90", "180"), new LatLngStatus(90, 180),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("-90", "180"), new LatLngStatus(-90, 180),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("90", "-180"), new LatLngStatus(90, -180),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("-90", "-180"), new LatLngStatus(-90, -180),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("0", "0"), new LatLngStatus(0, 0, GeospatialIssue.ZERO_COORDINATES),
      ParseResult.CONFIDENCE.POSSIBLE);

    // check swapped coords
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("100", "40"),
      GeospatialIssue.PRESUMED_INVERTED_COORDINATES);
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("-100", "90"),
      GeospatialIssue.PRESUMED_INVERTED_COORDINATES);

    // check errors
    assertErrored(GeospatialParseUtils.parseLatLng("tim", "tom"));
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("200", "200"), GeospatialIssue.COORDINATES_OUT_OF_RANGE);
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("-200", "30"), GeospatialIssue.COORDINATES_OUT_OF_RANGE);
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("200", "30"), GeospatialIssue.COORDINATES_OUT_OF_RANGE);
  }

  @Test
  public void testParseDepth() {
    assertExpected(GeospatialParseUtils.parseDepth("10", "20", null), new LongPrecisionStatus(1500l, null, 0),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseDepth("10", "20", "1"), new LongPrecisionStatus(1500l, 100l, 0),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseDepth("10", "10", "1"), new LongPrecisionStatus(1000l, 100l, 0),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseDepth("10", null, "1"), new LongPrecisionStatus(1000l, 100l, 0),
      ParseResult.CONFIDENCE.DEFINITE);

    // check units are removed
    assertExpected(GeospatialParseUtils.parseDepth("10m", null, "1"),
      new LongPrecisionStatus(1000l, 100l, DepthIssue.GEOSPATIAL_PRESUMED_DEPTH_NON_NUMERIC.getIssueCode()),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseDepth("3.27ft", null, "1"), new LongPrecisionStatus(100l, 100l,
      DepthIssue.GEOSPATIAL_PRESUMED_DEPTH_IN_FEET.getIssueCode() | DepthIssue.GEOSPATIAL_PRESUMED_DEPTH_NON_NUMERIC
        .getIssueCode()), ParseResult.CONFIDENCE.DEFINITE);

    // check out of range
    assertExpected(GeospatialParseUtils.parseDepth("100000000", null, "1"),
      new LongPrecisionStatus(null, null, DepthIssue.GEOSPATIAL_DEPTH_OUT_OF_RANGE.getIssueCode()),
      ParseResult.CONFIDENCE.DEFINITE);

    // nonsense
    assertFailed(GeospatialParseUtils.parseDepth("booya", "boom", "1"));
  }

  @Test
  public void testParseAltitude() {
    assertExpected(GeospatialParseUtils.parseAltitude("10", "20", null), new LongPrecisionStatus(15L, null, 0),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("10", "20", "1"), new LongPrecisionStatus(15L, 1L, 0),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("10", "10", "1"), new LongPrecisionStatus(10L, 1L, 0),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("10", null, "1"), new LongPrecisionStatus(10L, 1L, 0),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude(null, "10000", "1"), new LongPrecisionStatus(10000L, 1L, 0),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("4061987", "4061987", null),
      new LongPrecisionStatus(null, null, AltitudeIssue.GEOSPATIAL_ALTITUDE_OUT_OF_RANGE.getIssueCode()),
      ParseResult.CONFIDENCE.DEFINITE);

    // check units are removed
    assertExpected(GeospatialParseUtils.parseAltitude("1000m", null, "1"),
      new LongPrecisionStatus(1000L, 1L, AltitudeIssue.GEOSPATIAL_PRESUMED_ALTITUDE_NON_NUMERIC.getIssueCode()),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("3280ft", null, "1"), new LongPrecisionStatus(1000L, 1L,
      AltitudeIssue.GEOSPATIAL_PRESUMED_ALTITUDE_IN_FEET.getIssueCode() | AltitudeIssue
        .GEOSPATIAL_PRESUMED_ALTITUDE_NON_NUMERIC.getIssueCode()), ParseResult.CONFIDENCE.DEFINITE);

    // check out of range
    assertExpected(GeospatialParseUtils.parseAltitude("100000000000", null, "1"),
      new LongPrecisionStatus(null, 1L, AltitudeIssue.GEOSPATIAL_ALTITUDE_OUT_OF_RANGE.getIssueCode()),
      ParseResult.CONFIDENCE.DEFINITE);

    // nonsense
    assertFailed(GeospatialParseUtils.parseAltitude("booya", "boom", "1"));
  }

  private void assertExpected(ParseResult<?> pr, Object expected, ParseResult.CONFIDENCE c) {
    assertNotNull(pr);
    assertEquals(ParseResult.STATUS.SUCCESS, pr.getStatus());
    assertEquals(c, pr.getConfidence());
    assertNotNull(pr.getPayload());
    assertEquals(expected, pr.getPayload());
  }

  private void assertErrored(ParseResult<?> pr) {
    assertNotNull(pr);
    assertEquals(ParseResult.STATUS.ERROR, pr.getStatus());
  }

  private void assertFailed(ParseResult<?> pr) {
    assertNotNull(pr);
    assertEquals(ParseResult.STATUS.FAIL, pr.getStatus());
  }

  private void assertFailedWithIssues(ParseResult<LatLngStatus> pr, GeospatialIssue issue) {
    assertFailed(pr);
    assertNotNull(pr.getPayload().getIssue());
    assertEquals(issue, pr.getPayload().getIssue());
  }
}
