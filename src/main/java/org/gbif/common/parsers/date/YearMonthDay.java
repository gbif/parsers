package org.gbif.common.parsers.date;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @deprecated replaced by AtomizedLocalDate + threeten
 */
@Deprecated
public class YearMonthDay {

  protected String year;
  protected String month;
  protected String day;

  public YearMonthDay(String year, String month, String day) {
    setYear(year);
    setMonth(month);
    setDay(day);
  }

  public YearMonthDay() {
  }

  public String getYear() {
    return year;
  }

  /**
   * @return day value or null, catching exceptions
   */
  public Integer getIntegerYear() {
    return parseInt(year);
  }

  private static Integer parseInt(String x) {
    if (!Strings.isNullOrEmpty(x)) {
      try {
        return Integer.valueOf(x);
      } catch (NumberFormatException e) {
        // swallow
      }
    }
    return null;
  }

  public void setYear(@Nullable String year) {
    this.year = year;
    if (year != null) {
      for (int i = year.length(); i < 4; i++) {
        this.year = "0" + year;
      }
    }
  }

  public String getMonth() {
    return month;
  }

  /**
   * @return day value or null, catching exceptions
   */
  public Integer getIntegerMonth() {
    return parseInt(month);
  }

  public void setMonth(@Nullable String month) {
    this.month = month;
    if (month != null) {
      for (int i = month.length(); i < 2; i++) {
        this.month = "0" + month;
      }
    }
  }

  public String getDay() {
    return day;
  }

  /**
   * @return day value or null, catching exceptions
   */
  public Integer getIntegerDay() {
    return parseInt(day);
  }

  public void setDay(@Nullable String day) {
    this.day = day;
    if (day != null) {
      for (int i = day.length(); i < 2; i++) {
        this.day = "0" + day;
      }
    }
  }

  /**
   * @return true if year month and day are all null
   */
  public boolean representsNull() {
    return year == null && month == null && day == null;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(year).append(month).append(day).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof YearMonthDay) {
      YearMonthDay ymd = (YearMonthDay) obj;
      return new EqualsBuilder().append(year, ymd.getYear()).append(month, ymd.getMonth()).append(day, ymd.getDay())
        .isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("year", year).append("month", month)
      .append("day", day).toString();
  }
}
