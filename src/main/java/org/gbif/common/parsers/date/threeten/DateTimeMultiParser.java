package org.gbif.common.parsers.date.threeten;

import java.util.List;

import com.google.common.collect.Lists;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * Internal (package protected) wrapper to support more than one DateTimeParser that are considered "vague".
 * This class will try all the parsers and record all the results.
 */
class DateTimeMultiParser {

  private DateTimeParser preferred;
  private List<DateTimeParser> otherParsers;
  private List<DateTimeParser> allParsers;

  DateTimeMultiParser(List<DateTimeParser> parsers){
    this(null, parsers);
  }

  DateTimeMultiParser(DateTimeParser preferred, List<DateTimeParser> otherParsers){
    this.preferred = preferred;
    this.otherParsers = Lists.newArrayList(otherParsers);
    this.allParsers = Lists.newArrayList();

    if(preferred != null){
      allParsers.add(preferred);
    }
    allParsers.addAll(otherParsers);
  }

  public List<DateTimeParser> getAllParsers(){
    return allParsers;
  }

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
