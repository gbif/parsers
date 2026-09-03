package org.gbif.common.parsers.date;

import org.gbif.api.util.IsoDateInterval;
import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.gbif.common.parsers.date.DateComponentOrdering.DMY;
import static org.gbif.common.parsers.date.DateComponentOrdering.DMYT;
import static org.gbif.common.parsers.date.DateComponentOrdering.DMY_FORMATS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ConstantConditions")
public class TemporalRangeParserTest {

  // See https://www.esrl.noaa.gov/gmd/grad/neubrew/Calendar.jsp for day of years.

  @Test
  public void singleDateRangeTest() {
    TemporalRangeParser trp =
        TemporalRangeParser.builder()
            .temporalParser(MultiinputTemporalParser.create(Collections.singletonList(DMY)))
            .create();

    OccurrenceParseResult<IsoDateInterval> result = trp.parse("1930/1929");
    assertEquals("1929", result.getPayload().getFrom().toString());
    assertEquals("1930", result.getPayload().getTo().toString());

    result = trp.parse("1930/1931");
    assertEquals("1930", result.getPayload().getFrom().toString());
    assertEquals("1931", result.getPayload().getTo().toString());

    result = trp.parse("1930/1930");
    assertEquals("1930", result.getPayload().getFrom().toString());
    assertEquals("1930", result.getPayload().getTo().toString());

    result = trp.parse("1930-01");
    assertEquals("1930-01", result.getPayload().getFrom().toString());
    assertEquals("1930-01", result.getPayload().getTo().toString());

    // Does not support
    result = trp.parse("01/1930");
    assertFalse(result.isSuccessful());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    result = trp.parse("1930-01-02/1930-02-01");
    assertEquals("1930-01-02", result.getPayload().getFrom().toString());
    assertEquals("1930-02-01", result.getPayload().getTo().toString());

    result = trp.parse("02/01/1930");
    assertEquals("1930-01-02", result.getPayload().getFrom().toString());
    assertEquals("1930-01-02", result.getPayload().getTo().toString());

    result = trp.parse("1930-01-02/02-01");
    assertEquals("1930-01-02", result.getPayload().getFrom().toString());
    assertEquals("1930-02-01", result.getPayload().getTo().toString());

    result = trp.parse("1930-01-02/15");
    assertEquals("1930-01-02", result.getPayload().getFrom().toString());
    assertEquals("1930-01-15", result.getPayload().getTo().toString());

    result = trp.parse("1930-01-02/1930-01-02");
    assertEquals("1930-01-02", result.getPayload().getFrom().toString());
    assertEquals("1930-01-02", result.getPayload().getTo().toString());
  }

  @Test
  public void ambiguousDateTest() {
    // Use a default parser, which cannot understand like: 01/02/2020
    TemporalRangeParser trp = TemporalRangeParser.builder().create();

    OccurrenceParseResult<IsoDateInterval> result = trp.parse("01/02/1999");
    assertFalse(result.getIssues().isEmpty());
    assertEquals(1, result.getIssues().size());
    assertTrue(result.getIssues().contains(OccurrenceIssue.RECORDED_DATE_INVALID));

    // Dots are reliably European order
    result = trp.parse("01.02.1999");
    assertEquals("1999-02-01", result.getPayload().getFrom().toString());
    assertEquals("1999-02-01", result.getPayload().getTo().toString());

    result = trp.parse("1999-01-02");
    assertTrue(result.getIssues().isEmpty());
    assertEquals("1999-01-02", result.getPayload().getFrom().toString());
    assertEquals("1999-01-02", result.getPayload().getTo().toString());

    // Use a DMY_FORMATS parser
    trp =
        TemporalRangeParser.builder()
            .temporalParser(MultiinputTemporalParser.create(Arrays.asList(DMY_FORMATS)))
            .create();

    result = trp.parse("01/02/1999");
    assertTrue(result.getIssues().isEmpty());
    assertEquals("1999-02-01", result.getPayload().getFrom().toString());
    assertEquals("1999-02-01", result.getPayload().getTo().toString());

    result = trp.parse("1999-01-02");
    assertTrue(result.getIssues().isEmpty());
    assertEquals("1999-01-02", result.getPayload().getFrom().toString());
    assertEquals("1999-01-02", result.getPayload().getTo().toString());
  }

