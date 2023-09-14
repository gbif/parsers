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
  }
}
