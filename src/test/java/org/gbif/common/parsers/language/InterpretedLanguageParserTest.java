package org.gbif.common.parsers.language;


import org.gbif.api.vocabulary.Language;

import junit.framework.TestCase;
import org.junit.Test;

public class InterpretedLanguageParserTest extends TestCase {

  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    InterpretedLanguageParser parser = InterpretedLanguageParser.getInstance();
    assertEquals(Language.GERMAN, parser.parse("German").getPayload().getInterpreted());
    assertEquals(Language.GERMAN, parser.parse("deutsch").getPayload().getInterpreted());
    assertEquals(Language.GERMAN, parser.parse("de").getPayload().getInterpreted());
    assertEquals(Language.GERMAN, parser.parse("ger").getPayload().getInterpreted());

    assertEquals(Language.BASQUE, parser.parse("baq").getPayload().getInterpreted());
  }

}
