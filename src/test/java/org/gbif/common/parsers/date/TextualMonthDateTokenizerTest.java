/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.common.parsers.date;

import java.util.function.Function;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import static org.gbif.common.parsers.utils.CSVBasedAssertions.assertTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(new TextualMonthDateTokenizer.DateToken(int2Token, TextualMonthDateTokenizer.TokenType.INT_2), dateTokens.getToken(TextualMonthDateTokenizer.TokenType.INT_2),
            "Raw: " + raw);
        assertEquals(new TextualMonthDateTokenizer.DateToken(textToken, TextualMonthDateTokenizer.TokenType.TEXT), dateTokens.getToken(TextualMonthDateTokenizer.TokenType.TEXT),
            "Raw: " + raw);
        assertEquals(new TextualMonthDateTokenizer.DateToken(int4Token, TextualMonthDateTokenizer.TokenType.INT_4), dateTokens.getToken(TextualMonthDateTokenizer.TokenType.INT_4),
            "Raw: " + raw);

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
