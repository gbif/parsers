package org.gbif.common.parsers.date;

import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * AtomizedLocalDate is a simple immutable class to hold data from a
 * {@link org.threeten.bp.temporal.TemporalAccessor}.
 *
 */
public class AtomizedLocalDate {

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
   * Build a new instance of {@link AtomizedLocalDate} based on a {@link org.threeten.bp.temporal.TemporalAccessor}.
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
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("year", year).append("month", month)
            .append("day", day).append("resolution", resolution).toString();
  }
}
