package org.gbif.common.parsers.date;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gbif.api.util.IsoDateInterval;
import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.common.parsers.utils.DelimiterUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class TemporalRangeParser implements Serializable {

  private final MultiinputTemporalParser temporalParser;

  @Builder(buildMethodName = "create")
  private TemporalRangeParser(MultiinputTemporalParser temporalParser) {
    if (temporalParser != null) {
      this.temporalParser = temporalParser;
    } else {
      this.temporalParser = MultiinputTemporalParser.create();
    }
  }

  public OccurrenceParseResult<IsoDateInterval> parse(String dateRange) {
    return parse(null, null, null, dateRange, null, null);
  }

  public OccurrenceParseResult<IsoDateInterval> parse(String year, String month, String day, String dateRange) {
    return parse(year, month, day, dateRange, null, null);
  }

  public OccurrenceParseResult<IsoDateInterval> parse(
      String year,
      String month,
      String day,
      String dateRange,
      String startDayOfYear,
      String endDayOfYear) {

    Set<OccurrenceIssue> issues = new HashSet<>();

    try {
      // Even a single date will be split to two
      final String[] rawPeriod = DelimiterUtils.splitPeriod(dateRange);

      Temporal from;
      Temporal to;

      // If eventDate and year are present
      if (StringUtils.isNotBlank(dateRange) && StringUtils.isNotBlank(year)) {
        OccurrenceParseResult<TemporalAccessor> dateRangeOnlyStart = temporalParser.parseRecordedDate(null, null, null, rawPeriod[0], null);
        OccurrenceParseResult<TemporalAccessor> dateRangeOnlyEnd = temporalParser.parseRecordedDate(null, null, null, rawPeriod[1], null);
        OccurrenceParseResult<TemporalAccessor> ymdOnly = temporalParser.parseRecordedDate(year, month, day, null, null);

        if (ymdOnly.isSuccessful()) {
          if (dateRangeOnlyStart.isSuccessful() && dateRangeOnlyEnd.isSuccessful()) {
            // If eventDate is a multi-day range, with at least day precision, and year+month+day are set, we must test
            // whether year+month+day falls within this range.
            if (StringUtils.isNotBlank(month) && StringUtils.isNotBlank(day)) {
              if (dateRangeOnlyStart.getPayload().isSupported(ChronoField.DAY_OF_YEAR)
                && dateRangeOnlyEnd.getPayload().isSupported(ChronoField.DAY_OF_YEAR)
                && ymdOnly.getPayload().isSupported(ChronoField.DAY_OF_YEAR)) {
                if (TemporalAccessorUtils.withinRange(dateRangeOnlyStart.getPayload(), dateRangeOnlyEnd.getPayload(), ymdOnly.getPayload()) ||
                  // Also if the range is backwards
                  TemporalAccessorUtils.withinRange(dateRangeOnlyEnd.getPayload(), dateRangeOnlyStart.getPayload(), ymdOnly.getPayload())) {
                  // Then we can just check the startDayOfYear and endDayOfYear fields match.
                  from = parseAndSet(null, null, null, rawPeriod[0], startDayOfYear, issues);
                  to = parseAndSet(null, null, null, rawPeriod[1], endDayOfYear, issues);
                  log.trace("Range {}|{}|{}|{}|{}|{} succeeds with ymd within range {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);
                  return OccurrenceParseResult.success(ParseResult.CONFIDENCE.DEFINITE, finalChecks(from, to, issues), issues);
                }
              }
            }
          }

          // If that didn't work, test whether year+month+day are set according to the constant parts of eventDate.
          Optional<TemporalAccessor> dateRangeConstant = TemporalAccessorUtils.nonConflictingDateParts(dateRangeOnlyStart.getPayload(), dateRangeOnlyEnd.getPayload(), null);
          if (dateRangeConstant.isPresent() && ymdOnly.getPayload().equals(dateRangeConstant.get())) {
            // Then we can just check the startDayOfYear and endDayOfYear fields match.
            from = parseAndSet(null, null, null, rawPeriod[0], startDayOfYear, issues);
            to = parseAndSet(null, null, null, rawPeriod[1], endDayOfYear, issues);
            log.trace("Range {}|{}|{}|{}|{}|{} succeeds with correct ymd parts {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);
            return OccurrenceParseResult.success(ParseResult.CONFIDENCE.DEFINITE, finalChecks(from, to, issues), issues);
          }
        }
      }

      // Otherwise, we will reduce the precision of the given dates until they all agree.

      // Year+month+day, first part of eventDate, and startDayOfYear to the best we can get.
      from = parseAndSet(year, month, day, rawPeriod[0], startDayOfYear, issues);
      // Year+month+day, second part of eventDate, and endDayOfYear to the best we can get.
      to = parseAndSet(year, month, day, rawPeriod[1], endDayOfYear, issues);
      log.trace("Range {}|{}|{}|{}|{}|{} parsed to {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);

      // Neither could be parsed, or else the year is outside the range.
      if (from == null && to == null) {
        log.debug("Range {}|{}|{}|{}|{}|{} could not be parsed", year, month, day, dateRange, startDayOfYear, endDayOfYear);
        return OccurrenceParseResult.fail(issues);
      }

      // The year is outside one part of the date range.
      if (from != null ^ to != null) {
        log.debug("Range {}|{}|{}|{}|{}|{} fails due to missing start xor end {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);
        issues.add(OccurrenceIssue.RECORDED_DATE_MISMATCH);
        return OccurrenceParseResult.fail(issues);
      }

      // Apply final checks
      log.trace("Range {}|{}|{}|{}|{}|{} succeeds {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);
      return OccurrenceParseResult.success(ParseResult.CONFIDENCE.DEFINITE, finalChecks(from, to, issues), issues);
    } catch (Exception e) {
      log.error("Exception when parsing dates: {}, {}, {}, {}, {}, {}, {}", year, month, day, dateRange, startDayOfYear, endDayOfYear);
      log.error("Exception is "+e.getMessage(), e);
      issues.add(OccurrenceIssue.RECORDED_DATE_INVALID);
      issues.add(OccurrenceIssue.INTERPRETATION_ERROR);
      return OccurrenceParseResult.fail(issues);
    }
  }

  private Temporal parseAndSet(
      String year,
      String month,
      String day,
      String rawDate,
      String dayOfYear,
      Set<OccurrenceIssue> issues) {
    OccurrenceParseResult<TemporalAccessor> result =
        temporalParser.parseRecordedDate(year, month, day, rawDate, dayOfYear);
    issues.addAll(result.getIssues());
    if (result.isSuccessful()) {
      return (Temporal) result.getPayload();
    } else {
      return null;
    }
  }

  /** Compare dates and returns difference between FROM and TO dates in milliseconds */
  private static long getRangeDiff(Temporal from, Temporal to) {
    if (from == null || to == null) {
      return 1L;
    }
    TemporalUnit unit = null;
    if (from instanceof Year) {
      unit = ChronoUnit.YEARS;
    } else if (from instanceof YearMonth) {
      unit = ChronoUnit.MONTHS;
    } else if (from instanceof LocalDate) {
      unit = ChronoUnit.DAYS;
    } else if (from instanceof LocalDateTime
        || from instanceof OffsetDateTime
        || from instanceof ZonedDateTime) {
      unit = ChronoUnit.SECONDS;
    }
    return from.until(to, unit);
  }

  /**
   * Make sure the range is the correct way around, has equal resolutions, and matching time zones.
   */
  private IsoDateInterval finalChecks(Temporal from, Temporal to, Set<OccurrenceIssue> issues) {
    if (from != null && to != null) {
      // If the dateRange has different resolutions on either side, truncate it.
      if (TemporalAccessorUtils.resolutionToSeconds(from) != TemporalAccessorUtils.resolutionToSeconds(to)) {
        log.trace("Resolutions don't match, truncate.");
        int requiredResolution = Math.min(TemporalAccessorUtils.resolutionToSeconds(from), TemporalAccessorUtils.resolutionToSeconds(to));
        from = TemporalAccessorUtils.limitToResolution(from, requiredResolution);
        to = TemporalAccessorUtils.limitToResolution(to, requiredResolution);
        issues.add(OccurrenceIssue.RECORDED_DATE_MISMATCH);
      }

      // If one side has a time zone and the other doesn't, remove it.
      if (from.isSupported(ChronoField.OFFSET_SECONDS) ^ to.isSupported(ChronoField.OFFSET_SECONDS)) {
        from = from.query(LocalDateTime::from);
        to = to.query(LocalDateTime::from);
      }

      // Reverse order if needed
      if (from.getClass() == to.getClass()) {
        long rangeDiff = getRangeDiff(from, to);
        if (rangeDiff < 0) {
          log.trace("Range was inverted.");
          issues.add(OccurrenceIssue.RECORDED_DATE_INVALID);
          return new IsoDateInterval(to, from);
        }
      } else {
        issues.add(OccurrenceIssue.RECORDED_DATE_UNLIKELY);
      }
    }
    return new IsoDateInterval(from, to);
  }
}
