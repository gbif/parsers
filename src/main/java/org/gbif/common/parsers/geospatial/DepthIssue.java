package org.gbif.common.parsers.geospatial;

public enum DepthIssue {

  /**
   * Set if supplied depth is in feet instead of metric
   */
  GEOSPATIAL_PRESUMED_DEPTH_IN_FEET(0x400),

  /**
   * Set if depth is larger than is feasible
   */
  GEOSPATIAL_DEPTH_OUT_OF_RANGE(0x800),

  /**
   * Set if supplied min>max
   */
  GEOSPATIAL_PRESUMED_MIN_MAX_DEPTH_REVERSED(0x1000), /**
   * /** Set if depth is a non numeric value
   */
  GEOSPATIAL_PRESUMED_DEPTH_NON_NUMERIC(0x8000);

  private final int issue;

  DepthIssue(int issue) {
    this.issue = issue;
  }

  public int getIssueCode() {
    return issue;
  }
}

