package org.gbif.common.parsers.date;

import java.io.Serializable;
import java.time.temporal.TemporalAccessor;
import org.gbif.common.parsers.core.ParseResult;
import javax.annotation.Nullable;

/**
 * Wrap up a set of DateTimeFormatters to <code>parse</code> method.
 *
 * Use <code>orderings</code> formatters to parse date, if ISO formatter parsing process fails
 */
public class PrefTextDateParser implements TemporalParser, Serializable {
  private DateComponentOrdering[] orderings;
  private TextDateParser parser = new TextDateParser();
  /**
   * @param orderings a set of DateTimeFormatters
   * @return
   */
  public static TemporalParser getInstance(DateComponentOrdering[] orderings){
      PrefTextDateParser ptdp = new PrefTextDateParser();
      ptdp.orderings = orderings;
      return ptdp;
  }

  @Override
  public ParseResult<TemporalAccessor> parse(String input) {
    return parser.parse(input, orderings);
  }

  @Override
  public ParseResult<TemporalAccessor> parse(String input,
    @Nullable DateComponentOrdering ordering) {
    return parser.parse(input,ordering);
  }

  @Override
  public ParseResult<TemporalAccessor> parse(String input,
    @Nullable DateComponentOrdering[] orderings) {
    return parser.parse(input, orderings);
  }

  @Override
  public ParseResult<TemporalAccessor> parse(
    @Nullable String year,
    @Nullable String month,
    @Nullable String day) {
    return parser.parse(year, month, day);
  }

  @Override
  public ParseResult<TemporalAccessor> parse(
    @Nullable Integer year,
    @Nullable Integer month,
    @Nullable Integer day) {
    return parser.parse(year, month, day);
  }
}
