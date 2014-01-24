package org.gbif.common.parsers.countryname;

import org.gbif.api.vocabulary.Country;
import org.gbif.common.parsers.InterpretedEnumParser;
import org.gbif.common.parsers.Parsable;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Interpreted enum parser version of CountryNameParser.
 */
public class InterpretedCountryParser extends InterpretedEnumParser<Country> {

  private static InterpretedCountryParser singletonObject = null;
  private final Map<String, Country> pref2enum = Maps.newHashMap();

  private InterpretedCountryParser(Class<Country> clazz, Parsable<String, String> parser) {
    super(clazz, parser);
    for (Country l : Country.values()) {
      pref2enum.put(l.getIso2LetterCode().toUpperCase(), l);
    }
  }

  public static InterpretedCountryParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (InterpretedCountryParser.class) {
      if (singletonObject == null) {
        singletonObject = new InterpretedCountryParser(Country.class, CountryNameParser.getInstance());
      }
    }
    return singletonObject;
  }

  @Override
  protected Country toEnum(String parsedValue) {
    if (parsedValue != null) {
      return pref2enum.get(parsedValue.trim().toUpperCase());
    }
    return null;
  }
}
