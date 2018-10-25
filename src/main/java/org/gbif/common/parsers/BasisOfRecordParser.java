package org.gbif.common.parsers;

import org.gbif.api.vocabulary.BasisOfRecord;
import org.gbif.common.parsers.core.EnumParser;

import java.io.InputStream;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/basisOfRecord.txt.
 */
public class BasisOfRecordParser extends EnumParser<BasisOfRecord> {

  private static BasisOfRecordParser singletonObject = null;

  private BasisOfRecordParser(InputStream... file) {
    super(BasisOfRecord.class, false, file);
  }

  public static BasisOfRecordParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (BasisOfRecordParser.class) {
      if (singletonObject == null) {
        singletonObject = new BasisOfRecordParser(BasisOfRecordParser.class.getResourceAsStream("/dictionaries/parse/basisOfRecord.tsv"));
      }
    }
    return singletonObject;
  }

}
