package org.gbif.common.parsers.date;


import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Year;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.temporal.TemporalAccessor;
import org.threeten.bp.temporal.TemporalQuery;

/**
 * Internal (package protected) wrapper of ThreeTen {@link DateTimeFormatter}.
 * It adds some flexibility with the {@linkInternalDateTimeNormalizer} and
 * simple optimization the support of DateFormatHint.
 */
class InternalDateTimeParser {

  private DateTimeFormatter formatter;
  private InternalDateTimeNormalizer normalizer;
  private ThreeTenNumericalDateParser.DateFormatHint hint;

  private TemporalQuery<?>[] types;
  private int minLength;

  /**
   * Private constructor.
   * Use static InternalDateTimeParser.of(...) methods.
   *
   * @param formatter
   * @param normalizer
   * @param hint
   * @param minLength
   */
  private InternalDateTimeParser(DateTimeFormatter formatter, InternalDateTimeNormalizer normalizer,
                                 ThreeTenNumericalDateParser.DateFormatHint hint, int minLength){
    this.formatter = formatter;
    this.hint = hint;
    this.normalizer = normalizer;
    this.minLength = minLength;
    this.types = getTypesFromHint(hint);
  }

  /**
   * Creates a {@link InternalDateTimeParser} without normalizer.
   *
   * @param formatter
   * @param hint
   * @param minLength
   * @return
   */
  public static InternalDateTimeParser of(DateTimeFormatter formatter, ThreeTenNumericalDateParser.DateFormatHint hint,
                                          int minLength){
    return new InternalDateTimeParser(formatter, null,  hint, minLength);
  }

  /**
   * Creates a {@link InternalDateTimeParser} that includes a {@link InternalDateTimeNormalizer}.
   *
   * @param formatter
   * @param normalizer
   * @param hint
   * @param minLength
   * @return
   */
  public static InternalDateTimeParser of(DateTimeFormatter formatter, InternalDateTimeNormalizer normalizer,
                                          ThreeTenNumericalDateParser.DateFormatHint hint, int minLength){
    return new InternalDateTimeParser(formatter, normalizer, hint, minLength);
  }

  /**
   * The idea is to only use the types that are possible with DateTimeFormatter.parseBest method.
   *
   * @param hint
   * @return
   */
  private TemporalQuery<?>[] getTypesFromHint(ThreeTenNumericalDateParser.DateFormatHint hint){
    switch(hint){
      case YMDT: return new TemporalQuery<?>[]{LocalDateTime.FROM, LocalDate.FROM, YearMonth.FROM, Year.FROM};
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

  public ThreeTenNumericalDateParser.DateFormatHint getHint() {
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
