package org.gbif.common.parsers.core;

import org.gbif.api.vocabulary.Rank;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
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
    assertEquals("HELLO_MR6", parser.normalize("hello_mr6"));
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

}
