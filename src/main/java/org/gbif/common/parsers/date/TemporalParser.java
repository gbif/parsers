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
   * Parse a String date <em>restricted to the <code>ordering</code> provided</em>.
   *
   * Set a general date ordering for the parsing.  For example, DMY will support both 14.08.2020 and
   * 14/08/2020, but <em>not</em> 2020-08-14 or 08/14/2020.
   *
   * NOTE, this behaviour <strong>differs</strong> from {@link #parse(String, DateComponentOrdering[])}.
   *
   * @param ordering required date ordering.
   * @return result, never null
   */
  ParseResult<TemporalAccessor> parse(String input, @Nullable DateComponentOrdering ordering);

  /**
   * Parse a String date to a TemporalAccessor, attempting unambiguous formats and the
   * <code>orderings</code> provided.
   *
   * The date 2020-08-14 will always parse.  An <code>ordering</code> of DMY_FORMATS will also
   * support 14.08.2020, 14/08/2020 and 14/08/2020 14:11:00, but not 08/14/2020.
   *
   * NOTE, this behaviour <strong>differs</strong> from {@link #parse(String, DateComponentOrdering)}.
   *
   * @param orderings required general date orderings
   * @return result, never null
   */
  ParseResult<TemporalAccessor> parse(String input, @Nullable DateComponentOrdering[] orderings);

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
