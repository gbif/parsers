/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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

import java.time.Month;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests related to {@link DatePartsNormalizer}
 */
public class DatePartsNormalizerTest {

  private static DatePartsNormalizer NORMALIZER = DatePartsNormalizer.newInstance();

  /**
   * This happens when integer are exported as float/double by a data publisher.
   */
  @Test
  public void testFloatNormalization(){
    DatePartsNormalizer.NormalizedYearMonthDay result = NORMALIZER.normalize("1975.0", "2.0", "1.0");
    assertEquals(new Integer(1), result.getDay());
    assertEquals(Month.FEBRUARY.getValue(), result.getMonth().intValue());
    assertEquals(new Integer(1975), result.getYear());
  }

  @Test
  public void testNormalizationWithSpaces(){
    DatePartsNormalizer.NormalizedYearMonthDay result = NORMALIZER.normalize("1975 ", " 2 ", " 1");
    assertEquals(new Integer(1), result.getDay());
    assertEquals(Month.FEBRUARY.getValue(), result.getMonth().intValue());
    assertEquals(new Integer(1975), result.getYear());
  }

  @Test
  public void testMonthNameNormalization(){
    assertEquals(Month.JANUARY.getValue(), NORMALIZER.monthNameToNumerical("January").intValue());
    assertEquals(Month.JANUARY.getValue(), NORMALIZER.monthNameToNumerical("JANUARY").intValue());
    assertEquals(Month.JANUARY.getValue(), NORMALIZER.monthNameToNumerical("Jan").intValue());
    assertEquals(Month.JANUARY.getValue(), NORMALIZER.monthNameToNumerical("JAN.").intValue());
    assertEquals(Month.DECEMBER.getValue(), NORMALIZER.monthNameToNumerical("DEC").intValue());
    assertEquals(Month.APRIL.getValue(), NORMALIZER.monthNameToNumerical("April").intValue());
    assertEquals(Month.SEPTEMBER.getValue(), NORMALIZER.monthNameToNumerical("Sept").intValue());
    assertEquals(Month.JUNE.getValue(), NORMALIZER.monthNameToNumerical("June").intValue());
    assertEquals(Month.JUNE.getValue(), NORMALIZER.monthNameToNumerical("Jun").intValue());
    assertEquals(Month.JUNE.getValue(), NORMALIZER.monthNameToNumerical("JUNE").intValue());
    assertEquals(Month.NOVEMBER.getValue(), NORMALIZER.monthNameToNumerical("November").intValue());
  }

  @Test
  public void testDiscardedDateParts(){

    DatePartsNormalizer.NormalizedYearMonthDay result = NORMALIZER.normalize("a", "5", "3");
    assertTrue(result.yDiscarded());
    assertNull(result.getYear());
    assertNotNull(result.getMonth());

    result = NORMALIZER.normalize("2000", "a", "3");
    assertTrue(result.mDiscarded());
    assertNull(result.getMonth());
    assertNotNull(result.getYear());

    result = NORMALIZER.normalize("2000", "5", "a");
    assertTrue(result.dDiscarded());
    assertNull(result.getDay());
    assertNotNull(result.getYear());

    // empty String, null and \\N should NOT be considered as "discarded"
    // they simply represent "not provided"
    result = NORMALIZER.normalize("2000", "5", "");
    assertFalse(result.dDiscarded());
    assertNull(result.getDay());

    result = NORMALIZER.normalize("2000", "5", null);
    assertFalse(result.dDiscarded());
    assertNull(result.getDay());

    result = NORMALIZER.normalize("2000", "5", "\\N");
    assertFalse(result.dDiscarded());
    assertNull(result.getDay());
  }
}
