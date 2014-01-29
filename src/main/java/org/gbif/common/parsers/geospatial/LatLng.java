package org.gbif.common.parsers.geospatial;

import com.google.common.base.Objects;

/**
 * Simple container class for an interpreted coordinate.
 */
public class LatLng {

  private final Double lat;
  private final Double lng;

  public LatLng(double lat, double lng) {
    this.lat = lat;
    this.lng = lng;
  }

  public LatLng() {
    lat = null;
    lng = null;
  }

  public Double getLat() {
    return lat;
  }

  public Double getLng() {
    return lng;
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("lat", lat)
      .add("lng", lng)
      .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(lat, lng);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof LatLng)) {
      return false;
    }

    LatLng that = (LatLng) object;
    return Objects.equal(this.lat, that.lat)
           && Objects.equal(this.lng, that.lng);
  }
}
