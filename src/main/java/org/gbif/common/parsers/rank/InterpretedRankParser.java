package org.gbif.common.parsers.rank;

import org.gbif.api.vocabulary.Rank;
import org.gbif.common.parsers.InterpretedEnumParser;
import org.gbif.common.parsers.Parsable;
import org.gbif.common.parsers.language.InterpretedLanguageParser;


/**
 * Interpreted enum parser version of TypeStatusParser.
 */
public class InterpretedRankParser extends InterpretedEnumParser<Rank> {

  private static InterpretedRankParser singletonObject = null;

  private InterpretedRankParser(Class<Rank> clazz, Parsable<String, String> parser) {
    super(clazz, parser);
  }

  public static InterpretedRankParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (InterpretedLanguageParser.class) {
      if (singletonObject == null) {
        singletonObject = new InterpretedRankParser(Rank.class, RankParser.getInstance());
      }
    }
    return singletonObject;
  }

}

