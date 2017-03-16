package org.gbif.common.parsers.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class AtomizedLocalDateTimeTest {

  private static final int YEAR = 1978;
  private static final Month MONTH_ENUM = Month.DECEMBER;
  private static final int MONTH = 12;
  private static final int DAY = 21;

  private static final int HOUR = 2;
  private static final int MINUTE = 3;
  private static final int SECOND = 4;
  private static final int MILLISECOND = 5;
  // not a mistake, LocalDateTime.of take nanoseconds and not milliseconds
  private static final int NANOSECOND = 5000000;

  @Test
  public void testAtomizedLocalDateTime(){
    AtomizedLocalDateTime dateTime = AtomizedLocalDateTime.fromTemporalAccessor(LocalDateTime.of(YEAR, MONTH_ENUM, DAY, HOUR,
            MINUTE, SECOND, NANOSECOND));
    assertAtomizedLocalDateTime(dateTime, YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND);

    dateTime = AtomizedLocalDateTime.fromTemporalAccessor(LocalDateTime.of(YEAR, MONTH_ENUM, DAY, HOUR,
            MINUTE));
    assertAtomizedLocalDateTime(dateTime, YEAR, MONTH, DAY, HOUR, MINUTE, 0, 0);
    assertEquals(7, dateTime.getResolution());

    // this will probably not be supported ------
    dateTime = AtomizedLocalDateTime.fromTemporalAccessor(LocalTime.of(HOUR, MINUTE));
    assertAtomizedLocalDateTime(dateTime, null, null, null, HOUR, MINUTE, 0, 0);
    assertEquals(4, dateTime.getResolution());

    dateTime = AtomizedLocalDateTime.fromTemporalAccessor(LocalTime.of(HOUR, MINUTE));
    assertAtomizedLocalDateTime(dateTime, null, null, null, HOUR, MINUTE, 0, 0);
    assertEquals(4, dateTime.getResolution());
    // end of this will probably not be supported ------

    // ensure it works with a LocalDate alone (without time component)
    dateTime = AtomizedLocalDateTime.fromTemporalAccessor(LocalDate.of(YEAR, MONTH_ENUM, DAY));
    assertAtomizedLocalDateTime(dateTime, YEAR, MONTH, DAY, null, null, null, null);
  }

  @Test
  public void testAtomizedLocalDateEquals(){
    AtomizedLocalDateTime date1 = AtomizedLocalDateTime.fromTemporalAccessor(LocalDate.of(YEAR, MONTH_ENUM, DAY));
    AtomizedLocalDateTime date2 = AtomizedLocalDateTime.fromTemporalAccessor(LocalDate.of(YEAR, MONTH_ENUM, DAY));
    assertEquals(date1, date2);
  }

  private static void assertAtomizedLocalDateTime(AtomizedLocalDateTime dateTime, Integer year, Integer month,
                                                  Integer day, Integer hour, Integer minute, Integer second,
                                                  Integer millisecond){
    assertEquals(year, dateTime.getYear());
    assertEquals(month, dateTime.getMonth());
    assertEquals(day, dateTime.getDay());
    assertEquals(hour, dateTime.getHour());
    assertEquals(minute, dateTime.getMinute());
    assertEquals(second, dateTime.getSecond());
    assertEquals(millisecond, dateTime.getMillisecond());
  }
}
