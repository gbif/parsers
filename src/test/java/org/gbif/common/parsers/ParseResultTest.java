package org.gbif.common.parsers;

import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ParseResultTest {

  @Test
  public void testSuccess() {
    // check with basic string payload
    assertNotNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")));
    assertNotNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")).getPayload());
    assertEquals(String.class,
      ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")).getPayload().getClass());
    assertEquals(ParseResult.CONFIDENCE.DEFINITE,
      ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")).getConfidence());
    assertEquals(ParseResult.STATUS.SUCCESS,
      ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")).getStatus());
    assertNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")).getError());

    // check generics with Date payload
    assertNotNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()));
    assertNotNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()).getPayload());
    assertEquals(Date.class, ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()).getPayload().getClass());
    assertEquals(ParseResult.CONFIDENCE.DEFINITE,
      ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()).getConfidence());
    assertEquals(ParseResult.STATUS.SUCCESS,
      ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()).getStatus());
    assertNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()).getError());

  }

  @Test
  public void testFail() {
    assertNotNull(ParseResult.fail());
    assertEquals(ParseResult.STATUS.FAIL, ParseResult.fail().getStatus());
    assertNull(ParseResult.fail().getConfidence());
    assertNull(ParseResult.fail().getError());
    assertNull(ParseResult.fail().getPayload());
  }

  @Test
  public void testUnknownError() {
    assertNotNull(ParseResult.error());
    assertEquals(ParseResult.STATUS.ERROR, ParseResult.error().getStatus());
    assertNull(ParseResult.error().getConfidence());
    assertNull(ParseResult.error().getError());
    assertNull(ParseResult.error().getPayload());
  }

  @Test
  public void testError() {
    assertNotNull(ParseResult.error(new RuntimeException("Bingo")));
    assertEquals(ParseResult.STATUS.ERROR, ParseResult.error(new RuntimeException("Bingo")).getStatus());
    assertNull(ParseResult.error(new RuntimeException("Bingo")).getConfidence());
    assertNotNull(ParseResult.error(new RuntimeException("Bingo")).getError());
    assertEquals(RuntimeException.class, ParseResult.error(new RuntimeException("Bingo")).getError().getClass());
    assertEquals("Bingo", ParseResult.error(new RuntimeException("Bingo")).getError().getMessage());
    assertNull(ParseResult.error(new RuntimeException("Bingo")).getPayload());
  }
}
