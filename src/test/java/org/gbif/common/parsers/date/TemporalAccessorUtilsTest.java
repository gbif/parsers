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

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link TemporalAccessorUtils}.
 *
 */
public class TemporalAccessorUtilsTest {

  private ZoneId EUROPE_CENTRAL_TIME = ZoneId.of("Europe/Paris");

  // January 1st 2000, 00:00:00 (midnight)
  private long YEAR2000_1JAN_EPOCH_UTC = 946684800000L;

  // January 1st 2000, 02:03:04
  private long YEAR2000_1JAN_2H_3M_4S_EPOCH_UTC = 946692184000L;

  // February 1st 2000, midnight
  private long YEAR2000_2FEB_0H_0M_0S_EPOCH_UTC = 949363200000l;

  // January 1st 2000, midnight
  private long YEAR2000_1JAN_0H_0M_0S_EPOCH_UTC = 946684800000l;

  @Test
  public void testToDate() {
    TemporalAccessor ta = LocalDate.of(2000, 1,1);
    Date date = TemporalAccessorUtils.toDate(ta, false);
    assertEquals(YEAR2000_1JAN_EPOCH_UTC, date.getTime());

    ta = LocalDateTime.of(2000, 1, 1, 2, 3, 4);
    date = TemporalAccessorUtils.toDate(ta, false);
    assertEquals(YEAR2000_1JAN_2H_3M_4S_EPOCH_UTC, date.getTime());

    //test partial date transformed to Date
    //YearMonth should use the first of the month
    ta = YearMonth.of(2000, 2);
    date = TemporalAccessorUtils.toDate(ta, false);
    assertEquals(YEAR2000_2FEB_0H_0M_0S_EPOCH_UTC, date.getTime());

    //Year should use the first of January
    ta = Year.of(2000);
    date = TemporalAccessorUtils.toDate(ta, false);
    assertEquals(YEAR2000_1JAN_0H_0M_0S_EPOCH_UTC, date.getTime());
  }

  @Test
  public void testToDateWithTimeZone() {

    // Test with ZonedDateTime
    ZonedDateTime testZonedDateTime = ZonedDateTime.of(2000, 1, 1, 4 , 20, 0, 0, EUROPE_CENTRAL_TIME);
    Date date = TemporalAccessorUtils.toDate(testZonedDateTime, false);
    assertEquals(testZonedDateTime.toEpochSecond(), date.getTime()/1000);

    // Test the same ZonedDateTime but ignore Offset information (timezone)
    ZonedDateTime testTimeUTC = ZonedDateTime.of(2000, 1, 1, 4 , 20, 0, 0, TemporalAccessorUtils.UTC_ZONE_ID);
    date = TemporalAccessorUtils.toDate(testZonedDateTime, true);
    assertEquals(testTimeUTC.toEpochSecond(), date.getTime()/1000);

    // Ensure it is also working with OffsetDateTime
    OffsetDateTime testOffsetDateTimeTime = testZonedDateTime.toOffsetDateTime();
    date = TemporalAccessorUtils.toDate(testOffsetDateTimeTime, false);
    assertEquals(testOffsetDateTimeTime.toEpochSecond(), date.getTime()/1000);

    date = TemporalAccessorUtils.toDate(testOffsetDateTimeTime, true);
    assertEquals(testTimeUTC.toEpochSecond(), date.getTime()/1000);
  }

  @Test
  public void testBestResolution() {
    TemporalAccessor ta1 = Year.of(2005);
    TemporalAccessor ta2 = YearMonth.of(2005, 1);
    TemporalAccessor result = TemporalAccessorUtils.bestResolution(ta1, ta2).get();
    assertEquals(YearMonth.of(2005, 1), YearMonth.from(result));

    ta1 = LocalDate.of(2005, 1, 1);
    ta2 = Year.of(2005);
    result = TemporalAccessorUtils.bestResolution(ta1, ta2).get();
    assertEquals(LocalDate.of(2005, 1, 1), LocalDate.from(result));

    //this should not work
    ta1 = Year.of(2005);
    ta2 = YearMonth.of(2006, 1);
    assertFalse(TemporalAccessorUtils.bestResolution(ta1, ta2).isPresent());
  }

  @Test
  public void testSameOrContained() {
    TemporalAccessor ymd = LocalDate.of(1996, 4, 26);

    // Test nulls and wrong resolutions
    assertFalse(TemporalAccessorUtils.sameOrContained(null, null));
    assertFalse(TemporalAccessorUtils.sameOrContained(ymd, null));
    assertFalse(TemporalAccessorUtils.sameOrContained(null, ymd));

    // Test against same year
    assertTrue(TemporalAccessorUtils.sameOrContained(ymd, Year.of(1996)));
    // Test against same month
    assertTrue(TemporalAccessorUtils.sameOrContained(ymd, YearMonth.of(1996, 4)));
    // Test against same date
    assertTrue(TemporalAccessorUtils.sameOrContained(ymd, LocalDate.of(1996, 4, 26)));
    // Test against time on that day
    assertTrue(TemporalAccessorUtils.sameOrContained(ymd, LocalDateTime.of(1996, 4, 26, 1, 2, 3)));

    // Then different year/month/date
    assertFalse(TemporalAccessorUtils.sameOrContained(ymd, Year.of(1998)));
    assertFalse(TemporalAccessorUtils.sameOrContained(ymd, YearMonth.of(1996, 5)));
    assertFalse(TemporalAccessorUtils.sameOrContained(ymd, LocalDate.of(1996, 4, 27)));
  }

  @Test
  public void testLastDay() {
    TemporalAccessor year = Year.of(1996);
    assertEquals("1996-12-31T23:59:59.999", TemporalAccessorUtils.toLatestLocalDateTime(year,true).toString());

    TemporalAccessor ym = YearMonth.of(1996,1);
    assertEquals("1996-01-31T23:59:59.999", TemporalAccessorUtils.toLatestLocalDateTime(ym,true).toString());

    ym = YearMonth.of(1996,2);
    assertEquals("1996-02-29T23:59:59.999", TemporalAccessorUtils.toLatestLocalDateTime(ym,true).toString());

    TemporalAccessor ymd = LocalDate.of(1996,2,3);
    assertEquals("1996-02-03T23:59:59.999", TemporalAccessorUtils.toLatestLocalDateTime(ymd,true).toString());

    TemporalAccessor ymdt = LocalDateTime.of(1996,2,3,1,20);
    assertEquals("1996-02-03T01:20", TemporalAccessorUtils.toLatestLocalDateTime(ymdt,true).toString());
  }

  @Test
  public void testWithinRange() {
    TemporalAccessor ymd = LocalDate.of(1996,2,3);
    TemporalAccessor begin = LocalDateTime.of(1996,2,3,1,20);
    TemporalAccessor end = LocalDateTime.of(1996,2,3,1,20);
    assertTrue(TemporalAccessorUtils.withinRange(begin, end, ymd));

    begin = LocalDateTime.of(1996,2,3,0,0);
    assertTrue(TemporalAccessorUtils.withinRange(begin, end, ymd));

    begin = LocalDateTime.of(1996,2,2,1,20);
    assertTrue(TemporalAccessorUtils.withinRange(begin, end, ymd));

    end = LocalDateTime.of(1999,12,4,1,20);
    assertTrue(TemporalAccessorUtils.withinRange(begin, end, ymd));

    ymd = LocalDate.of(1990,2,3);
    assertFalse(TemporalAccessorUtils.withinRange(begin, end, ymd));
  }
}
