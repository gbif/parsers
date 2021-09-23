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
package org.gbif.common.parsers.date;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * AtomizedLocalDate is a simple immutable class to hold local date data from a
 * {@link TemporalAccessor}.
 *
 * Thread-Safe, immutable class.
 *
 */
public class AtomizedLocalDate {

  // Maximum resolution for a complete Local Date
  public static final int COMPLETE_LOCAL_DATE_RESOLUTION = 3;

  private final Integer year;
  private final Integer month;
  private final Integer day;

  private final int resolution;

  private AtomizedLocalDate(Integer year, Integer month, Integer day){
    this.year = year;
    this.month = month;
    this.day = day;

    int res = 0;
    if(year != null){
      res++;
    }
    if(month != null){
      res++;
    }
    if(day != null){
      res++;
    }
    resolution = res;
  }

  /**
   * Build a new instance of {@link AtomizedLocalDate} based on a {@link TemporalAccessor}.
   * This is done by extracting the {@link ChronoField}.
   * @param temporalAccessor
   * @return
   */
  public static AtomizedLocalDate fromTemporalAccessor(TemporalAccessor temporalAccessor){
    if(temporalAccessor == null){
      return null;
    }

    Integer y = null, m = null, d = null;
    if(temporalAccessor.isSupported(ChronoField.YEAR)){
      y = temporalAccessor.get(ChronoField.YEAR);
    }

    if(temporalAccessor.isSupported(ChronoField.MONTH_OF_YEAR)){
      m = temporalAccessor.get(ChronoField.MONTH_OF_YEAR);
    }

    if(temporalAccessor.isSupported(ChronoField.DAY_OF_MONTH)){
      d = temporalAccessor.get(ChronoField.DAY_OF_MONTH);
    }

    return new AtomizedLocalDate(y, m, d);
  }

  public Integer getYear() {
    return year;
  }

  public Integer getMonth() {
    return month;
  }

  public Integer getDay() {
    return day;
  }

  public int getResolution(){
    return resolution;
  }

  /**
   * Is the resolution of this local date matches the maximum.
   * @return
   */
  public boolean isComplete() {
    return COMPLETE_LOCAL_DATE_RESOLUTION == resolution;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(year).append(month).append(day).append(resolution).toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AtomizedLocalDate that = (AtomizedLocalDate) o;

    return resolution == that.resolution
        && Objects.equals(year, that.year)
        && Objects.equals(month, that.month)
        && Objects.equals(day, that.day);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("year", year)
            .append("month", month)
            .append("day", day)
            .append("resolution", resolution)
            .toString();
  }
}
