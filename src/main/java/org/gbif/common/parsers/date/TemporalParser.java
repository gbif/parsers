package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;

import java.time.temporal.TemporalAccessor;
import javax.annotation.Nullable;

/**
 * Main interface for date/time parsing based.
 */
public interface TemporalParser extends Parsable<TemporalAccessor> {

  /**
   * Parse a date represented as a single String into a TemporalAccessor.
   *
   * @param input
   * @return result, never null
   */
  ParseResult<TemporalAccessor> parse(String input);

  /**
   * Parse a date represented as a single String into a TemporalAccessor.
   *
   * Set a specific date format (hint) for the parsing.
   *
   * @param input
   * @param hint help to speed up the parsing and possibly return a better confidence
   * @return result, never null
   */
  ParseResult<TemporalAccessor> parse(String input, @Nullable DateFormatHint hint);

  /**
   * Parse year, month, day strings as a TemporalAccessor.
   *
   * @param year numerical value of a year
   * @param month value of the mont depending on the implementation, numerical value of a
   *              month (starting at 1 for January) or possibly text.
   * @param day numerical value of a day
   * @return result, never null
   */
  ParseResult<TemporalAccessor> parse(@Nullable String year, @Nullable String month, @Nullable String day);

  /**
   * Parse year, month, day integers as a TemporalAccessor.
   *
   * @param year numerical value of a year
   * @param month numerical value of a month (starting at 1 for January)
   * @param day numerical value of a day
   * @return result, never null
   */
  ParseResult<TemporalAccessor> parse(@Nullable Integer year, @Nullable Integer month, @Nullable Integer day);
}
