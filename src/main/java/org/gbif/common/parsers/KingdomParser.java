package org.gbif.common.parsers;

import org.gbif.api.vocabulary.Kingdom;
import org.gbif.common.parsers.core.EnumParser;

public class KingdomParser extends EnumParser<Kingdom> {
    private static KingdomParser singletonObject = null;

    private KingdomParser() {
        super(Kingdom.class, false);
        // make sure we have all continents from the enum
        for (Kingdom k : Kingdom.values()) {
            add(k.name(), k);
        }
        // use dict file last
        init(KingdomParser.class.getResourceAsStream("/dictionaries/parse/kingdoms.tsv"));
    }

    public static KingdomParser getInstance() {
        synchronized (KingdomParser.class) {
            if (singletonObject == null) {
                singletonObject = new KingdomParser();
            }
        }
        return singletonObject;
    }
}
