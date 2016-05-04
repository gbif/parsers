package org.gbif.common.parsers.date.threeten;

import org.gbif.common.parsers.core.ParseResult;
import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Month;
import org.threeten.bp.Year;
import org.threeten.bp.YearMonth;
import org.threeten.bp.temporal.TemporalAccessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Unit testing for ThreeTenNumericalDateParser.
 *
 */
public class ThreeTenNumericalDateParserTest {

  private static final String LOCALDATE_TEST_FILE = "parse/date/ThreeTenLocalDateTests.txt";
  private static final String LOCALDATETIME_TEST_FILE = "parse/date/ThreeTenLocalDateTimeTests.txt";
  private static final String BADDATE_TEST_FILE = "parse/date/ThreeTenBadDateTests.txt";
  private static final String COLUMN_SEPARATOR = ";";
  private static final String COMMENT_MARKER = "#";

  private static final int RAW_VAL_IDX = 0;
  private static final int YEAR_VAL_IDX = 1;
  private static final int MONTH_VAL_IDX = 2;
  private static final int DAY_VAL_IDX = 3;
  private static final int HOUR_VAL_IDX = 4;
  private static final int MIN_VAL_IDX = 5;
  private static final int SEC_VAL_IDX = 6;

  private static final ThreeTenNumericalDateParser PARSER = ThreeTenNumericalDateParser.getParser();

  @Test
  public void testLocalDateFromFile() {
    assertTestFile(LOCALDATE_TEST_FILE,
            new Function<String[], Void>() {
              @Nullable
              @Override
              public Void apply(@Nullable String[] row) {
                String raw = row[RAW_VAL_IDX];
                try {
                  int year = Integer.parseInt(row[YEAR_VAL_IDX]);
                  int month = Integer.parseInt(row[MONTH_VAL_IDX]);
                  int day = Integer.parseInt(row[DAY_VAL_IDX]);
                  ParseResult<TemporalAccessor> result = PARSER.parse(raw);
                  assertNotNull(raw + " generated null payload", result.getPayload());
                  assertEquals("Test file rawValue: " + raw, LocalDate.of(year, month, day),
                          LocalDate.from(result.getPayload()));
                }
                catch (NumberFormatException nfEx){
                  fail("Error while parsing the test input file content." + nfEx.getMessage());
                }
                return null;
              }
            });
  }

  @Test
  public void testLocalDateTimeFromFile() {
    assertTestFile(LOCALDATETIME_TEST_FILE,
            new Function<String[], Void>() {
              @Nullable
              @Override
              public Void apply(@Nullable String[] row) {
                String raw = row[RAW_VAL_IDX];
                try {
                  int year = Integer.parseInt(row[YEAR_VAL_IDX]);
                  int month = Integer.parseInt(row[MONTH_VAL_IDX]);
                  int day = Integer.parseInt(row[DAY_VAL_IDX]);
                  int hour = Integer.parseInt(row[HOUR_VAL_IDX]);
                  int minute = Integer.parseInt(row[MIN_VAL_IDX]);
                  int second = Integer.parseInt(row[SEC_VAL_IDX]);

                  ParseResult<TemporalAccessor> result = PARSER.parse(raw);
                  assertNotNull(raw + " generated null payload", result.getPayload());

                  assertEquals("Test file rawValue: " + raw, LocalDateTime.of(year, month, day, hour, minute, second),
                          LocalDateTime.from(result.getPayload()));
                } catch (NumberFormatException nfEx) {
                  fail("Error while parsing the test input file content." + nfEx.getMessage());
                }
                return null;
              }
            });
  }

  @Test
  public void testBadDateFromFile() {
    assertTestFile(BADDATE_TEST_FILE,
            new Function<String[], Void>() {
              @Nullable
              @Override
              public Void apply(@Nullable String[] row) {
                assertEquals("Test file rawValue: " + row[RAW_VAL_IDX], ParseResult.STATUS.FAIL, PARSER.parse(row[RAW_VAL_IDX]).getStatus());
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
  public void testParseAsLocalDateTime(){
    ThreeTenNumericalDateParser parser = ThreeTenNumericalDateParser.getParser(Year.of(1900));

    //month first with 2 digits years >_<
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("122178").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12/21/78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12\\21\\78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12.21.78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12-21-78").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("12_21_78").getPayload()));

    //month/year alone
    assertEquals(YearMonth.of(1978,12), YearMonth.from(parser.parse("1978-12").getPayload()));

    //year alone
    assertEquals(Year.of(1978), Year.from(parser.parse("1978").getPayload()));
   // assertEquals(Year.of(1978), Year.from(parser.parse("78").getPayload()));
  }

  @Test
  public void testParseAsLocalDateByDateParts(){
    ThreeTenNumericalDateParser parser = ThreeTenNumericalDateParser.getParser();
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse("1978", "12", "21").getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 21), LocalDate.from(parser.parse(1978, 12, 21).getPayload()));
    assertEquals(LocalDate.of(1978, Month.DECEMBER, 1), LocalDate.from(parser.parse("1978", "12", "1").getPayload()));
    assertEquals(YearMonth.of(1978,12), YearMonth.from(parser.parse("1978", "12", null).getPayload()));
    assertEquals(Year.of(1978), Year.from(parser.parse("1978","",null).getPayload()));
  }

  @Test
  public void testBlankDates(){
    ThreeTenNumericalDateParser parser = ThreeTenNumericalDateParser.getParser();
    assertEquals(ParseResult.STATUS.FAIL, parser.parse(" ").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse("").getStatus());
    assertEquals(ParseResult.STATUS.FAIL, parser.parse(null).getStatus());
  }

}
