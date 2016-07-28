package org.gbif.common.parsers;

import org.gbif.api.vocabulary.MaintenanceUpdateFrequency;
import org.gbif.common.parsers.core.EnumParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/maintenanceUpdateFrequency.txt.
 */
public class MaintenanceUpdateFrequencyParser extends EnumParser<MaintenanceUpdateFrequency> {

  private static MaintenanceUpdateFrequencyParser singletonObject = null;

  private MaintenanceUpdateFrequencyParser() {
    super(MaintenanceUpdateFrequency.class, false);
    // make sure we have all values from the enum
    for (MaintenanceUpdateFrequency m : MaintenanceUpdateFrequency.values()) {
      add(m.name(), m);
    }
    // use dict file last
    init(
      MaintenanceUpdateFrequencyParser.class.getResourceAsStream("/dictionaries/parse/maintenanceUpdateFrequency.txt"));
  }

  public static MaintenanceUpdateFrequencyParser getInstance() {
    synchronized (MaintenanceUpdateFrequencyParser.class) {
      if (singletonObject == null) {
        singletonObject = new MaintenanceUpdateFrequencyParser();
      }
    }
    return singletonObject;
  }
}
