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
package org.gbif.common.parsers.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Utility methods to work with {@link TemporalAccessor}
 */
public class TemporalAccessorUtils {

  public static ZoneId UTC_ZONE_ID = ZoneOffset.UTC;

  /**
   * Transform a {@link TemporalAccessor} to a {@link java.util.Date}.
   * If the provided {@link TemporalAccessor} contains offset(timezone) information it will be used.
   * See {@link #toDate(TemporalAccessor, boolean)} for more details.
   *
   * @return  the Date object or null if a Date object can not be created
   */
  public static Date toDate(TemporalAccessor temporalAccessor) {
    return toDate(temporalAccessor, false);
  }

  /**
   * Transform a {@link TemporalAccessor} to a {@link java.util.Date}, rounding a partial date/time to the start
   * of the period.
   *
   * For {@link YearMonth}, the {@link java.util.Date} will represent the first day of the month.
   * For {@link Year}, the {@link java.util.Date} will represent the first day of January.
   *
   * Remember that a {@link Date} object will always display the date in the current timezone.
   *
   * @param ignoreOffset in case offset information is available in the provided {@link TemporalAccessor}, should it
   *                     be used ?
   * @return the Date object or null if a Date object can not be created
   */
  public static Date toDate(TemporalAccessor temporalAccessor, boolean ignoreOffset) {
    return Date.from(toEarliestLocalDateTime(temporalAccessor, ignoreOffset).toInstant(ZoneOffset.UTC));
  }

  /**
   * Removes any time zone, optionally adjusting the time using the zone offset first.
   */
  public static TemporalAccessor stripOffsetOrZone(TemporalAccessor temporalAccessor, boolean ignoreOffset) {
    if (temporalAccessor == null) {
      return null;
    }

    // Use offset if present
    if (!ignoreOffset && temporalAccessor.isSupported(ChronoField.OFFSET_SECONDS)) {
      return temporalAccessor.query(OffsetDateTime::from).atZoneSameInstant(UTC_ZONE_ID).toLocalDateTime();
    }

    if (temporalAccessor.isSupported(ChronoField.SECOND_OF_DAY)) {
      return temporalAccessor.query(LocalDateTime::from);
    }

    return temporalAccessor;
  }

  /**
   * Transform a {@link TemporalAccessor} to a {@link java.time.LocalDateTime}, rounding a partial date/time to the
   * start of the period.
   *
   * @param temporalAccessor
   * @param ignoreOffset in case offset information is available in the provided {@link TemporalAccessor}, should it
   *                     be used ?
   * @return the LocalDateTime object or null if a LocalDateTime object can not be created
   */
  public static LocalDateTime toEarliestLocalDateTime(TemporalAccessor temporalAccessor, boolean ignoreOffset) {
    if (temporalAccessor == null) {
      return null;
    }

    // Use offset if present
    if (!ignoreOffset && temporalAccessor.isSupported(ChronoField.OFFSET_SECONDS)) {
      return temporalAccessor.query(OffsetDateTime::from).atZoneSameInstant(UTC_ZONE_ID).toLocalDateTime();
    }

    if (temporalAccessor.isSupported(ChronoField.SECOND_OF_DAY)) {
      return temporalAccessor.query(LocalDateTime::from);
    }

    // this may return null in case of partial dates
    LocalDate localDate = temporalAccessor.query(TemporalQueries.localDate());

    // try YearMonth
    if (localDate == null && temporalAccessor.isSupported(ChronoField.MONTH_OF_YEAR)) {
      YearMonth yearMonth = YearMonth.from(temporalAccessor);
      localDate = yearMonth.atDay(1);
    }

    // try Year
    if (localDate == null && temporalAccessor.isSupported(ChronoField.YEAR)) {
      Year year = Year.from(temporalAccessor);
      localDate = year.atDay(1);
    }

    if (localDate != null) {
      return LocalDateTime.from(localDate.atStartOfDay(UTC_ZONE_ID));
    }

    return null;
  }

  /**
   * Transform a {@link TemporalAccessor} to a {@link java.time.LocalDateTime}, rounding a partial date/time to the
   * end of the period.
   *
   * 1990 will be 1990-12-31,
   * 1996-02 will be 1996-02-29
   *
   * @param temporalAccessor
   * @param ignoreOffset in case offset information is available in the provided {@link TemporalAccessor}, should it
   *                     be used ?
   * @return the LocalDateTime object or null if a LocalDateTime object can not be created
   */
  public static LocalDateTime toLatestLocalDateTime(TemporalAccessor temporalAccessor, boolean ignoreOffset) {
    if (temporalAccessor == null) {
      return null;
    }

    // Use offset if present
    if (!ignoreOffset && temporalAccessor.isSupported(ChronoField.OFFSET_SECONDS)) {
      return temporalAccessor.query(OffsetDateTime::from).atZoneSameInstant(UTC_ZONE_ID).toLocalDateTime();
    }

    if (temporalAccessor.isSupported(ChronoField.SECOND_OF_DAY)) {
      return temporalAccessor.query(LocalDateTime::from);
    }

    // this may return null in case of partial dates
    LocalDate localDate = temporalAccessor.query(TemporalQueries.localDate());

    // try YearMonth
    if (localDate == null && temporalAccessor.isSupported(ChronoField.MONTH_OF_YEAR)) {
      YearMonth yearMonth = YearMonth.from(temporalAccessor);
      localDate = yearMonth.atEndOfMonth();
    }

    // try Year
    if (localDate == null && temporalAccessor.isSupported(ChronoField.YEAR)) {
      Year year = Year.from(temporalAccessor);
      localDate = LocalDate.of(year.getValue(), 12, 31);
    }

    if (localDate != null) {
      return LocalDateTime.from(localDate.atTime(23, 59, 59));
    }

    return null;
  }

