package org.gbif.common.parsers.core;

import org.gbif.api.vocabulary.Rank;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class EnumParserTest {

  @Test
  public void testNormalize() throws Exception {
    EnumParser parser = new EnumParser(Rank.class, true);
    assertEquals("HALLO", parser.normalize("hàlló"));

    assertEquals("HALLO", parser.normalize("Hallo"));
    assertEquals("HALLO", parser.normalize(" Hallo  "));
    assertEquals("HALLO", parser.normalize("HallO"));
    assertEquals("HALLO", parser.normalize("Hallo!"));
    assertEquals("HALLO", parser.normalize("(Hallo)"));
    assertEquals("HALLO", parser.normalize("(Hällö"));

    assertEquals("HELLOBERTOCKO", parser.normalize("Hello Bértöçkø"));
  }

}
