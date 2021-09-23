package org.gbif.common.parsers;

import org.gbif.common.parsers.NumberParser;

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
