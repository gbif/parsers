package org.gbif.common.parsers.basisofrecord;

import org.gbif.api.vocabulary.BasisOfRecord;
import org.gbif.common.parsers.InterpretedEnumParser;
import org.gbif.common.parsers.Parsable;
import org.gbif.common.parsers.language.InterpretedLanguageParser;

/**
 * Interpreted enum parser version of BasisOfRecordParser.
 */
public class InterpretedBasisOfRecordParser extends InterpretedEnumParser<BasisOfRecord> {

  private static InterpretedBasisOfRecordParser singletonObject = null;

  private InterpretedBasisOfRecordParser(Class<BasisOfRecord> clazz, Parsable<String, String> parser) {
    super(clazz, parser);
  }

  public static InterpretedBasisOfRecordParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (InterpretedLanguageParser.class) {
      if (singletonObject == null) {
        singletonObject = new InterpretedBasisOfRecordParser(BasisOfRecord.class, BasisOfRecordParser.getInstance());
      }
    }
    return singletonObject;
  }

}

