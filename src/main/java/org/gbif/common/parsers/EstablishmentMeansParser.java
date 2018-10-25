package org.gbif.common.parsers;

import org.gbif.api.vocabulary.EstablishmentMeans;
import org.gbif.common.parsers.core.EnumParser;

public class EstablishmentMeansParser extends EnumParser<EstablishmentMeans> {

  private static EstablishmentMeansParser singletonObject = null;

  private EstablishmentMeansParser() {
    super(EstablishmentMeans.class, false);
    // also make sure we have all official iso countries mapped
    for (EstablishmentMeans c : EstablishmentMeans.values()) {
      add(c.name(), c);
    }
    // use dict file last
    init(EstablishmentMeansParser.class.getResourceAsStream("/dictionaries/parse/establishment_means.tsv"));
  }

  public static EstablishmentMeansParser getInstance() {
    synchronized (EstablishmentMeansParser.class) {
      if (singletonObject == null) {
        singletonObject = new EstablishmentMeansParser();
      }
    }
    return singletonObject;
  }

}