  // https://github.com/gbif/parsers/issues/6
  @Test
  public void alternativePayloadTest() {
    TemporalRangeParser trp = TemporalRangeParser.builder().create();

    OccurrenceParseResult<IsoDateInterval> result = trp.parse("1999", "1", "2", "01/02/1999");
    assertEquals("1999-01-02", result.getPayload().getFrom().toString());
    assertEquals("1999-01-02", result.getPayload().getTo().toString());

    result = trp.parse("1999", "1", null, "01/02/1999");
    assertEquals("1999-01", result.getPayload().getFrom().toString());
    assertEquals("1999-01", result.getPayload().getTo().toString());
  }

  @Test
  public void testYMDT() {
    TemporalRangeParser trp =
      TemporalRangeParser.builder()
        .temporalParser(MultiinputTemporalParser.create(Collections.singletonList(DMY)))
        .create();
    // Should fail
    OccurrenceParseResult<IsoDateInterval> result = trp.parse("01/03/1930T12:01");
    assertFalse(result.isSuccessful());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    trp =
      TemporalRangeParser.builder()
        .temporalParser(MultiinputTemporalParser.create(Arrays.asList(DMY, DMYT)))
        .create();
    result = trp.parse("01/03/1930T12:01");

    assertEquals("1930-03-01T12:01", result.getPayload().getFrom().toString());
  }

  @Test
  public void testYearDayOfYear() {
    TemporalRangeParser trp = TemporalRangeParser.builder().create();

    OccurrenceParseResult<IsoDateInterval> result = trp.parse("2023", null, null, null, "12", "50");
    assertEquals("2023-01-12", result.getPayload().getFrom().toString());
    assertEquals("2023-02-19", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());

    result = trp.parse("2023", null, null, "2023-01-12/2023-02-19", "12", "50");
    assertEquals("2023-01-12", result.getPayload().getFrom().toString());
    assertEquals("2023-02-19", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());
  }

  @Test
  public void testYMDWithinRange() {
    TemporalRangeParser trp = TemporalRangeParser.builder().create();

    OccurrenceParseResult<IsoDateInterval> result = trp.parse("2000", "05", "06", "2000-04-01T01:02:03/2000-06-01T22:23:24", "92", "153");
    assertEquals("2000-04-01T01:02:03", result.getPayload().getFrom().toString());
    assertEquals("2000-06-01T22:23:24", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());

    result = trp.parse("2000", "05", "06", "2000-04-01T01:02:03/2000-06-01T22:23:24", null, null);
    assertEquals("2000-04-01T01:02:03", result.getPayload().getFrom().toString());
    assertEquals("2000-06-01T22:23:24", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());

    result = trp.parse("2000", "06", "06", "2000-04-01T01:02:03/2000-06-01T22:23:24", "92", "153");
    assertEquals("2000", result.getPayload().getFrom().toString());
    assertEquals("2000", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());
  }

