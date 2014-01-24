package org.gbif.common.parsers.date;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

  public Integer getIntegerYear() {
    return year == null ? null : Integer.valueOf(year);
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

  public Integer getIntegerMonth() {
    return month == null ? null : Integer.valueOf(month);
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