  /**
   * The idea of "best resolution" TemporalAccessor is to get the TemporalAccessor that offers more resolution than
   * the other, but they must NOT contradict.
   * e.g. 2005-01 and 2005-01-01 will return 2005-01-01.
   *
   * Note that if one of the 2 parameters is null the other one will be considered having the best resolution
   *
   * @return TemporalAccessor representing the best resolution
   */
  public static Optional<TemporalAccessor> bestResolution(@Nullable TemporalAccessor ta1, @Nullable TemporalAccessor ta2) {
    // handle nulls combinations
    if (ta1 == null && ta2 == null) {
      return Optional.empty();
    }
    if (ta1 == null) {
      return Optional.of(ta2);
    }
    if (ta2 == null) {
      return Optional.of(ta1);
    }

    AtomizedLocalDateTime ymd1 = AtomizedLocalDateTime.fromTemporalAccessor(ta1);
    AtomizedLocalDateTime ymd2 = AtomizedLocalDateTime.fromTemporalAccessor(ta2);

    // If they both provide the year, it must match
    if (ymd1.getYear() != null && ymd2.getYear() != null && !ymd1.getYear().equals(ymd2.getYear())) {
      return Optional.empty();
    }
    // If they both provide the month, it must match
    if (ymd1.getMonth() != null && ymd2.getMonth() != null && !ymd1.getMonth().equals(ymd2.getMonth())) {
      return Optional.empty();
    }
    // If they both provide the day, it must match
    if (ymd1.getDay() != null && ymd2.getDay() != null && !ymd1.getDay().equals(ymd2.getDay())) {
      return Optional.empty();
    }
    // If they both provide the hour, it must match
    if (ymd1.getHour() != null && ymd2.getHour() != null && !ymd1.getHour().equals(ymd2.getHour())) {
      return Optional.empty();
    }
    // If they both provide the minute, it must match
    if (ymd1.getMinute() != null && ymd2.getMinute() != null && !ymd1.getMinute().equals(ymd2.getMinute())) {
      return Optional.empty();
    }
    // If they both provide the second, it must match
    if (ymd1.getSecond() != null && ymd2.getSecond() != null && !ymd1.getSecond().equals(ymd2.getSecond())) {
      return Optional.empty();
    }
    // If they both provide the millisecond, it must match
    if (ymd1.getMillisecond() != null && ymd2.getMillisecond() != null && !ymd1.getMillisecond().equals(ymd2.getMillisecond())) {
      return Optional.empty();
    }

    if (ymd1.getResolution() > ymd2.getResolution()) {
      return Optional.of(ta1);
    }

    return Optional.of(ta2);
  }

  /**
   * The idea of "non-conflicting date parts" TemporalAccessor is to get as much of year, then month, then day as possible,
   * ignoring null and stopping once there is a contradiction.  Times are ignored for comparison, but the argument with
   * the highest resolution is returned.
   *
   * e.g. 2005-02, 2005-02-03, 2005-02-03T04:05:06 will return 2005-02-03T04:05:06.
   *
   * Null arguments are ignored.
   *
   * @return TemporalAccessor representing the best resolution
   */
  public static Optional<TemporalAccessor> nonConflictingDateParts(
    @Nullable TemporalAccessor ta1, @Nullable TemporalAccessor ta2, @Nullable TemporalAccessor ta3) {
    // handle nulls combinations
    if (ta1 == null && ta2 == null && ta3 == null) {
      return Optional.empty();
    }
    if (ta2 == null && ta3 == null) {
      return Optional.of(ta1);
    }
    if (ta3 == null && ta1 == null) {
      return Optional.of(ta2);
    }
    if (ta1 == null && ta2 == null) {
      return Optional.of(ta3);
    }

    AtomizedLocalDateTime ymd1 = AtomizedLocalDateTime.fromTemporalAccessor(ta1);
    AtomizedLocalDateTime ymd2 = AtomizedLocalDateTime.fromTemporalAccessor(ta2);
    AtomizedLocalDateTime ymd3 = AtomizedLocalDateTime.fromTemporalAccessor(ta3);

    List<AtomizedLocalDateTime> ymd = new ArrayList<>();
    if (ymd1 != null) ymd.add(ymd1);
    if (ymd2 != null) ymd.add(ymd2);
    if (ymd3 != null) ymd.add(ymd3);

    final Integer year, month;

    // If they both provide the year, it must match
    if (ymd.stream().map(m -> m.getYear()).distinct().count() == 1) {
      year = ymd.stream().filter(m -> m.getYear() != null).findFirst().map(AtomizedLocalDateTime::getYear).orElse(null);
    } else {
      return Optional.empty();
    }

    // If they both provide the month, it must match
    if (ymd.stream().map(m -> m.getMonth()).distinct().count() == 1) {
      month = ymd.stream().filter(m -> m.getMonth() != null).findFirst().map(AtomizedLocalDateTime::getMonth).orElse(null);
    } else {
      return Optional.of(Year.of(year));
    }

    // If they both provide the day, it must match
    if (ymd.stream().map(m -> m.getDay()).distinct().count() == 1) {
      // Then return the one with the best resolution
      return bestResolution(ta1, bestResolution(ta2, ta3).orElse(null));
    } else {
      if (month == null) {
        return Optional.of(Year.of(year));
      } else {
        return Optional.of(YearMonth.of(year, month));
      }
    }
  }

