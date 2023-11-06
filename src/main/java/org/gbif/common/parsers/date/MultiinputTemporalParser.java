package org.gbif.common.parsers.date;

import com.google.common.base.Strings;
import com.google.common.collect.Range;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.gbif.common.parsers.core.ParseResult;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.gbif.common.parsers.core.ParseResult.CONFIDENCE.PROBABLE;

/**
 * A date parser accepting multiple dates, and returning a common interpretation of them.
 */
@Slf4j
public class MultiinputTemporalParser implements Serializable {

  private static final long serialVersionUID = -8845127337324812802L;

  private static final LocalDate MIN_LOCAL_DATE = LocalDate.of(1500, 1, 1);

  private final TemporalParser temporalParser;

  private MultiinputTemporalParser(List<DateComponentOrdering> orderings) {
    if (orderings != null && !orderings.isEmpty()) {
      DateComponentOrdering[] array = orderings.toArray(new DateComponentOrdering[0]);
      temporalParser = CustomizedTextDateParser.getInstance(array);
    } else {
      temporalParser = DateParsers.defaultTemporalParser();
    }
  }

  public static MultiinputTemporalParser create(List<DateComponentOrdering> orderings) {
    return new MultiinputTemporalParser(orderings);
  }

  public static MultiinputTemporalParser create() {
    return create(Collections.emptyList());
  }

  public OccurrenceParseResult<TemporalAccessor> parseRecordedDate(
      String year, String month, String day, String dateString) {
    return parseRecordedDate(year, month, day, dateString, null);
  }

