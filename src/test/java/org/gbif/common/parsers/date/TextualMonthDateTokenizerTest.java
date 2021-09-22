package org.gbif.common.parsers.date;

import javax.annotation.Nullable;

import org.junit.Test;

import java.util.function.Function;

import static org.gbif.common.parsers.utils.CSVBasedAssertions.assertTestFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link TextualMonthDateTokenizer }.
 */
public class TextualMonthDateTokenizerTest {

  private static final String TEST_FILE = "parse/date/textual_month_date_tokenizer_tests.txt";

  private static final int RAW_VAL_IDX = 0;
  private static final int INT_4_IDX = 1;
  private static final int TEXT_IDX = 2;
  private static final int INT_2_IDX = 3;

  private TextualMonthDateTokenizer DATE_TOKENIZER = TextualMonthDateTokenizer.newInstance();

  @Test
  public void testDateTokenizerFromFile(){

    assertTestFile(TEST_FILE, new Function<String[], Void>(){

      @Nullable
      @Override
      public Void apply(String[] row) {

        String raw = row[RAW_VAL_IDX];
        String int4Token = row[INT_4_IDX];
        String textToken = row[TEXT_IDX];
        String int2Token = row[INT_2_IDX];

        TextualMonthDateTokenizer.DateTokens dateTokens = DATE_TOKENIZER.tokenize(raw);
        assertEquals("Raw: " + raw, new TextualMonthDateTokenizer.DateToken(int2Token, TextualMonthDateTokenizer.TokenType.INT_2),
                dateTokens.getToken(TextualMonthDateTokenizer.TokenType.INT_2));
        assertEquals("Raw: " + raw, new TextualMonthDateTokenizer.DateToken(textToken, TextualMonthDateTokenizer.TokenType.TEXT),
                dateTokens.getToken(TextualMonthDateTokenizer.TokenType.TEXT));
        assertEquals("Raw: " + raw, new TextualMonthDateTokenizer.DateToken(int4Token, TextualMonthDateTokenizer.TokenType.INT_4),
                dateTokens.getToken(TextualMonthDateTokenizer.TokenType.INT_4));

        return null;
      }
    });
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
