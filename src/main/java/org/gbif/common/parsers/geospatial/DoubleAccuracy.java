package org.gbif.common.parsers.geospatial;

import java.util.Objects;
import java.util.StringJoiner;

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
    return new StringJoiner(", ", DoubleAccuracy.class.getSimpleName() + "[", "]")
        .add("value=" + value)
        .add("accuracy=" + accuracy)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DoubleAccuracy that = (DoubleAccuracy) o;
    return Objects.equals(value, that.value) && Objects.equals(accuracy, that.accuracy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, accuracy);
  }
}
