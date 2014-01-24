package org.gbif.common.parsers.language;

import org.gbif.api.vocabulary.Language;
import org.gbif.common.parsers.InterpretedEnumParser;
import org.gbif.common.parsers.Parsable;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Interpreted enum parser version of LanguageParser.
 */
public class InterpretedLanguageParser extends InterpretedEnumParser<Language> {

  private static InterpretedLanguageParser singletonObject = null;
  private final Map<String, Language> pref2enum = Maps.newHashMap();

  private InterpretedLanguageParser(Class<Language> clazz, Parsable<String, String> parser) {
    super(clazz, parser);
    for (Language l : Language.values()) {
      pref2enum.put(l.getIso2LetterCode().toUpperCase(), l);
    }
  }

  public static InterpretedLanguageParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (InterpretedLanguageParser.class) {
      if (singletonObject == null) {
        singletonObject = new InterpretedLanguageParser(Language.class, LanguageParser.getInstance());
      }
    }
    return singletonObject;
  }

  @Override
  protected Language toEnum(String parsedValue) {
    if (parsedValue != null) {
      return pref2enum.get(parsedValue.trim().toUpperCase());
    }
    return null;
  }
}
