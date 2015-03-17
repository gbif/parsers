package org.gbif.common.parsers;

import org.junit.Test;

/**
 *
 */
public class TypifiedNameParserTest extends ParserTestBase<String> {

  public TypifiedNameParserTest() {
    super(TypifiedNameParser.getInstance());
  }

  @Test
  public void testParse() throws Exception {
    assertParseSuccess("Abies alba", null, " Holotype of Abies alba");
    assertParseSuccess("Dianthus fruticosus subsp. amorginus Runemark", null, "Holotype of Dianthus fruticosus ssp. amorginus Runemark");
    assertParseSuccess("Abies alba", null, " Holotype of: Abies alba");
    assertParseSuccess("Abies alba", null, " Holotype of  Abies alba.");

    assertParseFailure("Part of Holotype");
    assertParseFailure("Figured Specimen");
//    assertParseFailure("Cast of Figured Specimen");
    assertParseFailure("Cast of holotype");
    assertParseFailure("Cast of syntype");
    assertParseFailure("mark");
  }

}
