package org.gbif.common.parsers;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.gbif.api.vocabulary.NomenclaturalStatus;
import org.gbif.common.parsers.core.EnumParser;
import org.gbif.common.parsers.core.ParseResult;

import java.io.InputStream;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/rank.txt.
 */
public class NomStatusParser extends EnumParser<NomenclaturalStatus> {
    private Pattern CLEAN_PREFIX = Pattern.compile("\\s*\\.?\\s*");
    private Map<String, NomenclaturalStatus> PREFIXES = ImmutableMap.<String, NomenclaturalStatus>builder()
        .put("nom illeg", NomenclaturalStatus.ILLEGITIMATE)
        .put("nom inval", NomenclaturalStatus.INVALID)
        .put("comb nov", NomenclaturalStatus.NEW_COMBINATION)
        .put("nom nov", NomenclaturalStatus.REPLACEMENT)
        .put("nom nud", NomenclaturalStatus.NUDUM)
        .put("nom rej", NomenclaturalStatus.REJECTED)
        .put("unavailable", NomenclaturalStatus.INVALID)
        .build();
  private static NomStatusParser singletonObject = null;

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
        if (!result.isSuccessful() && !Strings.isNullOrEmpty(input)) {
            String normed = CLEAN_PREFIX.matcher(input).replaceFirst(" ").trim().toLowerCase();
            if (!Strings.isNullOrEmpty(normed)) {
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
        singletonObject = new NomStatusParser(NomStatusParser.class.getResourceAsStream("/dictionaries/parse/nomStatus.txt"));
      }
    }
    return singletonObject;
  }


}
