package org.gbif.common.parsers;

import org.gbif.api.vocabulary.OccurrenceStatus;
import org.gbif.common.parsers.core.EnumParser;

public class OccurrenceStatusParser extends EnumParser<OccurrenceStatus> {

  private static OccurrenceStatusParser singletonObject = null;

  private OccurrenceStatusParser() {
    super(OccurrenceStatus.class, false);
    // make sure we have all occurrence_status enum mapped
    for (OccurrenceStatus c : OccurrenceStatus.values()) {
      add(c.name(), c);
    }
    // use dict file last
    init(OccurrenceStatusParser.class.getResourceAsStream("/dictionaries/parse/occurrence_status.txt"));
  }

  public static OccurrenceStatusParser getInstance() {
    synchronized (OccurrenceStatusParser.class) {
      if (singletonObject == null) {
        singletonObject = new OccurrenceStatusParser();
      }
    }
    return singletonObject;
  }

}
