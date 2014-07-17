package org.gbif.common.parsers;

import org.gbif.api.vocabulary.TaxonomicStatus;
import org.gbif.common.parsers.core.EnumParser;

import java.io.InputStream;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/rank.txt.
 */
public class TaxStatusParser extends EnumParser<TaxonomicStatus> {

  private static TaxStatusParser singletonObject = null;

  private TaxStatusParser(InputStream... file) {
    super(TaxonomicStatus.class, false, file);
  }

  public static TaxStatusParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (TaxStatusParser.class) {
      if (singletonObject == null) {
        singletonObject = new TaxStatusParser(TaxStatusParser.class.getResourceAsStream("/dictionaries/parse/taxStatus.txt"));
      }
    }
    return singletonObject;
  }


}
