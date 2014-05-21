package org.gbif.common.parsers.geospatial;

import org.gbif.common.parsers.core.ASCIIParser;
import org.gbif.common.parsers.core.FileBasedDictionaryParser;
import org.gbif.common.parsers.core.ParseResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

/**
 * Parser for geodetic datum strings into EPSG integer codes.
 * These codes are suitable for instantiating spatial reference systems (SRS) in spatial libraries like geotools.
 * See <a href="http://docs.geotools.org/latest/userguide/library/referencing/epsg.html">EPSG on Geotools</a>.
 *
 * For mapping common names to EPSG codes use the code registry search here:
 * <ul>
 *   <li>http://georepository.com/search/by-name/?query=samoa</li>
 *   <li>http://www.epsg-registry.org/</li>
 *   <li>http://prj2epsg.org/apidocs.html</li>
 * </ul>
 *
 */
public class DatumParser extends FileBasedDictionaryParser<Integer> {
  private static DatumParser singletonObject;
  private final Pattern EPSG = Pattern.compile("\\s*(EPSG|ESPG)\\s*:+\\s*(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);
  private final Pattern NORMALIZER = Pattern.compile("[^a-zA-Z0-9]+");
  private final ASCIIParser ascii = ASCIIParser.getInstance();

  private DatumParser() {
    super(false);
    init(DatumParser.class.getResourceAsStream("/dictionaries/parse/datum.txt"));
  }

  public static DatumParser getInstance() {
    synchronized (DatumParser.class) {
      if (singletonObject == null) {
        singletonObject = new DatumParser();
      }
    }
    return singletonObject;
  }

  @Override
  public ParseResult<Integer> parse(String input) {
    if (Strings.isNullOrEmpty(input)) {
      return null;
    }
    // try EPSG codes directly, allow common typo
    Matcher m = EPSG.matcher(input);
    if (m.find()) {
      Integer code = Integer.valueOf(m.group(2));
      return ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, code);
    }
    // try dictionary for well known SRS names
    return super.parse(input);
  }

  @Override
  protected String normalize(String value) {
    if (Strings.isNullOrEmpty(value)) return null;
    // convert to ascii
    ParseResult<String> asci = ascii.parse(value);
    return NORMALIZER.matcher(asci.getPayload()).replaceAll("").toUpperCase();
  }

  @Override
  protected Integer fromDictFile(String value) {
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }
    return Integer.valueOf(value);
  }

}
