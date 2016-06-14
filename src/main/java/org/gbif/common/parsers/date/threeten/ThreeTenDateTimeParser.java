package org.gbif.common.parsers.date.threeten;


import org.gbif.common.parsers.date.DateFormatHint;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Year;
import org.threeten.bp.YearMonth;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.temporal.TemporalAccessor;
import org.threeten.bp.temporal.TemporalQuery;

/**
 * Internal (package protected) wrapper of ThreeTen {@link DateTimeFormatter}.
 * It adds some flexibility with the {@link DateTimeSeparatorNormalizer} and
 * simple optimization the support of DateFormatHint.
 */
class ThreeTenDateTimeParser {

  private DateTimeFormatter formatter;
  private DateTimeSeparatorNormalizer normalizer;
  private DateFormatHint hint;

  private TemporalQuery<?>[] types;
  private int minLength;

  /**
   * Package protected constructor.
   * Use {@link ThreeTenNumericalDateParserBuilder}
   *
   * @param formatter
   * @param normalizer
   * @param hint
   * @param minLength
   */
  ThreeTenDateTimeParser(DateTimeFormatter formatter, DateTimeSeparatorNormalizer normalizer,
                         DateFormatHint hint, int minLength){
    this.formatter = formatter;
    this.hint = hint;
    this.normalizer = normalizer;
    this.minLength = minLength;
    this.types = getTypesFromHint(hint);
  }

  /**
   * The idea is to only use the types that are possible with DateTimeFormatter.parseBest method.
   *
   * @param hint
   * @return
   */
  private TemporalQuery<?>[] getTypesFromHint(DateFormatHint hint){
    switch(hint){
      case YMDT: return new TemporalQuery<?>[]{ZonedDateTime.FROM, LocalDateTime.FROM, LocalDate.FROM, YearMonth.FROM, Year.FROM};
      case YMD: return new TemporalQuery<?>[]{LocalDate.FROM, YearMonth.FROM, Year.FROM};
      case YM: return new TemporalQuery<?>[]{YearMonth.FROM, Year.FROM};
      case Y: return new TemporalQuery<?>[]{Year.FROM};
      case DMY:
      case MDY:
      case HAN:  return new TemporalQuery<?>[]{LocalDate.FROM, YearMonth.FROM, Year.FROM};
      case NONE:
      default: return new TemporalQuery<?>[]{LocalDateTime.FROM, LocalDate.FROM, YearMonth.FROM, Year.FROM};
    }
  }

  public DateFormatHint getHint() {
    return hint;
  }

  /**
   * Parse the provided String as a TemporalAccessor if possible, otherwise return null;
   *
   * @param input
   * @return
   */
  public TemporalAccessor parse(String input){

    // return fast if minimum length is not meet
    if(input.length() < minLength){
      return null;
    }

    if(normalizer != null){
      input = normalizer.normalize(input);
    }

    try {
      if(types.length > 1) {
        return formatter.parseBest(input, types);
      }
      return (TemporalAccessor)formatter.parse(input, types[0]);
    }
    catch (DateTimeParseException dpe){}
    return null;
  }

}
