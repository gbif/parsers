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
package org.gbif.common.parsers.core;

import org.gbif.api.vocabulary.Rank;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EnumParserTest {

  @Test
  public void testNormalizeDigits() throws Exception {
    EnumParser parser = new EnumParser(Rank.class, true);
    assertEquals("HALLO", parser.normalize("hàlló"));

    assertEquals("HALLO", parser.normalize("Hallo"));
    assertEquals("HALLO", parser.normalize(" Hallo  "));
    assertEquals("HALLO", parser.normalize("HallO"));
    assertEquals("HALLO", parser.normalize("Hallo!"));
    assertEquals("HALLO", parser.normalize("(Hallo)"));
    assertEquals("HALLO", parser.normalize("(Hällö"));

    assertEquals("H6", parser.normalize("(h6"));
    assertEquals("HELLOMR6", parser.normalize("hello_mr6"));
    assertEquals("HELLOBERTOCKO", parser.normalize("Hello Bértöçkø"));
  }

  @Test
  public void testNormalizeCharsOnly() throws Exception {
    EnumParser parser = new EnumParser(Rank.class, false);
    assertEquals("HALLO", parser.normalize("hàlló"));

    assertEquals("HALLO", parser.normalize("Hallo"));
    assertEquals("HALLO", parser.normalize(" Hallo  "));
    assertEquals("HALLO", parser.normalize("HallO"));
    assertEquals("HALLO", parser.normalize("Hallo!"));
    assertEquals("HALLO", parser.normalize("(Hallo)"));
    assertEquals("HALLO", parser.normalize("(Hällö"));

    assertEquals("H", parser.normalize("(h6"));
    assertEquals("HELLOMR", parser.normalize("hello_mr6"));
    assertEquals("HELLOBERTOCKO", parser.normalize("Hello Bértöçkø"));
  }

  @Test
  public void testNormalizeNotAvailableEtc() throws Exception {
    EnumParser parser = new EnumParser(Rank.class, true);

    assertEquals("NA", parser.normalize("NA"));
    assertEquals("NA", parser.normalize("/NA/"));
    assertNull(parser.normalize("N/A"));
    assertNull(parser.normalize("N/a"));
    assertNull(parser.normalize("n/a"));
    assertNull(parser.normalize("n/A"));
    assertNull(parser.normalize("n.a."));
    assertNull(parser.normalize("n.k."));
  }
}
