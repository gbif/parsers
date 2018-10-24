package org.gbif.common.parsers;

import org.gbif.api.vocabulary.TypeStatus;

import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Resources;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TypeStatusParserTest extends ParserTestBase<TypeStatus> {

  public TypeStatusParserTest() {
    super(TypeStatusParser.getInstance());
  }

  /**
   * This ensures that ALL enum values are at least parsable by the name they
   * are created with.
   */
  @Test
  public void testCompleteness() {
    for (TypeStatus t : TypeStatus.values()) {
      assertParseSuccess(t, t.name());
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
    assertParseSuccess(TypeStatus.ALLOTYPE, "allotype");
    assertParseSuccess(TypeStatus.HOLOTYPE, "Holotype of Abies alba");
    assertParseSuccess(TypeStatus.HOLOTYPE, "Holotype for Abies alba");
    assertParseSuccess(TypeStatus.PARATYPE, "paratype(s)");
    assertParseSuccess(TypeStatus.PARATYPE, "Parátipo");
  }

  @Test
  public void testFileCoverage() throws IOException {
    // parses all values in our test file (generated from real occurrence data) and verifies we never get worse at parsing
    Resources.readLines(Resources.getResource("parse/typestatus/type_status.txt"), Charset.forName("UTF8"), this);
    BatchParseResult result = getResult();
    System.out.println(String.format("%s out of %s lines failed to parse", result.failed, result.total));
    assertTrue(result.failed <= 26813);
  }

}
