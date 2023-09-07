package org.gbif.common.parsers.date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.gbif.common.parsers.core.ParseResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MultiinputTemporalParserTest {

  @Test
  public void testIsValidDate() {
    assertTrue(MultiinputTemporalParser.isValidDate(Year.of(2005)));
    assertTrue(MultiinputTemporalParser.isValidDate(YearMonth.of(2005, 1)));
    assertTrue(MultiinputTemporalParser.isValidDate(LocalDate.of(2005, 1, 1)));
    assertTrue(MultiinputTemporalParser.isValidDate(LocalDateTime.of(2005, 1, 1, 2, 3, 4)));
    assertTrue(MultiinputTemporalParser.isValidDate(LocalDate.now()));
    assertTrue(MultiinputTemporalParser.isValidDate(LocalDateTime.now().plus(23, ChronoUnit.HOURS)));

    // Dates out of bounds
    assertFalse(MultiinputTemporalParser.isValidDate(YearMonth.of(1499, 12)));

    // we tolerate an offset of 1 day
    assertFalse(MultiinputTemporalParser.isValidDate(LocalDate.now().plusDays(2)));
  }

  @Test
  public void testParseRecordedDate() {
    MultiinputTemporalParser temporalParser = MultiinputTemporalParser.create();
    OccurrenceParseResult<TemporalAccessor> result;

    result = temporalParser.parseRecordedDate("2005", "1", "1", "2005-01-01");
    assertResult(2005, 1, 1, result);
    assertEquals(0, result.getIssues().size());

    // ensure that eventDate with more precision will not record an issue and the one with most
    // precision will be returned
    result = temporalParser.parseRecordedDate("1996", "1", "26", "1996-01-26T01:00Z");
    assertEquals(
        ZonedDateTime.of(LocalDateTime.of(1996, 1, 26, 1, 0), ZoneId.of("Z")), result.getPayload());
    assertEquals(0, result.getIssues().size());

    // if dates contradict, return the parts that agree
    result = temporalParser.parseRecordedDate("2005", "1", "2", "2005-01-05");
    assertResult(2005, 1, result);
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());
  }

  @Test
  public void testGoodDate() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("1984", "3", "22", null);
    assertResult(1984, 3, 22, result);
    assertEquals(0, result.getIssues().size());
  }

  @Test
  public void testGoodOldDate() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("1757", "3", "22", null);
    assertResult(1757, 3, 22, result);
    assertEquals(0, result.getIssues().size());
  }

  /** 0 month now fails. */
  @Test
  public void testZeroMonth() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("1984", "0", "22", null);
    assertFalse(result.isSuccessful());
    assertNullPayload(result, OccurrenceIssue.RECORDED_DATE_INVALID);
  }

  @Test
  public void testOldYear() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("1499", "3", "22", null);
    assertNullPayload(result, OccurrenceIssue.RECORDED_DATE_UNLIKELY);
  }

  @Test
  public void testFutureYear() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("2100", "3", "22", null);
    assertNullPayload(result, OccurrenceIssue.RECORDED_DATE_UNLIKELY);
  }

  @Test
  public void testBadDay() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("1984", "3", "32", null);
    assertNullPayload(result, OccurrenceIssue.RECORDED_DATE_INVALID);
  }

  @Test
  public void testStringGood() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate(null, null, null, "1984-03-22");
    assertResult(1984, 3, 22, result);
    assertEquals(0, result.getIssues().size());
  }

  @Test
  public void testStringTimestamp() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate(null, null, null, "1984-03-22T00:00");
    assertResult(LocalDateTime.of(1984, 3, 22, 0, 0), result);
    assertEquals(0, result.getIssues().size());
  }

  @Test
  public void testStringBad() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate(null, null, null, "22-17-1984");
    assertNullPayload(result, OccurrenceIssue.RECORDED_DATE_INVALID);
  }

  @Test
  public void testStringMismatch() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("1984", "3", null, "1984-03-22");
    assertResult(1984, 3, result);
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());
  }

  @Test
  public void testStrange() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("16", "6", "1990", "16-6-1990");
    assertResult(1990, 6, 16, result);
    assertEquals(ParseResult.CONFIDENCE.PROBABLE, result.getConfidence());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());
  }

  @Test
  public void testStringRubbish() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("1984", "3", null, "22-17-1984");
    assertResult(1984, 3, result);
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());
  }

  // these two tests demonstrate the problem from POR-2120
  @Test
  public void testOnlyYear() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("1984", null, null, null);
    assertResult(1984, result);
    assertEquals(0, result.getIssues().size());

    result = MultiinputTemporalParser.create().parseRecordedDate(null, null, null, "1984");
    assertResult(1984, result);
    assertEquals(0, result.getIssues().size());

    result = MultiinputTemporalParser.create().parseRecordedDate("1984", null, null, "1984");
    assertResult(1984, result);
    assertEquals(0, result.getIssues().size());
  }

  @Test
  public void testYearWithZeros() {
    // providing 0 will cause a RECORDED_DATE_MISMATCH since 0 could be null but also January
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("1984", "0", "0", "1984");
    assertResult(1984, result);
    assertEquals(ParseResult.CONFIDENCE.PROBABLE, result.getConfidence());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    result = MultiinputTemporalParser.create().parseRecordedDate(null, null, null, "1984");
    assertEquals(ParseResult.CONFIDENCE.DEFINITE, result.getConfidence());
    assertTrue(result.getIssues().isEmpty());

    // This is not supported
    result = MultiinputTemporalParser.create().parseRecordedDate("1984", "0", "0", null);
    assertNullPayload(result, OccurrenceIssue.RECORDED_DATE_INVALID);

    result = MultiinputTemporalParser.create().parseRecordedDate(null, null, null, "0-0-1984");
    assertEquals(ParseResult.STATUS.FAIL, result.getStatus());
    assertNullPayload(result, OccurrenceIssue.RECORDED_DATE_INVALID);
  }

  @Test
  public void testYearMonthNoDay() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate("1984", "4", null, null);
    assertResult(1984, 4, result);
    assertTrue(result.getIssues().isEmpty());

    result = MultiinputTemporalParser.create().parseRecordedDate("1984", "3", null, "1984-03");
    assertResult(1984, 3, result);
    assertTrue(result.getIssues().isEmpty());

    result = MultiinputTemporalParser.create().parseRecordedDate(null, null, null, "1984-02");
    assertResult(1984, 2, result);
    assertTrue(result.getIssues().isEmpty());

    result = MultiinputTemporalParser.create().parseRecordedDate("2000", "3", null, "2000-03");
    assertResult(2000, 3, result);
    assertTrue(result.getIssues().isEmpty());
  }

  /** https://github.com/gbif/parsers/issues/8 */
  @Test
  public void testDifferentResolutions() {
    OccurrenceParseResult<TemporalAccessor> result;

    result = MultiinputTemporalParser.create().parseRecordedDate("1984", "3", "18", "1984-03");
    assertResult(1984, 3, result);
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    result = MultiinputTemporalParser.create().parseRecordedDate("1984", "3", null, "1984-03-18");
    assertResult(1984, 3, result);
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    result = MultiinputTemporalParser.create().parseRecordedDate("1984", null, null, "1984-03-18");
    assertResult(1984, result);
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    result = MultiinputTemporalParser.create().parseRecordedDate("1984", "3", null, "1984");
    assertResult(1984, result);
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    result = MultiinputTemporalParser.create().parseRecordedDate("1984", "05", "02", "1984-05-02T19:34");
    assertResult(LocalDateTime.of(1984, 5, 2, 19, 34, 0), result);
    assertEquals(0, result.getIssues().size());
  }

  /** https://github.com/gbif/parsers/issues/6 */
  @Test
  public void testLessConfidentMatch() {
    OccurrenceParseResult<TemporalAccessor> result;

    result = MultiinputTemporalParser.create().parseRecordedDate("2014", "2", "5", "5/2/2014");
    assertResult(2014, 2, 5, result);
    assertEquals(ParseResult.CONFIDENCE.POSSIBLE, result.getConfidence());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());
  }

  /** Only month now fails */
  @Test
  public void testOnlyMonth() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate(null, "3", null, null);
    assertNullPayload(result, OccurrenceIssue.RECORDED_DATE_INVALID);
  }

  /** Only day now fails */
  @Test
  public void testOnlyDay() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate(null, null, "23", null);
    assertNullPayload(result, OccurrenceIssue.RECORDED_DATE_INVALID);
  }

  @Test
  public void testAllNulls() {
    OccurrenceParseResult<TemporalAccessor> result =
        MultiinputTemporalParser.create().parseRecordedDate(null, null, null, null);
    // null and/or empty string will not return an error
    assertNullPayload(result, null);
  }

  @Test
  public void testDateStrings() {
    testEventDate(1999, 7, 19, "1999-07-19");
    testEventDate(1999, 7, 19, "19-07-1999");
    testEventDate(1999, 7, 19, "07-19-1999");
    testEventDate(1999, 7, 19, "19/7/1999");
    testEventDate(1999, 7, 19, "1999.7.19");
    testEventDate(1999, 7, 19, "19.7.1999");
    testEventDate(1999, 7, 19, "19990719");
    testEventDate(2012, 5, 6, "20120506");

    assertResult(
        LocalDateTime.of(1999, 7, 19, 0, 0),
        MultiinputTemporalParser.create().parseRecordedDate(null, null, null, "1999-07-19T00:00:00"));
  }

  private void testEventDate(int y, int m, int d, String input) {
    assertResult(y, m, d, MultiinputTemporalParser.create().parseRecordedDate(null, null, null, input));
  }

  /**
   * Tests that a date representing 'now' is interpreted with CONFIDENCE.DEFINITE even after
   * v1TemporalInterpreter was instantiated. See POR-2860.
   */
  @Test
  public void testNow() {

    MultiinputTemporalParser temporalParser = MultiinputTemporalParser.create();

    // Makes sure the static content is loaded
    OccurrenceParseResult<TemporalAccessor> result =
        temporalParser.parseRecordedDate(
            null, null, null, DateFormatUtils.ISO_DATETIME_FORMAT.format(Calendar.getInstance()));
    assertEquals(ParseResult.CONFIDENCE.DEFINITE, result.getConfidence());

    // Sorry for this Thread.sleep, we need to run the v1TemporalInterpreter at least 1 second later
    // until
    // we refactor to inject a Calendar or we move to new Java 8 Date/Time API
    try {
      TimeUnit.MILLISECONDS.sleep(1_000);
    } catch (InterruptedException e) {
      fail(e.getMessage());
    }

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    result =
        temporalParser.parseRecordedDate(
            null, null, null, DateFormatUtils.ISO_DATETIME_FORMAT.format(cal.getTime()));
    assertEquals(ParseResult.CONFIDENCE.DEFINITE, result.getConfidence());
  }

  @Test
  public void testDateRanges() {
    MultiinputTemporalParser temporalParser = MultiinputTemporalParser.create();
    // Some of the many ways of providing a range.
    assertResult(Year.of(1999), temporalParser.parseRecordedDate(null, null, null, "1999"));
    assertResult(Year.of(1999), temporalParser.parseRecordedDate(null, null, null, "1999/2000"));
    assertResult(
        YearMonth.of(1999, 1),
        temporalParser.parseRecordedDate(null, null, null, "1999-01/1999-12"));
    assertResult(
        ZonedDateTime.of(LocalDateTime.of(2004, 12, 30, 0, 0, 0, 0), ZoneOffset.UTC),
        temporalParser.parseRecordedDate(
            null, null, null, "2004-12-30T00:00:00+0000/2005-03-13T24:00:00+0000"));
  }

  private void assertInts(Integer expected, Integer x) {
    if (expected == null) {
      assertNull(x);
    } else {
      assertEquals(expected, x);
    }
  }

  /**
   * Utility method to assert a ParseResult when a LocalDate is expected. This method should not be
   * used to test expected null results.
   */
  private void assertResult(Integer y, Integer m, Integer d, ParseResult<TemporalAccessor> result) {
    // sanity checks
    assertNotNull(result);

    LocalDate localDate = result.getPayload().query(LocalDate::from);
    assertInts(y, localDate.getYear());
    assertInts(m, localDate.getMonthValue());
    assertInts(d, localDate.getDayOfMonth());

    assertEquals(LocalDate.of(y, m, d), result.getPayload());
  }

  private void assertResult(TemporalAccessor expectedTA, ParseResult<TemporalAccessor> result) {
    assertEquals(expectedTA, result.getPayload());
  }

  /**
   * Utility method to assert a ParseResult when a YearMonth is expected. This method should not be
   * used to test expected null results.
   */
  private void assertResult(Integer y, Integer m, ParseResult<TemporalAccessor> result) {
    // sanity checks
    assertNotNull(result);

    YearMonth yearMonthDate = result.getPayload().query(YearMonth::from);
    assertInts(y, yearMonthDate.getYear());
    assertInts(m, yearMonthDate.getMonthValue());

    assertEquals(YearMonth.of(y, m), result.getPayload());
  }

  private void assertResult(Integer y, ParseResult<TemporalAccessor> result) {
    // sanity checks
    assertNotNull(result);

    Year yearDate = result.getPayload().query(Year::from);
    assertInts(y, yearDate.getValue());

    assertEquals(Year.of(y), result.getPayload());
  }

  private void assertNullPayload(
      OccurrenceParseResult<TemporalAccessor> result, OccurrenceIssue expectedIssue) {
    assertNotNull(result);
    assertFalse(result.isSuccessful());
    assertNull(result.getPayload());

    if (expectedIssue != null) {
      assertTrue(result.getIssues().contains(expectedIssue));
    }
  }
}
