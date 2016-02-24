package org.gbif.common.parsers;

import org.gbif.api.vocabulary.TaxonomicStatus;

import org.junit.Test;

public class TaxStatusParserTest extends ParserTestBase<TaxonomicStatus> {

  public TaxStatusParserTest() {
    super(TaxStatusParser.getInstance());
  }

  /**
   * This ensures that ALL enum values are at least parsable by the name they
   * are created with.
   */
  @Test
  public void testCompleteness() {
    for (TaxonomicStatus t : TaxonomicStatus.values()) {
      assertParseSuccess(t, t.name());
      assertParseSuccess(t, t.name().toLowerCase());
      assertParseSuccess(t, t.name().replace("_", "").toLowerCase());
    }
  }

  @Test
  public void testFailures() {
    assertParseFailure(null);
    assertParseFailure("");
    assertParseFailure("Tim");
  }


  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(TaxonomicStatus.ACCEPTED, "a");
    assertParseSuccess(TaxonomicStatus.SYNONYM, "INValid");
    assertParseSuccess(TaxonomicStatus.HOMOTYPIC_SYNONYM, "is homotypic synonym of");
    assertParseSuccess(TaxonomicStatus.ACCEPTED, "NOME_ACEITO");

  }

}