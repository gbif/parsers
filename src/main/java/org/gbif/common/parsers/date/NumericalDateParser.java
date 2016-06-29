package org.gbif.common.parsers.date;

import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;

import javax.annotation.Nullable;

import org.threeten.bp.temporal.TemporalAccessor;

/**
 *
 * Main interface for date parsing based on numerical values.
 *
 */
public interface NumericalDateParser extends Parsable<TemporalAccessor> {

  /**
   * Parse a date represented as a single String into a TemporalAccessor.
   *
   * @param input
   * @param hint help to speed up the parsing and possibly return a better confidence
   * @return
   */
  ParseResult<TemporalAccessor> parse(String input, @Nullable DateFormatHint hint);

  /**
   * Parse year, month, day strings as a TemporalAccessor.
   *
   * @param year
   * @param month
   * @param day
   * @return
   */
  ParseResult<TemporalAccessor> parse(@Nullable String year, @Nullable String month, @Nullable String day);

  /**
   * Parse year, month, day integers as a TemporalAccessor.
   *
   * @param year
   * @param month
   * @param day
   * @return
   */
  ParseResult<TemporalAccessor> parse(@Nullable Integer year, @Nullable Integer month, @Nullable Integer day);

}