  /**
   * Three dates are provided:
   *
   * <ul>
   *   <li>year, month and day
   *   <li>dateString
   *   <li>year and dayOfYear
   * </ul>
   *
   * <p>Produces a single date at the best resolution possible, ignoring missing values.
   *
   * <p>Years are verified to be before this year and after 1500.
   *
   * @return interpretation result, never null
   */
  public OccurrenceParseResult<TemporalAccessor> parseRecordedDate(
      String year, String month, String day, String dateString, String dayOfYear) {

    boolean ymdProvided =
        StringUtils.isNotBlank(year)
            || StringUtils.isNotBlank(month)
            || StringUtils.isNotBlank(day);
    boolean dateStringProvided = StringUtils.isNotBlank(dateString);
    boolean yDoyProvided = StringUtils.isNotBlank(year) && StringUtils.isNotBlank(dayOfYear);

    // If we have only a year and dayOfYear, don't parse the year alone (without the month and day)
    if (yDoyProvided && (StringUtils.isBlank(month) && StringUtils.isBlank(day))) {
      ymdProvided = false;
    }

    boolean twoOrMoreProvided = (ymdProvided ? 1 : 0) + (dateStringProvided ? 1 : 0) + (yDoyProvided ? 1 : 0) >= 2;

    if (!ymdProvided && !dateStringProvided && !yDoyProvided) {
      log.trace("Date {}|{}|{}|{}|{} is all null", year, month, day, dateString, dayOfYear);
      return OccurrenceParseResult.fail();
    }

    Set<OccurrenceIssue> issues = EnumSet.noneOf(OccurrenceIssue.class);

    TemporalAccessor parsedTemporalAccessor;
    ParseResult.CONFIDENCE confidence;

    // Parse all three possible dates
    ParseResult<TemporalAccessor> parsedYMDResult =
        ymdProvided ? temporalParser.parse(year, month, day) : ParseResult.fail();
    ParseResult<TemporalAccessor> parsedDateResult =
        dateStringProvided ? temporalParser.parse(dateString) : ParseResult.fail();
    ParseResult<TemporalAccessor> parsedYearDoyResult =
        yDoyProvided ? temporalParser.parse(year, dayOfYear) : ParseResult.fail();
    TemporalAccessor parsedYmdTa = parsedYMDResult.getPayload();
    TemporalAccessor parsedDateTa = parsedDateResult.getPayload();
    TemporalAccessor parsedYearDoyTa = parsedYearDoyResult.getPayload();

    int ymdResolution = -1, dateStringResolution = -1;
    if (ymdProvided && parsedYMDResult.isSuccessful()) {
      ymdResolution = TemporalAccessorUtils.resolution(parsedYmdTa);
    }
    if (dateStringProvided && parsedDateResult.isSuccessful()) {
      dateStringResolution = TemporalAccessorUtils.resolution(parsedDateTa);
    }

    // Add issues if we failed to parse any dates that were present
    if (ymdProvided && !parsedYMDResult.isSuccessful()) {
      issues.add(OccurrenceIssue.RECORDED_DATE_INVALID);
    }
    if (dateStringProvided && !parsedDateResult.isSuccessful()) {
      issues.add(OccurrenceIssue.RECORDED_DATE_INVALID);
    }
    if (yDoyProvided && !parsedYearDoyResult.isSuccessful()) {
      issues.add(OccurrenceIssue.RECORDED_DATE_INVALID);
    }

    log.trace("Date {}|{}|{}|{}|{} parsed to {}—{}—{}", year, month, day, dateString, dayOfYear, parsedYMDResult, parsedDateResult, parsedYearDoyResult);

    // If a dateString is provided with something else, handle the case where it doesn't match.
    boolean ambiguityResolved = false;
    if (ymdProvided
        && dateStringProvided
        && !TemporalAccessorUtils.sameOrContained(parsedYmdTa, parsedDateTa)
        && parsedDateResult.getAlternativePayloads() != null) {

      // eventDate could be ambiguous (5/4/2014), but disambiguated by year-month-day.
      Optional<TemporalAccessor> resolved =
          TemporalAccessorUtils.resolveAmbiguousDates(
              parsedYmdTa, parsedDateResult.getAlternativePayloads());
      if (resolved.isPresent()) {
        parsedDateTa = resolved.get();
        ambiguityResolved = true;
        log.trace("Date {}|{}|{}|{}|{} ambiguous₁ y-m-d resolved {}", year, month, day, dateString, dayOfYear, parsedDateTa);
      }
      // still a conflict
      if (!ambiguityResolved) {
        if (parsedYmdTa == null || parsedDateTa == null) {
          log.debug("Date {}|{}|{}|{}|{} ambiguous₁ invalid", year, month, day, dateString, dayOfYear);
          issues.add(OccurrenceIssue.RECORDED_DATE_INVALID);
        } else {
          log.debug("Date {}|{}|{}|{}|{} ambiguous₁ mismatch", year, month, day, dateString, dayOfYear);
          issues.add(OccurrenceIssue.RECORDED_DATE_MISMATCH);
        }
      }
    } else if (ymdProvided
        && yDoyProvided
        && !TemporalAccessorUtils.sameOrContained(parsedYearDoyTa, parsedDateTa)
        && parsedDateResult.getAlternativePayloads() != null) {

      // eventDate could be ambiguous (5/4/2014), but disambiguated by year-month-day.
      Optional<TemporalAccessor> resolved =
          TemporalAccessorUtils.resolveAmbiguousDates(
              parsedYearDoyTa, parsedDateResult.getAlternativePayloads());
      if (resolved.isPresent()) {
        parsedDateTa = resolved.get();
        ambiguityResolved = true;
        log.trace("Date {}|{}|{}|{}|{} ambiguous₂ y-doy resolved {}", year, month, day, dateString, dayOfYear, parsedDateTa);
      }
      // still a conflict
      if (!ambiguityResolved) {
        if (parsedYmdTa == null || parsedYearDoyTa == null) {
          log.debug("Date {}|{}|{}|{}|{} ambiguous₂ invalid", year, month, day, dateString, dayOfYear);
          issues.add(OccurrenceIssue.RECORDED_DATE_INVALID);
        } else {
          log.debug("Date {}|{}|{}|{}|{} ambiguous₂ mismatch", year, month, day, dateString, dayOfYear);
          issues.add(OccurrenceIssue.RECORDED_DATE_MISMATCH);
        }
      }
    }

    // Add an issue if there is any conflict between the dates
    if (TemporalAccessorUtils.sameOrContainedOrNull(parsedYmdTa, parsedDateTa)
        && TemporalAccessorUtils.sameOrContainedOrNull(parsedYmdTa, parsedYearDoyTa)
        && TemporalAccessorUtils.sameOrContainedOrNull(parsedDateTa, parsedYearDoyTa)) {
      confidence =
          parsedDateTa != null
              ? parsedDateResult.getConfidence()
              : (parsedYmdTa != null
                  ? parsedYMDResult.getConfidence()
                  : parsedYearDoyResult.getConfidence());
    } else {
      log.debug("Date {}|{}|{}|{}|{} mismatch (conflict)", year, month, day, dateString, dayOfYear);
      issues.add(OccurrenceIssue.RECORDED_DATE_MISMATCH);
      confidence = PROBABLE;
    }

    // Add an issue if the resolution af ymd / date / yDoy is different
    if (ymdResolution > 0 && dateStringResolution > 0) {
      if (ymdResolution != dateStringResolution) {
        log.debug("Date {}|{}|{}|{}|{} mismatch (resolution)", year, month, day, dateString, dayOfYear);
        issues.add(OccurrenceIssue.RECORDED_DATE_MISMATCH);
      }
    }

    // Best we can get from the three parts.
    // Note 2000-01-01 and 2000-01 and 2000 will return 2000-01-01.
    Optional<TemporalAccessor> nonConflictingTa =
        TemporalAccessorUtils.nonConflictingDateParts(parsedYmdTa, parsedDateTa, parsedYearDoyTa);

    if (nonConflictingTa.isPresent()) {
      parsedTemporalAccessor = nonConflictingTa.get();
      // if one of the parses failed we can not set the confidence to DEFINITE
      confidence =
          ((ymdProvided && parsedYmdTa == null)
                  || (dateStringProvided && parsedDateTa == null)
                  || (yDoyProvided && parsedYearDoyTa == null))
              ? PROBABLE
              : confidence;
    } else {
      log.debug("Date {}|{}|{}|{}|{} mismatch (conflicting)", year, month, day, dateString, dayOfYear);
      if (twoOrMoreProvided) {
        issues.add(OccurrenceIssue.RECORDED_DATE_MISMATCH);
      }
      return OccurrenceParseResult.fail(issues);
    }

    if (!isValidDate(parsedTemporalAccessor)) {
      if (parsedTemporalAccessor == null) {
        log.debug("Date {}|{}|{}|{}|{} mismatch (invalid)", year, month, day, dateString, dayOfYear);
        issues.add(OccurrenceIssue.RECORDED_DATE_INVALID);
      } else {
        log.debug("Date {}|{}|{}|{}|{} mismatch (unlikely)", year, month, day, dateString, dayOfYear);
        issues.add(OccurrenceIssue.RECORDED_DATE_UNLIKELY);
      }

      return OccurrenceParseResult.fail(issues);
    }

    return OccurrenceParseResult.success(confidence, parsedTemporalAccessor, issues);
  }

