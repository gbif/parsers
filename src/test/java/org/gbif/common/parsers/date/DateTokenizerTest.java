package org.gbif.common.parsers.date;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@DateTokenizer}.
 */
public class DateTokenizerTest {

  @Test
  public void testDateTokenizer(){
    DateTokenizer dt = new DateTokenizer();

    List<DateTokenizer.DateToken> dateParts = dt.tokenize("2nd jan. 2018");
    assertEquals(3, dateParts.size());
    assertEquals(new DateTokenizer.DateToken("2", DateTokenizer.TokenType.POSSIBLE_DAY), dateParts.get(0));
    assertEquals(new DateTokenizer.DateToken("jan.", DateTokenizer.TokenType.POSSIBLE_TEXT_MONTH), dateParts.get(1));
    assertEquals(new DateTokenizer.DateToken("2018", DateTokenizer.TokenType.POSSIBLE_YEAR), dateParts.get(2));

    dateParts = dt.tokenize("2018, March 1st");
    assertEquals(3, dateParts.size());
    assertEquals(new DateTokenizer.DateToken("2018", DateTokenizer.TokenType.POSSIBLE_YEAR), dateParts.get(0));
    assertEquals(new DateTokenizer.DateToken("March", DateTokenizer.TokenType.POSSIBLE_TEXT_MONTH), dateParts.get(1));
    assertEquals(new DateTokenizer.DateToken("1", DateTokenizer.TokenType.POSSIBLE_DAY), dateParts.get(2));

  }
}
