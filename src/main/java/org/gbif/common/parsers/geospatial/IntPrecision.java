package org.gbif.common.parsers.geospatial;

import com.google.common.base.Objects;

/**
 * Simple container class for integer based values with an accurracy (e.g. depth in meters).
 */
public class IntPrecision {

  private final Integer value;
  private final Integer precision;

  public IntPrecision(Integer value, Integer precision) {
    this.value = value;
    this.precision = precision;
  }

  public Integer getValue() {
    return value;
  }

  public Integer getPrecision() {
    return precision;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("value", value)
      .add("precision", precision)
      .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value, precision);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof IntPrecision)) {
      return false;
    }

    IntPrecision that = (IntPrecision) object;
    return Objects.equal(this.value, that.value)
           && Objects.equal(this.precision, that.precision);
  }

}
