package org.gbif.common.parsers.geospatial;

import com.google.common.base.Objects;

/**
 * Simple container class for integer based values with an accuracy (e.g. depth in meters).
 */
public class IntAccuracy {

  private final Integer value;
  private final Integer accuracy;

  public IntAccuracy(Integer value, Integer accuracy) {
    this.value = value;
    this.accuracy = accuracy;
  }

  public Integer getValue() {
    return value;
  }

  public Integer getAccuracy() {
    return accuracy;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
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
    if (!(object instanceof IntAccuracy)) {
      return false;
    }

    IntAccuracy that = (IntAccuracy) object;
    return Objects.equal(this.value, that.value)
           && Objects.equal(this.accuracy, that.accuracy);
  }

}
