package org.gbif.common.parsers.typestatus;

import org.gbif.api.vocabulary.TypeStatus;
import org.gbif.common.parsers.InterpretedEnumParser;
import org.gbif.common.parsers.Parsable;
import org.gbif.common.parsers.language.InterpretedLanguageParser;

/**
 * Interpreted enum parser version of TypeStatusParser.
 */
public class InterpretedTypeStatusParser extends InterpretedEnumParser<TypeStatus> {

  private static InterpretedTypeStatusParser singletonObject = null;

  private InterpretedTypeStatusParser(Class<TypeStatus> clazz, Parsable<String, String> parser) {
    super(clazz, parser);
  }

  public static InterpretedTypeStatusParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (InterpretedLanguageParser.class) {
      if (singletonObject == null) {
        singletonObject = new InterpretedTypeStatusParser(TypeStatus.class, TypeStatusParser.getInstance());
      }
    }
    return singletonObject;
  }

}

