/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
