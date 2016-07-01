package org.gbif.common.parsers.date;

import java.util.List;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * Supports multiple {@link DateTimeParser} that are considered ambiguous. Two {@link DateTimeParser} are considered
 * ambiguous when they can possibly produce 2 different {@link TemporalAccessor}.
 * e.g. "dd/MM/yyyy" and "MM/dd/yyyy"
 *
 * This class will try all the parsers and keep the all the successful results.
 *
 * This class is thread-safe once an instance is created.
 */
class DateTimeMultiParser {

  private final DateTimeParser preferred;
  private final List<DateTimeParser> otherParsers;
  private final List<DateTimeParser> allParsers;

  /**
   * Create a new instance of {@link DateTimeMultiParser}.
   * @param parsers requires more than 1 element in list
   */
  DateTimeMultiParser(@NotNull List<DateTimeParser> parsers){
    this(null, parsers);
  }

  /**
   *
   * Create a new instance of {@link DateTimeMultiParser}.
   * At least 2 {@link DateTimeParser} must be provided see details on parameters.
   *
   * @param preferred the preferred {@link DateTimeParser} or null
   * @param otherParsers list of {@link DateTimeParser} containing more than 1 element if no
   *                     preferred {@link DateTimeParser} is provided. Otherwise, the list must contain at least 1 element.
   */
  DateTimeMultiParser(@Nullable DateTimeParser preferred, @NotNull List<DateTimeParser> otherParsers){

    Preconditions.checkNotNull(otherParsers, "otherParsers list can not be null");
    Preconditions.checkArgument(otherParsers.size() > 0, "otherParsers must contain at least 1 element");

    if(preferred == null) {
      Preconditions.checkArgument(otherParsers.size() > 1, "If no preferred DateTimeParser is provided, " +
              "the otherParsers list must contain more than 1 element");
    }

    this.preferred = preferred;
    this.otherParsers = Lists.newArrayList(otherParsers);

    ImmutableList.Builder immutableListBuilder = new ImmutableList.Builder<DateTimeParser>();
    if(preferred != null){
      immutableListBuilder.add(preferred);
    }
    immutableListBuilder.addAll(otherParsers);

    this.allParsers = immutableListBuilder.build();
  }

  /**
   * Get the list of all parsers: the preferred (if specified in the constructor) + otherParsers.
   *
   * @return never null
   */
  public List<DateTimeParser> getAllParsers(){
    return allParsers;
  }

  /**
   * Try to parse the input using all the parsers specified in the constructor.
   *
   * @param input
   * @return {@link MultipleParseResult} instance, never null.
   */
  public MultipleParseResult parse(String input){
    int numberParsed = 0;
    TemporalAccessor lastParsed = null;
    TemporalAccessor preferredResult = null;

    //lazy initialized assuming it should not be used most of the time
    List<TemporalAccessor> otherResults = null;
    for(DateTimeParser currParser : otherParsers){
      lastParsed = currParser.parse(input);
      if(lastParsed != null){
        numberParsed++;
        if(otherResults == null){
          otherResults = Lists.newArrayList();
        }
        otherResults.add(lastParsed);
      }
    }

    //try the preferred DateTimeParser
    if(this.preferred != null){
      lastParsed = this.preferred.parse(input);
      if(lastParsed != null){
        numberParsed++;
        preferredResult = lastParsed;
      }
    }
    return new MultipleParseResult(numberParsed, preferredResult, otherResults);
  }

  /**
   * Nested class representing the result of a multi-parse.
   *
   */
  public static class MultipleParseResult {
    private int numberParsed;
    private TemporalAccessor preferredResult;
    private List<TemporalAccessor> otherResults;

    public MultipleParseResult(int numberParsed, TemporalAccessor preferredResult, List<TemporalAccessor> otherResults){
      this.numberParsed = numberParsed;
      this.preferredResult = preferredResult;
      this.otherResults = otherResults;
    }

    public int getNumberParsed() {
      return numberParsed;
    }

    public TemporalAccessor getPreferredResult() {
      return preferredResult;
    }

    public List<TemporalAccessor> getOtherResults() {
      return otherResults;
    }

    /**
     * Return the preferredResult if available otherwise the first element of otherResults.
     * If otherResults is empty, null is returned.
     * @return
     */
    public TemporalAccessor getResult() {
      if(preferredResult != null) {
        return preferredResult;
      }

      if(otherResults != null && otherResults.size() > 0){
        return otherResults.get(0);
      }
      return null;
    }
  }
}
