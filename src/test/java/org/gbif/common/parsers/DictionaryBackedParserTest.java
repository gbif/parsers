package org.gbif.common.parsers;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DictionaryBackedParserTest {

  @Test
  public void testParse() {
    List<KeyValue<String, Integer>> source = new ArrayList<KeyValue<String, Integer>>();
    source.add(new KeyValue<String, Integer>("Tim", 32));
    source.add(new KeyValue<String, Integer>("Markus", 38));
    source.add(new KeyValue<String, Integer>("Jose", 28));
    DictionaryBackedParser<String, Integer> dbp = new DictionaryBackedParser<String, Integer>(false);
    dbp.init(source.iterator());

    assertParsed(dbp, "Tim", 32);
    assertParsed(dbp, "TIM", 32);
    assertParsed(dbp, "tIm", 32);
    assertParsed(dbp, "Markus", 38);
    assertParsed(dbp, "MarKUS", 38);
    assertParsed(dbp, "Jose", 28);
    assertEquals(ParseResult.STATUS.FAIL, dbp.parse("Lars").getStatus());
  }

  private void assertParsed(DictionaryBackedParser<String, Integer> dbp, String input, Integer payload) {
    assertNotNull(dbp.parse(input));
    assertEquals(ParseResult.STATUS.SUCCESS, dbp.parse(input).getStatus());
    assertEquals(ParseResult.CONFIDENCE.DEFINITE, dbp.parse(input).getConfidence());
    assertEquals(payload, dbp.parse(input).getPayload());
  }

}
