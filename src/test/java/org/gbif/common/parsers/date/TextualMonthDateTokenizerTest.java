package org.gbif.common.parsers.date;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link TextualMonthDateTokenizer }.
 */
public class TextualMonthDateTokenizerTest {

  private TextualMonthDateTokenizer DATE_TOKENIZER = new TextualMonthDateTokenizer();

  @Test
  public void testDateTokenizer(){

    TextualMonthDateTokenizer.DateTokens dateTokens = DATE_TOKENIZER.tokenize("2nd jan. 2018");

    assertEquals(new TextualMonthDateTokenizer.DateToken("2", TextualMonthDateTokenizer.TokenType.INT_2),
            dateTokens.getToken(TextualMonthDateTokenizer.TokenType.INT_2));
    assertEquals(new TextualMonthDateTokenizer.DateToken("jan.", TextualMonthDateTokenizer.TokenType.TEXT),
            dateTokens.getToken(TextualMonthDateTokenizer.TokenType.TEXT));
    assertEquals(new TextualMonthDateTokenizer.DateToken("2018", TextualMonthDateTokenizer.TokenType.INT_4),
            dateTokens.getToken(TextualMonthDateTokenizer.TokenType.INT_4));

    dateTokens = DATE_TOKENIZER.tokenize("2018, March 1st");
    assertEquals(new TextualMonthDateTokenizer.DateToken("2018", TextualMonthDateTokenizer.TokenType.INT_4),
            dateTokens.getToken(TextualMonthDateTokenizer.TokenType.INT_4));
    assertEquals(new TextualMonthDateTokenizer.DateToken("March", TextualMonthDateTokenizer.TokenType.TEXT),
            dateTokens.getToken(TextualMonthDateTokenizer.TokenType.TEXT));
    assertEquals(new TextualMonthDateTokenizer.DateToken("1", TextualMonthDateTokenizer.TokenType.INT_2),
            dateTokens.getToken(TextualMonthDateTokenizer.TokenType.INT_2));
  }

  @Test
  public void testDateTokenizerWithNullAndEmpty(){
    assertNull(DATE_TOKENIZER.tokenize(null));
    assertNull(DATE_TOKENIZER.tokenize(""));
  }

  /**
   * Test behavior when no textual month is provided.
   * Month and Day will be returned as TokenType.INT_2, one of the 2 will fall under the discarded tokens.
   * See {@link TextualMonthDateTokenizer } for more details.
   */
  @Test
  public void testDateTokenizerWithISODate(){
    TextualMonthDateTokenizer.DateTokens dateTokens = DATE_TOKENIZER.tokenize("2018-01-02");
    assertEquals(new TextualMonthDateTokenizer.DateToken("2018", TextualMonthDateTokenizer.TokenType.INT_4),
            dateTokens.getToken(TextualMonthDateTokenizer.TokenType.INT_4));
    assertTrue(dateTokens.containsDiscardedTokens());
  }
}
