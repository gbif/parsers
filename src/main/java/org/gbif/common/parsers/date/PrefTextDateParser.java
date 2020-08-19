package org.gbif.common.parsers.date;

import java.time.temporal.TemporalAccessor;
import org.gbif.common.parsers.core.ParseResult;

/**
 * Wrap up a set of DateTimeFormatters to <code>parse</code> method.
 *
 * Use <code>orderings</code> formatters to parse date, if ISO formatter parsing process fails
 */
public class PrefTextDateParser extends TextDateParser {
  private DateComponentOrdering[] orderings;

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
    return super.parse(input, orderings);
  }

}
