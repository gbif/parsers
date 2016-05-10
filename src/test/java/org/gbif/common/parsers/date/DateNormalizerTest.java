package org.gbif.common.parsers.date;

import org.junit.Test;
import org.threeten.bp.Month;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests related to {@link DatePartsNormalizer}
 */
public class DateNormalizerTest {

  /**
   * This happens when integer are exported as float/double by a data publisher.
   */
  @Test
  public void testFloatNormalization(){
    DatePartsNormalizer.NormalizedYearMonthDay result = DatePartsNormalizer.normalize("1975.0", "2.0", "1.0");
    assertEquals(new Integer(1), result.getDay());
    assertEquals(Month.FEBRUARY.getValue(), result.getMonth().intValue());
    assertEquals(new Integer(1975), result.getYear());
  }

  @Test
  public void testNormalizationWithSpaces(){
    DatePartsNormalizer.NormalizedYearMonthDay result = DatePartsNormalizer.normalize("1975 ", " 2 ", " 1");
    assertEquals(new Integer(1), result.getDay());
    assertEquals(Month.FEBRUARY.getValue(), result.getMonth().intValue());
    assertEquals(new Integer(1975), result.getYear());
  }

  @Test
  public void testMonthNameNormalization(){
    assertEquals(Month.JANUARY.getValue(), DatePartsNormalizer.monthNameToNumerical("January").intValue());
    assertEquals(Month.JANUARY.getValue(), DatePartsNormalizer.monthNameToNumerical("JANUARY").intValue());
    assertEquals(Month.JANUARY.getValue(), DatePartsNormalizer.monthNameToNumerical("Jan").intValue());
    assertEquals(Month.JANUARY.getValue(), DatePartsNormalizer.monthNameToNumerical("JAN.").intValue());
    assertEquals(Month.DECEMBER.getValue(), DatePartsNormalizer.monthNameToNumerical("DEC").intValue());
    assertEquals(Month.APRIL.getValue(), DatePartsNormalizer.monthNameToNumerical("April").intValue());
    assertEquals(Month.SEPTEMBER.getValue(), DatePartsNormalizer.monthNameToNumerical("Sept").intValue());
    assertEquals(Month.JUNE.getValue(), DatePartsNormalizer.monthNameToNumerical("June").intValue());
    assertEquals(Month.JUNE.getValue(), DatePartsNormalizer.monthNameToNumerical("Jun").intValue());
    assertEquals(Month.JUNE.getValue(), DatePartsNormalizer.monthNameToNumerical("JUNE").intValue());
    assertEquals(Month.NOVEMBER.getValue(), DatePartsNormalizer.monthNameToNumerical("November").intValue());
  }

  @Test
  public void testDiscardedDateParts(){

    DatePartsNormalizer.NormalizedYearMonthDay result = DatePartsNormalizer.normalize("a", "5", "3");
    assertTrue(result.yDiscarded());
    assertNull(result.getYear());
    assertNotNull(result.getMonth());

    result = DatePartsNormalizer.normalize("2000", "a", "3");
    assertTrue(result.mDiscarded());
    assertNull(result.getMonth());
    assertNotNull(result.getYear());

    result = DatePartsNormalizer.normalize("2000", "5", "a");
    assertTrue(result.dDiscarded());
    assertNull(result.getDay());
    assertNotNull(result.getYear());

    // empty String, null and \\N should NOT be considered as "discarded"
    // they simply represent "not provided"
    result = DatePartsNormalizer.normalize("2000", "5", "");
    assertFalse(result.dDiscarded());
    assertNull(result.getDay());

    result = DatePartsNormalizer.normalize("2000", "5", null);
    assertFalse(result.dDiscarded());
    assertNull(result.getDay());

    result = DatePartsNormalizer.normalize("2000", "5", "\\N");
    assertFalse(result.dDiscarded());
    assertNull(result.getDay());
  }
}
