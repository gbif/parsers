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
package org.gbif.common.parsers;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NumberParserTest {

  @Test
  public void testParseDouble() throws Exception {
    assertNull(NumberParser.parseDouble("312,dsfds"));

    assertEquals(2d, NumberParser.parseDouble("2"), 0.0);
    assertEquals(2.123d, NumberParser.parseDouble("2.123"), 0.0);
    assertEquals(-122.12345d, NumberParser.parseDouble("-122.12345"), 0.0);
    assertEquals(22788130.9993d, NumberParser.parseDouble("22.788.130,9993"), 0.0);
    assertEquals(12300d, NumberParser.parseDouble("1.23E4"), 0.0);

    // These should be parsing failures due to ambiguity, see issue 23.
    assertEquals(2.123d, NumberParser.parseDouble("2,123"), 0.0);
    assertEquals(-2.123d, NumberParser.parseDouble("-2,123"), 0.0);

    // These are unambiguous, and could be accepted
    // assertEquals(2.123d, NumberParser.parseDouble("2,123.0"), 0.0);
    // assertEquals(2.123d, NumberParser.parseDouble("2.123,0"), 0.0);

    assertNull(NumberParser.parseDouble(null));
    assertNull(NumberParser.parseDouble(""));
    assertNull(NumberParser.parseDouble(" "));
    assertNull(NumberParser.parseDouble("ds"));
    assertNull(NumberParser.parseDouble("312,dsfds"));
    assertNull(NumberParser.parseDouble("43-1"));
    assertNull(NumberParser.parseDouble("43,112,321"));

  }
}
