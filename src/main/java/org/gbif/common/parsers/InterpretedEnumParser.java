package org.gbif.common.parsers;

import org.gbif.api.model.common.InterpretedEnum;
import org.gbif.common.parsers.ParseResult.CONFIDENCE;
import org.gbif.common.parsers.ParseResult.STATUS;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/rank.txt.
 */
public abstract class InterpretedEnumParser<T extends Enum<T>> implements Parsable<String, InterpretedEnum<String, T>> {

  private final Parsable<String, String> parser;
  private final Class<T> clazz;

  protected InterpretedEnumParser(Class<T> clazz, Parsable<String, String> parser) {
    this.parser = parser;
    this.clazz = clazz;
  }

  protected T toEnum(String parsedValue) {
    return T.valueOf(clazz, parsedValue);
  }

  @Override
  public ParseResult<InterpretedEnum<String, T>> parse(String input) {
    ParseResult<String> preferred = parser.parse(input);
    if (preferred.isSuccessful()) {
      try {
        InterpretedEnum<String, T> result = new InterpretedEnum<String, T>(input, toEnum(preferred.getPayload()));
        return new ParseResult<InterpretedEnum<String, T>>(STATUS.SUCCESS, preferred.getConfidence(), result, null);
      } catch (Exception e) {
        return new ParseResult<InterpretedEnum<String, T>>(STATUS.FAIL, CONFIDENCE.DEFINITE, null, e);
      }
    }

    return new ParseResult<InterpretedEnum<String, T>>(STATUS.FAIL, CONFIDENCE.DEFINITE, null, null);
  }
}
