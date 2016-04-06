package org.gbif.common.parsers.date;

import org.junit.Test;
import org.threeten.bp.Month;

import static org.junit.Assert.assertEquals;

/**
 * Tests related to {@link DateNormalizer}
 */
public class DateNormalizerTest {

  /**
   * This happens when integer are exported as float/double by a data publisher.
   */
  @Test
  public void testFloatNormalization(){
    DateNormalizer.NormalizedYearMonthDay result = DateNormalizer.normalize("1975.0", "2.0", "1.0");
    assertEquals(new Integer(1), result.getDay());
    assertEquals(Month.FEBRUARY.getValue(), result.getMonth().intValue());
    assertEquals(new Integer(1975), result.getYear());
  }

  @Test
  public void testMonthNameNormalization(){
    assertEquals(Month.JANUARY.getValue(), DateNormalizer.monthNameToNumerical("January").intValue());
    assertEquals(Month.JANUARY.getValue(), DateNormalizer.monthNameToNumerical("JANUARY").intValue());
    assertEquals(Month.JANUARY.getValue(), DateNormalizer.monthNameToNumerical("Jan").intValue());
    assertEquals(Month.JANUARY.getValue(), DateNormalizer.monthNameToNumerical("JAN.").intValue());
    assertEquals(Month.DECEMBER.getValue(), DateNormalizer.monthNameToNumerical("DEC").intValue());
    assertEquals(Month.APRIL.getValue(), DateNormalizer.monthNameToNumerical("April").intValue());
    assertEquals(Month.SEPTEMBER.getValue(), DateNormalizer.monthNameToNumerical("Sept").intValue());
    assertEquals(Month.JUNE.getValue(), DateNormalizer.monthNameToNumerical("June").intValue());
    assertEquals(Month.JUNE.getValue(), DateNormalizer.monthNameToNumerical("Jun").intValue());
    assertEquals(Month.JUNE.getValue(), DateNormalizer.monthNameToNumerical("JUNE").intValue());
    assertEquals(Month.NOVEMBER.getValue(), DateNormalizer.monthNameToNumerical("November").intValue());
  }
}
