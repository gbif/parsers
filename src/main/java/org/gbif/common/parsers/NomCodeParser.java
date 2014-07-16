package org.gbif.common.parsers;

import org.gbif.api.vocabulary.NomenclaturalCode;
import org.gbif.common.parsers.core.EnumParser;

import java.io.InputStream;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/rank.txt.
 */
public class NomCodeParser extends EnumParser<NomenclaturalCode> {

    private static NomCodeParser singletonObject = null;

    private NomCodeParser(InputStream... file) {
        super(NomenclaturalCode.class, false, file);
        // also make sure we have all enum knowledge mapped
        for (NomenclaturalCode c : NomenclaturalCode.values()) {
            add(c.getAcronym(), c);
            add(c.getTitle(), c);
        }
    }

    public static NomCodeParser getInstance()
        throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
        synchronized (NomCodeParser.class) {
            if (singletonObject == null) {
                singletonObject = new NomCodeParser(NomCodeParser.class.getResourceAsStream("/dictionaries/parse/nomCode.txt"));
            }
        }
        return singletonObject;
    }


}
