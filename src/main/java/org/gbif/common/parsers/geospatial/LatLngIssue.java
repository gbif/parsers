package org.gbif.common.parsers.geospatial;

import org.gbif.api.vocabulary.OccurrenceIssue;

import com.google.common.base.Objects;

/**
 * Simple container class for an interpreted coordinate and a single interpretation issue.
 * The issue will be null if non exists.
 */
public class LatLngIssue {

  private final Double lat;
  private final Double lng;
  private final OccurrenceIssue issue;

  public LatLngIssue(double lat, double lng, OccurrenceIssue issue) {
    this.lat = lat;
    this.lng = lng;
    this.issue = issue;
  }

  public LatLngIssue(OccurrenceIssue issue) {
    lat = null;
    lng = null;
    this.issue = issue;
  }

  public LatLngIssue(double lat, double lng) {
    this.lat = lat;
    this.lng = lng;
    this.issue = null;
  }

  public Double getLat() {
    return lat;
  }

  public Double getLng() {
    return lng;
  }

  public OccurrenceIssue getIssue() {
    return issue;
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("lat", lat)
      .add("lng", lng)
      .add("issue", issue)
      .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(lat, lng, issue);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof LatLngIssue)) {
      return false;
    }

    LatLngIssue that = (LatLngIssue) object;
    return Objects.equal(this.lat, that.lat)
           && Objects.equal(this.lng, that.lng)
           && Objects.equal(this.issue, that.issue);
  }
}
