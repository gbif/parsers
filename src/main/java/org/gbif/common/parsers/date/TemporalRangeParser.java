package org.gbif.common.parsers.date;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.OccurrenceParseResult;
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
import java.util.Optional;
import java.util.function.Consumer;

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

  public EventRange parse(String dateRange) {
    return parse(null, null, null, dateRange, null, null);
  }

  public EventRange parse(String year, String month, String day, String dateRange) {
    return parse(year, month, day, dateRange, null, null);
  }

  public EventRange parse(
      String year,
      String month,
      String day,
      String dateRange,
      String startDayOfYear,
      String endDayOfYear) {
    // Even a single date will be split to two
    String[] rawPeriod = DelimiterUtils.splitPeriod(dateRange);

    System.out.println(
        "Range parsing Y"
            + year
            + " M"
            + month
            + " D"
            + day
            + " ["
            + dateRange
            + "] s"
            + startDayOfYear
            + " e"
            + endDayOfYear);

    EventRange eventRange = new EventRange();

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
            parseAndSet(eventRange, null, null, null, rawPeriod[0], startDayOfYear, eventRange::setFrom);
            parseAndSet(eventRange, null, null, null, rawPeriod[1], endDayOfYear, eventRange::setTo);
            return eventRange;
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

        if (ymdOnly.getPayload().equals(dateRangeConstant.get())) {
          // Then we can just check the startDayOfYear and endDayOfYear fields match.
          parseAndSet(eventRange, null, null, null, rawPeriod[0], startDayOfYear, eventRange::setFrom);
          parseAndSet(eventRange, null, null, null, rawPeriod[1], endDayOfYear, eventRange::setTo);
          return eventRange;
        }
      }
    }

    // Otherwise, we will reduce the precision of the given dates until they all agree.

    // Year+month+day, first part of eventDate, and startDay of year to the best we can get.
    parseAndSet(eventRange, year, month, day, rawPeriod[0], startDayOfYear, eventRange::setFrom);
    // Year+month+day, second part of eventDate, and endDayOfYear of year to the best we can get.
    parseAndSet(eventRange, year, month, day, rawPeriod[1], endDayOfYear, eventRange::setTo);

    // If the resolutions are not equal, truncate such that they are.
    if (eventRange.getFrom().isPresent() && eventRange.getTo().isPresent()) {
      if (TemporalAccessorUtils.resolution(eventRange.getFrom().get()) != TemporalAccessorUtils.resolution(eventRange.getTo().get())) {
        int requiredResolution = Math.min(TemporalAccessorUtils.resolution(eventRange.getFrom().get()), TemporalAccessorUtils.resolution(eventRange.getTo().get()));
        if (requiredResolution == 3) {
          eventRange.setFrom(eventRange.getFrom().get().query(LocalDate::from));
          eventRange.setTo(eventRange.getTo().get().query(LocalDate::from));
        } else if (requiredResolution == 2) {
          eventRange.setFrom(eventRange.getFrom().get().query(YearMonth::from));
          eventRange.setTo(eventRange.getTo().get().query(YearMonth::from));
        } else {
          eventRange.setFrom(eventRange.getFrom().get().query(Year::from));
          eventRange.setTo(eventRange.getTo().get().query(Year::from));
        }
      }
    }

    // Reverse order if needed
    if (eventRange.getFrom().isPresent() && eventRange.getTo().isPresent()) {
      TemporalAccessor from = eventRange.getFrom().get();
      TemporalAccessor to = eventRange.getTo().get();
      if (from.getClass() == to.getClass()) {
        long rangeDiff = getRangeDiff((Temporal) from, (Temporal) to);
        if (rangeDiff < 0) {
          eventRange.addIssue(OccurrenceIssue.RECORDED_DATE_INVALID);
          EventRange reversed = new EventRange();
          reversed.setFrom(eventRange.getTo().get());
          reversed.setTo(eventRange.getFrom().get());
          reversed.setIssues(eventRange.getIssues());
          return reversed;
        }
      } else {
        eventRange.addIssue(OccurrenceIssue.RECORDED_DATE_UNLIKELY);
      }
    }

    return eventRange;
  }

  private void parseAndSet(
      EventRange range,
      String year,
      String month,
      String day,
      String rawDate,
      String dayOfYear,
      Consumer<TemporalAccessor> setFn) {
    OccurrenceParseResult<TemporalAccessor> result =
        temporalParser.parseRecordedDate(year, month, day, rawDate, dayOfYear);
    if (result.isSuccessful()) {
      Optional.ofNullable(result.getPayload()).ifPresent(setFn);
    }
    range.addIssues(result.getIssues());
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
