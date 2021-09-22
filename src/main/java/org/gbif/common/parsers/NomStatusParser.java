package org.gbif.common.parsers;

import org.apache.commons.lang3.StringUtils;
import org.gbif.api.vocabulary.NomenclaturalStatus;
import org.gbif.common.parsers.core.EnumParser;
import org.gbif.common.parsers.core.ParseResult;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/nomStatus.txt.
 */
public class NomStatusParser extends EnumParser<NomenclaturalStatus> {
    private Pattern CLEAN_PREFIX = Pattern.compile("\\s*\\.?\\s*");
    private final Map<String, NomenclaturalStatus> PREFIXES;
  private static NomStatusParser singletonObject = null;

  {
    Map<String, NomenclaturalStatus> prefixes = new HashMap<>();
    prefixes.put("nom illeg", NomenclaturalStatus.ILLEGITIMATE);
    prefixes.put("nom inval", NomenclaturalStatus.INVALID);
    prefixes.put("comb nov", NomenclaturalStatus.NEW_COMBINATION);
    prefixes.put("nom nov", NomenclaturalStatus.REPLACEMENT);
    prefixes.put("nom nud", NomenclaturalStatus.NUDUM);
    prefixes.put("nom rej", NomenclaturalStatus.REJECTED);
    prefixes.put("unavailable", NomenclaturalStatus.INVALID);
    PREFIXES = Collections.unmodifiableMap(prefixes);
  }

  private NomStatusParser(InputStream... file) {
    super(NomenclaturalStatus.class, false, file);
    // also make sure we have all enum knowledge mapped
    for (NomenclaturalStatus ns : NomenclaturalStatus.values()) {
      add(ns.getLatinLabel(), ns);
      add(ns.getAbbreviatedLabel(), ns);
    }
  }

    @Override
    public ParseResult<NomenclaturalStatus> parse(String input) {
        ParseResult<NomenclaturalStatus> result = super.parse(input);
        if (!result.isSuccessful() && StringUtils.isNotEmpty(input)) {
            String normed = CLEAN_PREFIX.matcher(input).replaceFirst(" ").trim().toLowerCase();
            if (StringUtils.isNotEmpty(normed)) {
                // try generic parsing of status prefixes only
                for (Map.Entry<String, NomenclaturalStatus> entry : PREFIXES.entrySet()) {
                    if (normed.startsWith(entry.getKey())) {
                        return ParseResult.success(ParseResult.CONFIDENCE.PROBABLE, entry.getValue());
                    }
                }
            }
        }
        return result;
    }

    public static NomStatusParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (NomStatusParser.class) {
      if (singletonObject == null) {
        singletonObject = new NomStatusParser(NomStatusParser.class.getResourceAsStream("/dictionaries/parse/nomStatus.tsv"));
      }
    }
    return singletonObject;
  }


}
