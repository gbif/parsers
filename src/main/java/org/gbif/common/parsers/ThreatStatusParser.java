package org.gbif.common.parsers;

import org.gbif.api.vocabulary.ThreatStatus;
import org.gbif.common.parsers.core.EnumParser;

public class ThreatStatusParser extends EnumParser<ThreatStatus> {

  private static ThreatStatusParser singletonObject = null;

  private ThreatStatusParser() {
    super(ThreatStatus.class, false);
    // also make sure we have all official iso countries mapped
    for (ThreatStatus c : ThreatStatus.values()) {
      add(c.name(), c);
      add(c.getCode(), c);
    }
    // use dict file last
    init(ThreatStatusParser.class.getResourceAsStream("/dictionaries/parse/threat_status.txt"));
  }

  public static ThreatStatusParser getInstance() {
    synchronized (ThreatStatusParser.class) {
      if (singletonObject == null) {
        singletonObject = new ThreatStatusParser();
      }
    }
    return singletonObject;
  }

}
