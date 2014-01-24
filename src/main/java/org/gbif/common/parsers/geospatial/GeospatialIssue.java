package org.gbif.common.parsers.geospatial;

/**
 * This enumerates all the possible issues we can detect with latitude and longitude pairs.
 */
public enum GeospatialIssue {

  NO_ISSUES(0x00),

  /**
   * Set if latitude appears to have the wrong sign
   */
  PRESUMED_NEGATED_LATITUDE(0x01),

  /**
   * Set if longitude appears to have the wrong sign
   */
  PRESUMED_NEGATED_LONGITUDE(0x02),

  /**
   * Set if latitude and longitude appears to have been switched.
   */
  PRESUMED_INVERTED_COORDINATES(0x04),

  /**
   * Set if coordinates are (0,0).
   */
  ZERO_COORDINATES(0x08),

  /**
   * Set if coordinates not in range -90 to 90 and -180 to 180.
   */
  COORDINATES_OUT_OF_RANGE(0x10),

  /**
   * Set if country name is not understood
   */
  COUNTRY_COORDINATE_MISMATCH(0x20);

  private final int issue;

  GeospatialIssue(int issue) {
    this.issue = issue;
  }

  public int getIssueCode() {
    return issue;
  }
}
