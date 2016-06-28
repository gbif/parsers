package org.gbif.common.parsers.date;

import java.util.Date;

import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.temporal.TemporalAccessor;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit test for {@link TemporalAccessorUtils}.
 *
 */
public class TemporalAccessorUtilsTest {

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
}
