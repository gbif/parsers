package org.gbif.common.parsers.date;

import java.util.Date;

import com.google.common.base.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.Year;
import org.threeten.bp.YearMonth;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAccessor;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit test for {@link TemporalAccessorUtils}.
 *
 */
public class TemporalAccessorUtilsTest {

  private ZoneId EUROPE_CENTRAL_TIME = ZoneId.of("Europe/Paris");

  // January 1st 2000, 00:00:00 (midnight)
  private long YEAR2000_1JAN_EPOCH_UTC = 946684800000l;

  // January 1st 2000, 02:03:04
  private long YEAR2000_1JAN_2H_3M_4S_EPOCH_UTC = 946692184000l;

  // February 1st 2000, midnight
  private long YEAR2000_2FEB_0H_0M_0S_EPOCH_UTC = 949363200000l;

  // January 1st 2000, midnight
  private long YEAR2000_1JAN_0H_0M_0S_EPOCH_UTC = 946684800000l;

  @Test
  public void testToDate(){
    TemporalAccessor ta = LocalDate.of(2000,01,01);
    Date date = TemporalAccessorUtils.toDate(ta, false);
    assertEquals(YEAR2000_1JAN_EPOCH_UTC, date.getTime());

    ta = LocalDateTime.of(2000, 01, 01, 2, 3, 4);
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
  public void testToDateWithTimeZone(){

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
  public void testGetBestResolutionTemporalAccessor(){
    TemporalAccessor ta1 = Year.of(2005);
    TemporalAccessor ta2 = YearMonth.of(2005, 1);
    Optional<? extends TemporalAccessor> result = TemporalAccessorUtils.getBestResolutionTemporalAccessor(ta1, ta2);
    Assert.assertEquals(YearMonth.of(2005, 1), YearMonth.from(result.get()));

    ta1 = LocalDate.of(2005, 1, 1);
    ta2 = Year.of(2005);
    result = TemporalAccessorUtils.getBestResolutionTemporalAccessor(ta1, ta2);
    Assert.assertEquals(LocalDate.of(2005, 1, 1), LocalDate.from(result.get()));

    //this should not work
    ta1 = Year.of(2005);
    ta2 = YearMonth.of(2006, 1);
    result = TemporalAccessorUtils.getBestResolutionTemporalAccessor(ta1, ta2);
    assertFalse(result.isPresent());
  }

}
