package org.gbif.common.parsers.date;

import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link TextualMonthDateTokenizer }.
 */
public class TextualMonthDateTokenizerTest {

  private static final String TEST_FILE = "parse/date/TextualMonthDateTokenizerTests.txt";
  private static final String COLUMN_SEPARATOR = ";";
  private static final String COMMENT_MARKER = "#";

  private static final int RAW_VAL_IDX = 0;
  private static final int INT_4_IDX = 1;
  private static final int TEXT_IDX = 2;
  private static final int INT_2_IDX = 3;

  private TextualMonthDateTokenizer DATE_TOKENIZER = new TextualMonthDateTokenizer();

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
        assertEquals(new TextualMonthDateTokenizer.DateToken(int2Token, TextualMonthDateTokenizer.TokenType.INT_2),
                dateTokens.getToken(TextualMonthDateTokenizer.TokenType.INT_2));
        assertEquals(new TextualMonthDateTokenizer.DateToken(textToken, TextualMonthDateTokenizer.TokenType.TEXT),
                dateTokens.getToken(TextualMonthDateTokenizer.TokenType.TEXT));
        assertEquals(new TextualMonthDateTokenizer.DateToken(int4Token, TextualMonthDateTokenizer.TokenType.INT_4),
                dateTokens.getToken(TextualMonthDateTokenizer.TokenType.INT_4));

        return null;
      }
    });
  }

  /**
   * Utility function to run assertions received as Function on each rows of an input file.
   *
   * @param filepath
   * @param assertRow
   */
  private void assertTestFile(String filepath, Function<String[], Void> assertRow) {
    File testInputFile = FileUtils.getClasspathFile(filepath);
    try{
      CSVReader csv = CSVReaderFactory.build(testInputFile, COLUMN_SEPARATOR, true);
      for (String[] row : csv) {
        if (row == null || row[0].startsWith(COMMENT_MARKER)) {
          continue;
        }
        assertRow.apply(row);
      }
    } catch (IOException e) {
      fail("Problem reading testFile " + filepath + " " + e.getMessage());
    }
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
