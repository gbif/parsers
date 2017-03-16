package org.gbif.common.parsers.date;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

import com.google.common.base.Objects;
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
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AtomizedLocalDate)) {
      return false;
    }

    AtomizedLocalDate that = (AtomizedLocalDate) obj;
    return Objects.equal(this.year, that.year)
            && Objects.equal(this.month, that.month)
            && Objects.equal(this.day, that.day)
            && Objects.equal(this.resolution, that.resolution);
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