  public OccurrenceParseResult<TemporalAccessor> parseRecordedDate(String dateString) {
    return parseRecordedDate(null, null, null, dateString, null);
  }

  /** @return TemporalAccessor that represents a LocalDate or LocalDateTime */
  public OccurrenceParseResult<TemporalAccessor> parseLocalDate(
    String dateString, Range<LocalDate> likelyRange, OccurrenceIssue unlikelyIssue) {
    return parseLocalDate(dateString, likelyRange, unlikelyIssue, null);
  }

  /** @return TemporalAccessor that represents a LocalDate or LocalDateTime */
  public OccurrenceParseResult<TemporalAccessor> parseLocalDate(
      String dateString, Range<LocalDate> likelyRange, OccurrenceIssue unlikelyIssue, OccurrenceIssue failIssue) {
    if (!Strings.isNullOrEmpty(dateString)) {
      OccurrenceParseResult<TemporalAccessor> result =
          new OccurrenceParseResult<>(temporalParser.parse(dateString));
      // check year makes sense
      if (result.isSuccessful() && !isValidDate(result.getPayload(), likelyRange)) {
        log.debug("Unlikely date parsed, ignore [{}].", dateString);
        Optional.ofNullable(unlikelyIssue).ifPresent(result::addIssue);
      } else if (!result.isSuccessful()) {
        Optional.ofNullable(failIssue).ifPresent(result::addIssue);
      }
      return result;
    }
    return OccurrenceParseResult.fail();
  }

  /**
   * Check if a date express as TemporalAccessor falls between the predefined range. Lower bound
   * defined by {@link #MIN_LOCAL_DATE} and upper bound by current date + 1 day
   *
   * @return valid or not according to the predefined range.
   */
  protected static boolean isValidDate(TemporalAccessor temporalAccessor) {
    LocalDate upperBound = LocalDate.now().plusDays(1);
    return isValidDate(temporalAccessor, Range.closed(MIN_LOCAL_DATE, upperBound));
  }

  /** Check if a date express as TemporalAccessor falls between the provided range. */
  protected static boolean isValidDate(
      TemporalAccessor temporalAccessor, Range<LocalDate> likelyRange) {

    if (temporalAccessor == null) {
      return false;
    }

    // if partial dates should be considered valid
    int year;
    int month = 1;
    int day = 1;
    if (temporalAccessor.isSupported(ChronoField.YEAR)) {
      year = temporalAccessor.get(ChronoField.YEAR);
    } else {
      return false;
    }

    if (temporalAccessor.isSupported(ChronoField.MONTH_OF_YEAR)) {
      month = temporalAccessor.get(ChronoField.MONTH_OF_YEAR);
    }

    if (temporalAccessor.isSupported(ChronoField.DAY_OF_MONTH)) {
      day = temporalAccessor.get(ChronoField.DAY_OF_MONTH);
    }

    return likelyRange.contains(LocalDate.of(year, month, day));
  }
}