  /**
   * Given two TemporalAccessor with possibly different resolutions, this method checks if they represent the same
   * date, or if one is contained within the other.  The comparison does not go beyond date resolution.
   *
   * If a null is provided, false will be returned.
   */
  public static boolean sameOrContained(@Nullable TemporalAccessor ta1, @Nullable TemporalAccessor ta2) {
    // handle nulls combinations
    if (ta1 == null || ta2 == null) {
      return false;
    }

    AtomizedLocalDate ymd1 = AtomizedLocalDate.fromTemporalAccessor(ta1);
    AtomizedLocalDate ymd2 = AtomizedLocalDate.fromTemporalAccessor(ta2);

    // we only deal with complete Local Date
    if (ymd1.isComplete() && ymd2.isComplete()) {
      return ymd1.equals(ymd2);
    }

    // check for equal years
    if (!ymd1.getYear().equals(ymd2.getYear())) {
      return false;
    }

    // compare months
    if (ymd1.getMonth() == null || ymd2.getMonth() == null) {
      return true;
    }
    if (!ymd1.getMonth().equals(ymd2.getMonth())) {
      return false;
    }

    // compare days
    if (ymd1.getDay() == null || ymd2.getDay() == null) {
      return true;
    }
    return ymd1.getDay().equals(ymd2.getDay());
  }

  /**
   * Given two TemporalAccessor with possibly different resolutions, this method checks if they represent the same
   * date, or if one is contained within the other.  The comparison does not go beyond date resolution.
   *
   * If a null is provided, true will be returned.
   */
  public static boolean sameOrContainedOrNull(@Nullable TemporalAccessor ta1, @Nullable TemporalAccessor ta2) {
    // handle nulls combinations
    if (ta1 == null || ta2 == null) {
      return true;
    }

    return sameOrContained(ta1, ta2);
  }

  /**
   * Given two TemporalAccessor with at least date resolution, this method checks if they represent the same
   * date.
   *
   * If a null is provided, false will be returned.
   */
  public static boolean sameDate(@Nullable TemporalAccessor ta1, @Nullable TemporalAccessor ta2) {
    // handle nulls combinations
    if (ta1 == null || ta2 == null) {
      return false;
    }

    AtomizedLocalDate ymd1 = AtomizedLocalDate.fromTemporalAccessor(ta1);
    AtomizedLocalDate ymd2 = AtomizedLocalDate.fromTemporalAccessor(ta2);

    // we only deal with complete Local Date
    if (ymd1.isComplete() && ymd2.isComplete()) {
      return ymd1.equals(ymd2);
    }

    return false;
  }

  /**
   * Returns a date from the list of dodgyTas which matches the reliableTa
   */
  public static Optional<TemporalAccessor> resolveAmbiguousDates(TemporalAccessor reliableTa, List<TemporalAccessor> dodgyTas) {
    // A DD-MM-YYYY or MM/DD/YYYY date could be disambiguated by another date.
    for (TemporalAccessor possibleTa : dodgyTas) {
      if (TemporalAccessorUtils.sameDate(reliableTa, possibleTa)) {
        return Optional.of(possibleTa);
      }
    }

    return Optional.empty();
  }

  /**
   * Returns the resolution of the TemporalAccessor: 1, 2, 3 for day, month year.  0 for null/empty.
   */
  public static int resolution(TemporalAccessor ta) {
    if (ta == null) {
      return 0;
    }

    AtomizedLocalDate ymd = AtomizedLocalDate.fromTemporalAccessor(ta);

    return ymd.getResolution();
  }
}
