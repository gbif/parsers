package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.ParseResult;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class StringToDateParserTest {
  private static final Logger LOG = LoggerFactory.getLogger(DateParseUtilsTest.class);

  @Test
  public void testParse() throws Exception {
    StringToDateParser parser = new StringToDateParser();

    assertEquivalent("21/12/1978", parser.parse("211278"), ParseResult.CONFIDENCE.PROBABLE);
    assertEquivalent("21/12/1978", parser.parse("21/12/1978"), ParseResult.CONFIDENCE.DEFINITE);
    assertEquivalent("21/12/1978", parser.parse("21/12/78"), ParseResult.CONFIDENCE.DEFINITE);

    assertEquals(ParseResult.STATUS.FAIL, parser.parse("fsfgr/12/78").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("fsfgr").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("//").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("0-0-0").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("%$-s2").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("as-fds-sdf").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("88/88/88").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse(" ").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse(null).getStatus());
  }

  private void assertEquivalent(String expected, ParseResult<Date> found, ParseResult.CONFIDENCE c) {
    if (ParseResult.STATUS.ERROR == found.getStatus()) {
      LOG.warn("Unexpected error found in parsing", found.getError());
    }
    assertEquals(ParseResult.STATUS.SUCCESS, found.getStatus());
    assertEquals(c, found.getConfidence());
    assertNotNull(found.getPayload());
    try {
      assertEquals(DateUtils.parseDateStrictly(expected, new String[] {"dd/MM/yyyy"}), found.getPayload());
    } catch (ParseException e) {
      fail("Error in the test class.  Expected date is not in the correct format of dd/MM/yyyy:" + expected);
    }
  }
}