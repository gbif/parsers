package org.gbif.common.parsers.date;

import java.util.List;

import com.google.common.collect.Lists;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * Allows to support more than one ThreeTenDateTimeParser that are considered "vague".
 * This class will try all the parsers and record all the results.
 */
public class ThreeTenDateTimeMultiParser {

  private ThreeTenDateTimeParser preferred;
  private List<ThreeTenDateTimeParser> otherParsers;
  private List<ThreeTenDateTimeParser> allParsers;

  ThreeTenDateTimeMultiParser(List<ThreeTenDateTimeParser> parsers){
    this(null, parsers);
  }

  ThreeTenDateTimeMultiParser(ThreeTenDateTimeParser preferred, List<ThreeTenDateTimeParser> otherParsers){
    this.preferred = preferred;
    this.otherParsers = Lists.newArrayList(otherParsers);
    this.allParsers = Lists.newArrayList();

    if(preferred != null){
      allParsers.add(preferred);
    }
    allParsers.addAll(otherParsers);
  }

  public List<ThreeTenDateTimeParser> getAllParsers(){
    return allParsers;
  }

  public MultipleParseResult parse(String input){
    int numberParsed = 0;
    TemporalAccessor lastParsed = null;

    TemporalAccessor preferredResult = null;
    //lazy initialized assuming it should not be used most of the time
    List<TemporalAccessor> otherResults = null;
    for(ThreeTenDateTimeParser currParser : otherParsers){
      lastParsed = currParser.parse(input);
      if(lastParsed != null){
        numberParsed++;
        if(otherResults == null){
          otherResults = Lists.newArrayList();
        }
        otherResults.add(lastParsed);
      }
    }

    //try the preferred ThreeTenDateTimeParser
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
