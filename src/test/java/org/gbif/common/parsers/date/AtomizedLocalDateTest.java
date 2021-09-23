package org.gbif.common.parsers.date;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link AtomizedLocalDate}.
 *
 */
public class AtomizedLocalDateTest {

  private static final int YEAR = 1978;
  private static final Month MONTH_ENUM = Month.DECEMBER;
  private static final int MONTH = 12;
  private static final int DAY = 21;

  @Test
  public void testAtomizedLocalDate(){
    AtomizedLocalDate date = AtomizedLocalDate.fromTemporalAccessor(LocalDate.of(YEAR, MONTH_ENUM, DAY));
    assertAtomizedLocalDate(date, YEAR, MONTH, DAY);

    date = AtomizedLocalDate.fromTemporalAccessor(YearMonth.of(YEAR, MONTH_ENUM));
    assertAtomizedLocalDate(date, YEAR, MONTH, null);

    date = AtomizedLocalDate.fromTemporalAccessor(Year.of(YEAR));
    assertAtomizedLocalDate(date, YEAR, null, null);
  }

  @Test
  public void testAtomizedLocalDateEquals(){
    AtomizedLocalDate date1 = AtomizedLocalDate.fromTemporalAccessor(LocalDate.of(YEAR, MONTH_ENUM, DAY));
    AtomizedLocalDate date2 = AtomizedLocalDate.fromTemporalAccessor(LocalDate.of(YEAR, MONTH_ENUM, DAY));
    assertEquals(date1, date2);
  }

  private static void assertAtomizedLocalDate(AtomizedLocalDate date, Integer y, Integer m, Integer d){
    assertEquals(y, date.getYear());
    assertEquals(m, date.getMonth());
    assertEquals(d, date.getDay());
  }
}
