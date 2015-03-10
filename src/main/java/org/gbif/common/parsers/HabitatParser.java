package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Habitat;
import org.gbif.common.parsers.core.EnumParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/habitat.txt.
 */
public class HabitatParser extends EnumParser<Habitat> {

  private static HabitatParser singletonObject = null;

  private HabitatParser() {
    super(Habitat.class, false, HabitatParser.class.getResourceAsStream("/dictionaries/parse/habitat.txt"));
  }

  public static HabitatParser getInstance() {
    synchronized (HabitatParser.class) {
      if (singletonObject == null) {
        singletonObject = new HabitatParser();
      }
    }
    return singletonObject;
  }

  /**
   * Strip of any s at the end often found in english plurals
   */
  @Override
  protected String normalize(String value) {
    String x = super.normalize(value);
    if (x != null && x.length() > 1) {
      if (x.endsWith("S")) {
        return x.substring(0, x.length()-1);
      }
    }
    return x;
  }
}
