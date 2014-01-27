package org.gbif.common.parsers.geospatial;


import org.gbif.api.vocabulary.OccurrenceValidationRule;
import org.gbif.common.parsers.ParseResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GeospatialParseUtilsTest {

  @Test
  public void testParseLatLng() {
    assertExpected(GeospatialParseUtils.parseLatLng("10.3", "99.99"), new LatLngIssue(10.3, 99.99),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("10", "10"), new LatLngIssue(10, 10),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("90", "180"), new LatLngIssue(90, 180),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("-90", "180"), new LatLngIssue(-90, 180),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("90", "-180"), new LatLngIssue(90, -180),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("-90", "-180"), new LatLngIssue(-90, -180),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseLatLng("0", "0"), new LatLngIssue(0, 0, OccurrenceValidationRule.ZERO_COORDINATE),
      ParseResult.CONFIDENCE.POSSIBLE);

    // check swapped coords
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("100", "40"),
                           OccurrenceValidationRule.PRESUMED_SWAPPED_COORDINATE);
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("-100", "90"),
                           OccurrenceValidationRule.PRESUMED_SWAPPED_COORDINATE);

    // check errors
    assertErrored(GeospatialParseUtils.parseLatLng("tim", "tom"));
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("200", "200"), OccurrenceValidationRule.COORDINATES_OUT_OF_RANGE);
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("-200", "30"), OccurrenceValidationRule.COORDINATES_OUT_OF_RANGE);
    assertFailedWithIssues(GeospatialParseUtils.parseLatLng("200", "30"), OccurrenceValidationRule.COORDINATES_OUT_OF_RANGE);
  }

  @Test
  public void testParseDepth() {
    assertExpected(GeospatialParseUtils.parseDepth("10", "20", null), new LongPrecisionIssue(1500l, null),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseDepth("10", "20", "1"), new LongPrecisionIssue(1500l, 100l),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseDepth("10", "10", "1"), new LongPrecisionIssue(1000l, 100l),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseDepth("10", null, "1"), new LongPrecisionIssue(1000l, 100l),
      ParseResult.CONFIDENCE.DEFINITE);

    // check units are removed
    assertExpected(GeospatialParseUtils.parseDepth("10m", null, "1"),
      new LongPrecisionIssue(1000l, 100l, OccurrenceValidationRule.DEPTH_NON_NUMERIC),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseDepth("3.27ft", null, "1"),
      new LongPrecisionIssue(100l, 100l,OccurrenceValidationRule.DEPTH_PRESUMED_IN_FEET, OccurrenceValidationRule.DEPTH_NON_NUMERIC),
      ParseResult.CONFIDENCE.DEFINITE);

    // check out of range
    assertExpected(GeospatialParseUtils.parseDepth("100000000", null, "1"),
      new LongPrecisionIssue(null, null, OccurrenceValidationRule.DEPTH_OUT_OF_RANGE),
      ParseResult.CONFIDENCE.DEFINITE);

    // nonsense
    assertFailed(GeospatialParseUtils.parseDepth("booya", "boom", "1"));
  }

  @Test
  public void testParseAltitude() {
    assertExpected(GeospatialParseUtils.parseAltitude("10", "20", null), new LongPrecisionIssue(15L, null),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("10", "20", "1"), new LongPrecisionIssue(15L, 1L),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("10", "10", "1"), new LongPrecisionIssue(10L, 1L),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("10", null, "1"), new LongPrecisionIssue(10L, 1L),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude(null, "10000", "1"), new LongPrecisionIssue(10000L, 1L),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("4061987", "4061987", null),
      new LongPrecisionIssue(null, null, OccurrenceValidationRule.ALTITUDE_OUT_OF_RANGE),
      ParseResult.CONFIDENCE.DEFINITE);

    // check units are removed
    assertExpected(GeospatialParseUtils.parseAltitude("1000m", null, "1"),
      new LongPrecisionIssue(1000L, 1L, OccurrenceValidationRule.ALTITUDE_NON_NUMERIC),
      ParseResult.CONFIDENCE.DEFINITE);
    assertExpected(GeospatialParseUtils.parseAltitude("3280ft", null, "1"),
      new LongPrecisionIssue(1000L, 1L, OccurrenceValidationRule.ALTITUDE_PRESUMED_IN_FEET, OccurrenceValidationRule.ALTITUDE_NON_NUMERIC),
      ParseResult.CONFIDENCE.DEFINITE);

    // check out of range
    assertExpected(GeospatialParseUtils.parseAltitude("100000000000", null, "1"),
      new LongPrecisionIssue(null, 1L, OccurrenceValidationRule.ALTITUDE_OUT_OF_RANGE),
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

  private void assertFailedWithIssues(ParseResult<LatLngIssue> pr, OccurrenceValidationRule issue) {
    assertFailed(pr);
    assertNotNull(pr.getPayload().getIssue());
    assertEquals(issue, pr.getPayload().getIssue());
  }
}