  @Test
  public void mismatchingDatesTest() {
    TemporalRangeParser trp = TemporalRangeParser.builder().create();
    OccurrenceParseResult<IsoDateInterval> result;

    result = trp.parse("2000", "06", "06", "", "9", null);
    assertEquals("2000", result.getPayload().getFrom().toString());
    assertEquals("2000", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    result = trp.parse("2000", "06", "06", "", "-9", null);
    assertEquals("2000-06-06", result.getPayload().getFrom().toString());
    assertEquals("2000-06-06", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    result = trp.parse("2000", "06", "06", "", "15.23", null);
    assertEquals("2000-06-06", result.getPayload().getFrom().toString());
    assertEquals("2000-06-06", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    result = trp.parse("2000", null, null, "2000-04-01/2001-06-01", null, null);
    assertFalse(result.isSuccessful());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    result = trp.parse(null, null, null, "05/02/78", null, null);
    assertFalse(result.isSuccessful());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());
  }

  @Test
  public void alreadyPerfectTest() {
    TemporalRangeParser trp = TemporalRangeParser.builder().create();
    OccurrenceParseResult<IsoDateInterval> result;

    // The fields are already provided in perfect form
    result = trp.parse("2008", null, null, "2008-6-15/2008-07-20", null, null);
    assertEquals("2008-06-15", result.getPayload().getFrom().toString());
    assertEquals("2008-07-20", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());

    result = trp.parse("2008", "7", null, "2008-7-15/2008-07-20", null, null);
    assertEquals("2008-07-15", result.getPayload().getFrom().toString());
    assertEquals("2008-07-20", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());

    result = trp.parse("2008", "7", "15", "2008-7-15T12:34/2008-07-15T13:56", null, null);
    assertEquals("2008-07-15T12:34", result.getPayload().getFrom().toString());
    assertEquals("2008-07-15T13:56", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());

    result = trp.parse("2008", null, null, "2008-6-15/2008-07-20", "197", "202");
    assertEquals("2008-06-15", result.getPayload().getFrom().toString());
    assertEquals("2008-07-20", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());

    result = trp.parse("2008", null, null, "2008-6/2008-07", null, null);
    assertEquals("2008-06", result.getPayload().getFrom().toString());
    assertEquals("2008-07", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());

    result = trp.parse("02008", "6", null, "2008-6/2008-06", null, null);
    assertEquals("2008-06", result.getPayload().getFrom().toString());
    assertEquals("2008-06", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());
  }

  @Test
  public void testEqualResolution() {
    TemporalRangeParser trp = TemporalRangeParser.builder().create();

    // Beginning of range will be parsed as 2000, but end of range as 2000-06.  We must have 2000/2000, not 2000/2000-06.
    OccurrenceParseResult<IsoDateInterval> result = trp.parse("2000", "06", "06", "2000-04-01T01:02:03/2000-06-01T22:23:24", null, null);
    assertEquals("2000", result.getPayload().getFrom().toString());
    assertEquals("2000", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    // Event date has different resolutions on each side of the range
    result = trp.parse(null, null, null, "2009-09/2009-10-05", null, null);
    assertEquals("2009-09", result.getPayload().getFrom().toString());
    assertEquals("2009-10", result.getPayload().getTo().toString());
    // TODO: A small improvement would assign INVALID rather than MISMATCH for this.
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    // Event date has different resolutions on each side of the range
    result = trp.parse(null, null, null, "2009-09-18/2009-10-05T17:36+0200", null, null);
    assertEquals("2009-09-18", result.getPayload().getFrom().toString());
    assertEquals("2009-10-05", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    // Event date has different resolutions on each side of the range
    result = trp.parse(null, null, null, "2019-10-07T13:42:25Z/2019-10-07", null, null);
    assertEquals("2019-10-07", result.getPayload().getFrom().toString());
    assertEquals("2019-10-07", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    // Another example
    result = trp.parse(null, null, null, "1983-12-31 23:59:59/1983", null, null);
    assertEquals("1983", result.getPayload().getFrom().toString());
    assertEquals("1983", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    // One part of the range has a time zone
    result = trp.parse(null, null, null, "2006-03-08T2005/2006-03-08T2015+12", null, null);
    assertEquals("2006-03-08T20:05", result.getPayload().getFrom().toString());
    assertEquals("2006-03-08T20:15", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());

    // One part of the range has a UTC time zone
    result = trp.parse(null, null, null, "2022-03-01T06:26:14/2022-03-01T06:47:58Z", null, null);
    assertEquals("2022-03-01T06:26:14", result.getPayload().getFrom().toString());
    assertEquals("2022-03-01T06:47:58", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());
  }

  @Test
  public void testDatePartsOutsideRange() {
    TemporalRangeParser trp = TemporalRangeParser.builder().create();

    // Zero date parts
    OccurrenceParseResult<IsoDateInterval> result = trp.parse("1864", "0", "0", "1864", null, null);
    assertEquals("1864", result.getPayload().getFrom().toString());
    assertEquals("1864", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());

    result = trp.parse("0", "0", "0", "1864-10-11", null, null);
    assertEquals("1864-10-11", result.getPayload().getFrom().toString());
    assertEquals("1864-10-11", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    result = trp.parse("0", "10", "11", "2002-10-11", null, null);
    assertEquals("2002-10-11", result.getPayload().getFrom().toString());
    assertEquals("2002-10-11", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    // Not a number
    result = trp.parse("07.09.1919", null, null, "07.09.1919", null, null);
    assertEquals("1919-09-07", result.getPayload().getFrom().toString());
    assertEquals("1919-09-07", result.getPayload().getTo().toString());

    // Far outside reasonable range
    result = trp.parse("12345", null, null, "16.09.2013", null, null);
    assertEquals("2013-09-16", result.getPayload().getFrom().toString());
    assertEquals("2013-09-16", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    // Well outside range
    result = trp.parse("1071", "8", "10", "2000-01-01", null, null);
    assertFalse(result.isSuccessful());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_MISMATCH, result.getIssues().iterator().next());

    // Empty strings and spaces
    result = trp.parse("", " ", "", "1980-2-2", null, null);
    assertEquals("1980-02-02", result.getPayload().getFrom().toString());
    assertEquals("1980-02-02", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());

    // Empty strings and spaces
    result = trp.parse("", " ", "", "2/2/1980", null, null);
    assertEquals("1980-02-02", result.getPayload().getFrom().toString());
    assertEquals("1980-02-02", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());
  }

  @Test
  public void testInvertedRange() {
    TemporalRangeParser trp = TemporalRangeParser.builder().create();

    // Plain inverted date range
    OccurrenceParseResult<IsoDateInterval> result = trp.parse("2000", "04", null, "2000-04-05/2000-04-01", null, null);
    assertEquals("2000-04-01", result.getPayload().getFrom().toString());
    assertEquals("2000-04-05", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    // Inverted with a day
    result = trp.parse("1985", "05", "19", "1985-05-20/19", null, null);
    assertEquals("1985-05-19", result.getPayload().getFrom().toString());
    assertEquals("1985-05-20", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    // Inverted date range with time
    result = trp.parse("2022", "07", "04", "2022-07-04T15:44/2022-07-04T14:54", null, null);
    assertEquals("2022-07-04T14:54", result.getPayload().getFrom().toString());
    assertEquals("2022-07-04T15:44", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    // Inverted date range with zoned time
    result = trp.parse("2014", "07", "15", "2014-07-15T13:00:00+02:00/2014-07-15T09:06:00+02:00", null, null);
    assertEquals("2014-07-15T09:06+02:00", result.getPayload().getFrom().toString());
    assertEquals("2014-07-15T13:00+02:00", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());
  }

  @Test
  public void testRemeiningWeirdCases() {
    TemporalRangeParser trp = TemporalRangeParser.builder().create();

    // Cases found in production data that don't fit elsewhere.

    // Range in the day field, and inverted.
    OccurrenceParseResult<IsoDateInterval> result = trp.parse("2005", "04", "8/11", "2005-04-11/08", null, null);
    assertEquals("2005-04-08", result.getPayload().getFrom().toString());
    assertEquals("2005-04-11", result.getPayload().getTo().toString());
    assertEquals(1, result.getIssues().size());
    assertEquals(OccurrenceIssue.RECORDED_DATE_INVALID, result.getIssues().iterator().next());

    result = trp.parse(null, null, null, "2002-03-10 00:00:00.0", null, null);
    assertEquals("2002-03-10T00:00", result.getPayload().getFrom().toString());
    assertEquals("2002-03-10T00:00", result.getPayload().getTo().toString());
    assertEquals(0, result.getIssues().size());
  }
}
