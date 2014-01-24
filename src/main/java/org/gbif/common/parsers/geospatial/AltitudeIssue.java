package org.gbif.common.parsers.geospatial;

public enum AltitudeIssue {

  /**
   * Set if altitude is >10000m or <-1000m
   */
  GEOSPATIAL_ALTITUDE_OUT_OF_RANGE(0x80),

  /**
   * Set if altitude is -9999 or some other bogus value
   */
  GEOSPATIAL_PRESUMED_ERRONOUS_ALTITUDE(0x100),

  /**
   * Set if supplied min > max altitude
   */
  GEOSPATIAL_PRESUMED_MIN_MAX_ALTITUDE_REVERSED(0x200),

  /**
   * Set if supplied altitude is in feet instead of metric
   */
  GEOSPATIAL_PRESUMED_ALTITUDE_IN_FEET(0x2000),

  /**
   * Set if altitude is a non numeric value
   */
  GEOSPATIAL_PRESUMED_ALTITUDE_NON_NUMERIC(0x4000);

  private final int issue;

  AltitudeIssue(int issue) {
    this.issue = issue;
  }

  public int getIssueCode() {
    return issue;
  }
}
