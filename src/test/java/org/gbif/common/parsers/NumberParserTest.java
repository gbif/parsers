package org.gbif.common.parsers;

import org.gbif.common.parsers.NumberParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class NumberParserTest {

  @Test
  public void testParseDouble() throws Exception {
    assertNull(NumberParser.parseDouble("312,dsfds"));

    assertEquals((Double)2d, NumberParser.parseDouble("2"));
    assertEquals((Double)2.123d, NumberParser.parseDouble("2.123"));
    assertEquals((Double)2.123d, NumberParser.parseDouble("2,123"));
    assertEquals((Double) (-2.123d), NumberParser.parseDouble("-2,123"));
    assertEquals((Double) (-122.12345d), NumberParser.parseDouble("-122.12345"));
    assertEquals((Double) (22788130.9993d), NumberParser.parseDouble("22.788.130,9993"));
    assertEquals((Double) (12300d), NumberParser.parseDouble("1.23E4"));

    assertNull(NumberParser.parseDouble(null));
    assertNull(NumberParser.parseDouble(""));
    assertNull(NumberParser.parseDouble(" "));
    assertNull(NumberParser.parseDouble("ds"));
    assertNull(NumberParser.parseDouble("312,dsfds"));
    assertNull(NumberParser.parseDouble("43-1"));
    assertNull(NumberParser.parseDouble("43,112,321"));

  }
}
