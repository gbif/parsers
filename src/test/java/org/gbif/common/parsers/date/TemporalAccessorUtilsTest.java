package org.gbif.common.parsers.date;

import java.util.Date;

import com.google.common.base.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
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

  private long YEAR2000_EPOCH_UTC = 946684800000l;
  private long YEAR2000_2H_3M_4S_EPOCH_UTC = 946692184000l;

  @Test
  public void testToUTCDate(){
    TemporalAccessor ta = LocalDate.of(2000,01,01);
    Date date = TemporalAccessorUtils.toUTCDate(ta);
    assertEquals(YEAR2000_EPOCH_UTC, date.getTime());

    ta = LocalDateTime.of(2000, 01, 01, 2, 3, 4);
    date = TemporalAccessorUtils.toUTCDate(ta);
    assertEquals(YEAR2000_2H_3M_4S_EPOCH_UTC, date.getTime());
  }

  @Test
  public void testToUTCDateTimeZone(){
    ZonedDateTime testTime = ZonedDateTime.of(2000, 1, 1, 4 , 20, 0, 0, EUROPE_CENTRAL_TIME);
    Date date = TemporalAccessorUtils.toUTCDate(testTime);

    assertEquals(testTime.toEpochSecond(), date.getTime()/1000);
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
