package org.gbif.common.parsers;

import org.gbif.api.vocabulary.DistributionStatus;
import org.gbif.common.parsers.core.EnumParser;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/distribution_status.tsv.
 */
public class DistributionStatusParser extends EnumParser<DistributionStatus> {

  private static DistributionStatusParser singletonObject = null;

  private DistributionStatusParser() {
    super(DistributionStatus.class, false);
    // make sure we have all occurrence_status enum mapped
    for (DistributionStatus c : DistributionStatus.values()) {
      add(c.name(), c);
    }
    // use dict file last
    init(DistributionStatusParser.class.getResourceAsStream("/dictionaries/parse/distribution_status.tsv"));
  }

  public static DistributionStatusParser getInstance() {
    synchronized (DistributionStatusParser.class) {
      if (singletonObject == null) {
        singletonObject = new DistributionStatusParser();
      }
    }
    return singletonObject;
  }

}
