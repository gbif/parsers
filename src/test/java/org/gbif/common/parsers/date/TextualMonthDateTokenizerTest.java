package org.gbif.common.parsers.date;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link TextualMonthDateTokenizer }.
 */
public class TextualMonthDateTokenizerTest {

  private TextualMonthDateTokenizer DATE_TOKENIZER = new TextualMonthDateTokenizer();

  @Test
  public void testDateTokenizer(){

    List<TextualMonthDateTokenizer.DateToken> dateParts = DATE_TOKENIZER.tokenize("2nd jan. 2018");
    assertEquals(3, dateParts.size());
    assertEquals(new TextualMonthDateTokenizer.DateToken("2", TextualMonthDateTokenizer.TokenType.POSSIBLE_DAY), dateParts.get(0));
    assertEquals(new TextualMonthDateTokenizer.DateToken("jan.", TextualMonthDateTokenizer.TokenType.POSSIBLE_TEXT_MONTH), dateParts.get(1));
    assertEquals(new TextualMonthDateTokenizer.DateToken("2018", TextualMonthDateTokenizer.TokenType.POSSIBLE_YEAR), dateParts.get(2));

    dateParts = DATE_TOKENIZER.tokenize("2018, March 1st");
    assertEquals(3, dateParts.size());
    assertEquals(new TextualMonthDateTokenizer.DateToken("2018", TextualMonthDateTokenizer.TokenType.POSSIBLE_YEAR), dateParts.get(0));
    assertEquals(new TextualMonthDateTokenizer.DateToken("March", TextualMonthDateTokenizer.TokenType.POSSIBLE_TEXT_MONTH), dateParts.get(1));
    assertEquals(new TextualMonthDateTokenizer.DateToken("1", TextualMonthDateTokenizer.TokenType.POSSIBLE_DAY), dateParts.get(2));
  }


  /**
   * Test behavior when no textual month is provided.
   * Month will be returned as TokenType.POSSIBLE_DAY, see {@link TextualMonthDateTokenizer } for more details.
   */
  @Test
  public void testDateTokenizerWithISODate(){
    List<TextualMonthDateTokenizer.DateToken> dateParts = DATE_TOKENIZER.tokenize("2018-01-02");
    assertEquals(3, dateParts.size());
    assertEquals(new TextualMonthDateTokenizer.DateToken("2018", TextualMonthDateTokenizer.TokenType.POSSIBLE_YEAR), dateParts.get(0));
    assertEquals(new TextualMonthDateTokenizer.DateToken("01", TextualMonthDateTokenizer.TokenType.POSSIBLE_DAY), dateParts.get(1));
    assertEquals(new TextualMonthDateTokenizer.DateToken("02", TextualMonthDateTokenizer.TokenType.POSSIBLE_DAY), dateParts.get(2));
  }
}
