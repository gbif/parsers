package org.gbif.common.parsers;

import org.gbif.api.vocabulary.NomenclaturalStatus;
import org.gbif.common.parsers.core.EnumParser;

import java.io.InputStream;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/rank.txt.
 */
public class NomStatusParser extends EnumParser<NomenclaturalStatus> {

  private static NomStatusParser singletonObject = null;

  private NomStatusParser(InputStream... file) {
    super(NomenclaturalStatus.class, false, file);
    // also make sure we have all enum knowledge mapped
    for (NomenclaturalStatus ns : NomenclaturalStatus.values()) {
      add(ns.getLatinLabel(), ns);
      add(ns.getAbbreviatedLabel(), ns);
    }
  }

  public static NomStatusParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (NomStatusParser.class) {
      if (singletonObject == null) {
        singletonObject = new NomStatusParser(NomStatusParser.class.getResourceAsStream("/dictionaries/parse/nomStatus.txt"));
      }
    }
    return singletonObject;
  }


}
