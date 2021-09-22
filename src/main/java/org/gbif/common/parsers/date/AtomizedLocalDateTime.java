package org.gbif.common.parsers.date;

import java.time.Duration;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Still experimental, the class should probably require to have a LocalDate otherwise the output
 * of getResolution can not be comparable with {@link AtomizedLocalDate}
 *
 * AtomizedLocalDateTime is a simple immutable class to hold local date and time data from a
 * {@link TemporalAccessor}.
 *
 *
 * Thread-Safe, immutable class.
 */
public class AtomizedLocalDateTime {

  private final AtomizedLocalDate localDate;
  private final Integer hour;
  private final Integer minute;
  private final Integer second;
  private final Integer millisecond;

  private final int resolution;


  private AtomizedLocalDateTime(AtomizedLocalDate localDate, Integer hour, Integer minute,
                                Integer second, Integer millisecond) {
    int res = 0;
    this.localDate = localDate;
    this.hour = hour;
    this.minute = minute;
    this.second = second;
    this.millisecond = millisecond;

    // compute resolution
    if(localDate != null){
      res += localDate.getResolution();
    }

    if(hour != null){
      res ++;
    }
    if(minute != null){
      res++;
    }
    if(second != null){
      res++;
    }
    if(millisecond != null){
      res++;
    }
    this.resolution = res;
  }

  public Integer getYear() {
    if(localDate == null){
      return null;
    }
    return localDate.getYear();
  }

  public Integer getMonth() {
    if(localDate == null){
      return null;
    }
    return localDate.getMonth();
  }

  public Integer getDay() {
    if(localDate == null){
      return null;
    }
    return localDate.getDay();
  }

  public Integer getHour() {
    return hour;
  }

  public Integer getMinute() {
    return minute;
  }

  public Integer getSecond() {
    return second;
  }

  public Integer getMillisecond() {
    return millisecond;
  }

  /**
   * Get the resolution of the {@link AtomizedLocalDateTime}.
   * Resolution represents the number of parts this date/time contains.
   * Be aware that it is not possible to express partial time which means that the lowest
   * resolution possible if 4 (Time with no date)
   * @return
   */
  public int getResolution(){
    return resolution;
  }

  /**
   * Build a new instance of {@link AtomizedLocalDateTime} based on a {@link TemporalAccessor}.
   * This is done by extracting the {@link ChronoField}.
   * Please note this expected behavior: if a least 1 time component is available on the TemporalAccessor, the other
   * time components will return 0 instead of null if they are not provided.
   *
   * AtomizedLocalDateTime.fromTemporalAccessor(LocalTime.of(HOUR, MINUTE) will return 0 as second and millisecond and
   * the resolution will be 4.
   */
  public static AtomizedLocalDateTime fromTemporalAccessor(TemporalAccessor temporalAccessor) {
    if (temporalAccessor == null) {
      return null;
    }

    //TODO if localDate == null, we should probably return null. If we want to handle time only
    //we should create AtomizedLocalTime

    AtomizedLocalDate localDate = AtomizedLocalDate.fromTemporalAccessor(temporalAccessor);


    Integer h = null, m = null, s = null, ms = null;
    if (temporalAccessor.isSupported(ChronoField.HOUR_OF_DAY)) {
      h = temporalAccessor.get(ChronoField.HOUR_OF_DAY);
    }

    if (temporalAccessor.isSupported(ChronoField.MINUTE_OF_HOUR)) {
      m = temporalAccessor.get(ChronoField.MINUTE_OF_HOUR);
    }

    if (temporalAccessor.isSupported(ChronoField.SECOND_OF_MINUTE)) {
      s = temporalAccessor.get(ChronoField.SECOND_OF_MINUTE);
    }

    if (temporalAccessor.isSupported(ChronoField.MILLI_OF_SECOND)) {
      ms = temporalAccessor.get(ChronoField.MILLI_OF_SECOND);
    } else if (temporalAccessor.isSupported(ChronoField.NANO_OF_SECOND)) {
      ms = Long.valueOf(Duration.ofNanos(temporalAccessor.get(ChronoField.NANO_OF_SECOND)).toMillis()).intValue();
    }
    return new AtomizedLocalDateTime(localDate, h, m, s, ms);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(localDate).append(hour).append(minute).
            append(second).append(millisecond).append(resolution).toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AtomizedLocalDateTime that = (AtomizedLocalDateTime) o;

    return resolution == that.resolution
        && Objects.equals(localDate, that.localDate)
        && Objects.equals(hour, that.hour)
        && Objects.equals(minute, that.minute)
        && Objects.equals(second, that.second)
        && Objects.equals(millisecond, that.millisecond);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("localDate", localDate)
            .append("hour", hour)
            .append("minute", minute)
            .append("second", second)
            .append("millisecond", millisecond)
            .append("resolution", resolution)
            .toString();
  }

}
