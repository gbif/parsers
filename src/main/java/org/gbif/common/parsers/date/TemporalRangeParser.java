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
    // Even a single date will be split to two
    String[] rawPeriod = DelimiterUtils.splitPeriod(dateRange);

    Temporal from;
    Temporal to;
    Set<OccurrenceIssue> issues = new HashSet<>();

    // If eventDate is a multi-day range, with at least day precision, and year+month+day are set, we must test
    // whether year+month+day falls within this range.
    if (StringUtils.isNotBlank(year) && StringUtils.isNotBlank(month) && StringUtils.isNotBlank(day) && StringUtils.isNotBlank(dateRange)) {
      OccurrenceParseResult<TemporalAccessor> dateRangeOnlyStart = temporalParser.parseRecordedDate(null, null, null, rawPeriod[0], null);
      OccurrenceParseResult<TemporalAccessor> dateRangeOnlyEnd = temporalParser.parseRecordedDate(null, null, null, rawPeriod[1], null);
      OccurrenceParseResult<TemporalAccessor> ymdOnly = temporalParser.parseRecordedDate(year, month, day, null, null);

      if (dateRangeOnlyStart.isSuccessful() && dateRangeOnlyEnd.isSuccessful() && ymdOnly.isSuccessful()) {
        if (dateRangeOnlyStart.getPayload().isSupported(ChronoField.DAY_OF_YEAR)
          && dateRangeOnlyEnd.getPayload().isSupported(ChronoField.DAY_OF_YEAR)
          && ymdOnly.getPayload().isSupported(ChronoField.DAY_OF_YEAR)) {
            if (TemporalAccessorUtils.withinRange(dateRangeOnlyStart.getPayload(), dateRangeOnlyEnd.getPayload(), ymdOnly.getPayload())) {
            // Then we can just check the startDayOfYear and endDayOfYear fields match.
            from = parseAndSet(null, null, null, rawPeriod[0], startDayOfYear, issues);
            to = parseAndSet(null, null, null, rawPeriod[1], endDayOfYear, issues);
            // If the dateRange has different resolutions on either side, truncate it.
            if (TemporalAccessorUtils.resolutionToSeconds(from) != TemporalAccessorUtils.resolutionToSeconds(to)) {
              int requiredResolution = Math.min(TemporalAccessorUtils.resolutionToSeconds(from), TemporalAccessorUtils.resolutionToSeconds(to));
              from = TemporalAccessorUtils.limitToResolution(from, requiredResolution);
              to = TemporalAccessorUtils.limitToResolution(to, requiredResolution);
              issues.add(OccurrenceIssue.RECORDED_DATE_INVALID);
            }
            log.trace("Range {}|{}|{}|{}|{}|{} succeeds with ymd within range {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);
            return OccurrenceParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new IsoDateInterval(from, to), issues);
          }
        }
      }
    }

    // If eventDate is a range, and at least year is set, we must test whether year+month+day are set according to the
    // constant parts of eventDate.
    if (StringUtils.isNotBlank(year) && StringUtils.isNotBlank(dateRange)) {
      OccurrenceParseResult<TemporalAccessor> dateRangeOnlyStart = temporalParser.parseRecordedDate(null, null, null, rawPeriod[0], null);
      OccurrenceParseResult<TemporalAccessor> dateRangeOnlyEnd = temporalParser.parseRecordedDate(null, null, null, rawPeriod[1], null);
      OccurrenceParseResult<TemporalAccessor> ymdOnly = temporalParser.parseRecordedDate(year, month, day, null, null);

      if (dateRangeOnlyStart.isSuccessful() && dateRangeOnlyEnd.isSuccessful() && ymdOnly.isSuccessful()) {
        Optional<TemporalAccessor> dateRangeConstant = TemporalAccessorUtils.nonConflictingDateParts(dateRangeOnlyStart.getPayload(), dateRangeOnlyEnd.getPayload(), null);

        if (dateRangeConstant.isPresent() && ymdOnly.getPayload().equals(dateRangeConstant.get())) {
          // Then we can just check the startDayOfYear and endDayOfYear fields match.
          from = parseAndSet(null, null, null, rawPeriod[0], startDayOfYear, issues);
          to = parseAndSet(null, null, null, rawPeriod[1], endDayOfYear, issues);
          if (TemporalAccessorUtils.resolutionToSeconds(from) != TemporalAccessorUtils.resolutionToSeconds(to)) {
            int requiredResolution = Math.min(TemporalAccessorUtils.resolutionToSeconds(from), TemporalAccessorUtils.resolutionToSeconds(to));
            from = TemporalAccessorUtils.limitToResolution(from, requiredResolution);
            to = TemporalAccessorUtils.limitToResolution(to, requiredResolution);
            issues.add(OccurrenceIssue.RECORDED_DATE_INVALID);
          }
          log.trace("Range {}|{}|{}|{}|{}|{} succeeds with correct ymd parts {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);
          return OccurrenceParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new IsoDateInterval(from, to), issues);
        }
      }
    }

    // Otherwise, we will reduce the precision of the given dates until they all agree.

    // Year+month+day, first part of eventDate, and startDay of year to the best we can get.
    from = parseAndSet(year, month, day, rawPeriod[0], startDayOfYear, issues);
    // Year+month+day, second part of eventDate, and endDayOfYear of year to the best we can get.
    to = parseAndSet(year, month, day, rawPeriod[1], endDayOfYear, issues);
    log.trace("Range {}|{}|{}|{}|{}|{} parsed to {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);

    // Return a failure, rather than a range with a missing start or end
    if (from != null ^ to != null) {
      log.debug("Range {}|{}|{}|{}|{}|{} fails due to missing start xor end {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);
      issues.add(OccurrenceIssue.RECORDED_DATE_MISMATCH);
      return OccurrenceParseResult.fail(issues);
    }

    // If the resolutions are not equal, truncate such that they are.
    if (from != null && to != null) {
      if (TemporalAccessorUtils.resolutionToSeconds(from) != TemporalAccessorUtils.resolutionToSeconds(to)) {
        log.trace("Range {}|{}|{}|{}|{}|{} has different resolutions, will be truncated {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);
        int requiredResolution = Math.min(TemporalAccessorUtils.resolutionToSeconds(from), TemporalAccessorUtils.resolutionToSeconds(to));
        from = TemporalAccessorUtils.limitToResolution(from, requiredResolution);
        to = TemporalAccessorUtils.limitToResolution(to, requiredResolution);
      }
    }

    // Reverse order if needed
    if (from != null && to != null) {
      if (from.getClass() == to.getClass()) {
        long rangeDiff = getRangeDiff(from, to);
        if (rangeDiff < 0) {
          log.trace("Range {}|{}|{}|{}|{}|{} will be reversed {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);
          issues.add(OccurrenceIssue.RECORDED_DATE_INVALID);
          return OccurrenceParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new IsoDateInterval(to, from), issues);
        }
      } else {
        issues.add(OccurrenceIssue.RECORDED_DATE_UNLIKELY);
      }
    }

    if (from == null && to == null) {
      log.debug("Range {}|{}|{}|{}|{}|{} could not be parsed", year, month, day, dateRange, startDayOfYear, endDayOfYear);
      return OccurrenceParseResult.fail(issues);
    } else if (from != null && to != null) {
      log.trace("Range {}|{}|{}|{}|{}|{} succeeds {}→{}", year, month, day, dateRange, startDayOfYear, endDayOfYear, from, to);
      return OccurrenceParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new IsoDateInterval(from, to), issues);
    } else {
      log.error("From {} and to {} dates of range are unexpectedly not-null and null when parsing {}, {}, {}, {}, {}, {}",
        from, to, year, month, day, dateRange, startDayOfYear, endDayOfYear);
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
}
