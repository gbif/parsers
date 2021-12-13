/*
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
    return new StringJoiner(", ", LatLng.class.getSimpleName() + "[", "]")
        .add("lat=" + lat)
        .add("lng=" + lng)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LatLng latLng = (LatLng) o;
    return Objects.equals(lat, latLng.lat) && Objects.equals(lng, latLng.lng);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lat, lng);
  }
}
