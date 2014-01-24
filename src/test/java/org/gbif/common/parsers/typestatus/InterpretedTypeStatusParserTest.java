package org.gbif.common.parsers.typestatus;

import org.gbif.api.vocabulary.TypeStatus;
import org.gbif.common.parsers.InterpretedParserTest;

import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Resources;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class InterpretedTypeStatusParserTest extends InterpretedParserTest<TypeStatus> {

  public InterpretedTypeStatusParserTest() {
    super(InterpretedTypeStatusParser.getInstance());
  }

  /**
   * This ensures that ALL enum values are at least parsable by the name they
   * are created with.
   */
  @Test
  public void testCompleteness() {
    for (TypeStatus t : TypeStatus.values()) {
      System.out.println("Testing [" + t.name()
                         + "].  Failures below might indicate new type status values added to the TypeStatus enum but not to the parse file");
      assertParseSuccess(parser, t, t.name());
    }
  }

  @Test
  public void testFailures() {
    assertParseFailure(parser, null);
    assertParseFailure(parser, "");
    assertParseFailure(parser, "Tim");
  }


  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(parser, TypeStatus.ALLOTYPE, "allotype");
    assertParseSuccess(parser, TypeStatus.HOLOTYPE, "Holotype of Abies alba");
    assertParseSuccess(parser, TypeStatus.PARATYPE, "paratype(s)");
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
