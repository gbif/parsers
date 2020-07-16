package org.gbif.common.parsers.geospatial;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Simple container class for double based values with an accuracy (e.g. depth in meters).
 */
public class DoubleAccuracy {

  private final Double value;
  private final Double accuracy;

  public DoubleAccuracy(Double value, Double accuracy) {
    this.value = value;
    // never negative
    this.accuracy = accuracy == null ? null : Math.abs(accuracy);
  }

  public Double getValue() {
    return value;
  }

  public Double getAccuracy() {
    return accuracy;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("value", value)
      .add("accuracy", accuracy)
      .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value, accuracy);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof DoubleAccuracy)) {
      return false;
    }

    DoubleAccuracy that = (DoubleAccuracy) object;
    return Objects.equal(this.value, that.value)
           && Objects.equal(this.accuracy, that.accuracy);
  }

}
