package org.gbif.common.parsers.geospatial;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Simple container class.
 */
public class LatLngStatus {

  private final Double lat;
  private final Double lng;
  private final GeospatialIssue issue;

  public LatLngStatus(double lat, double lng, GeospatialIssue issue) {
    this.lat = lat;
    this.lng = lng;
    this.issue = issue;
  }

  public LatLngStatus(GeospatialIssue issue) {
    lat = null;
    lng = null;
    this.issue = issue;
  }

  public LatLngStatus(double lat, double lng) {
    this.lat = lat;
    this.lng = lng;
    this.issue = GeospatialIssue.NO_ISSUES;
  }

  public Double getLat() {
    return lat;
  }

  public Double getLng() {
    return lng;
  }

  public GeospatialIssue getIssue() {
    return issue;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("lat", lat).append("lng", lng)
      .append("issue", issue).toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(lat).append(lng).append(issue).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LatLngStatus) {
      return new EqualsBuilder().append(lat, ((LatLngStatus) obj).getLat()).append(lng, ((LatLngStatus) obj).getLng())
        .append(issue, ((LatLngStatus) obj).getIssue()).isEquals();
    } else {
      return false;
    }
  }
}
