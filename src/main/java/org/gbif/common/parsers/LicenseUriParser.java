package org.gbif.common.parsers;

import com.google.common.base.Strings;
import org.gbif.api.vocabulary.License;
import org.gbif.common.parsers.core.ASCIIParser;
import org.gbif.common.parsers.core.FileBasedDictionaryParser;
import org.gbif.common.parsers.core.KeyValue;
import org.gbif.common.parsers.core.ParseResult;

import java.net.URI;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * A license parser giving URIs, compared to {@link License} which is for our GBIF enumeration.
 *
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/license_uri.txt to lookup a
 * License by its URI or its acronym/title, e.g. a lookup by "CC-BY 4.0" returns http://creativecommons.org/licenses/by/4.0/.
 * <br/>
 * Note a lookup by license acronym/title without a version number defaults to the latest version of that license,
 * e.g. a lookup by "CC-BY" returns http://creativecommons.org/licenses/by/4.0/.
 */
public class LicenseUriParser extends FileBasedDictionaryParser<URI> {

  private static final String COMMENT_MARKER = "#";
  private static final String LICENSE_FILEPATH = "/dictionaries/parse/license_uri.tsv";
  // allows us to remove the protocol part for http:// and https://
  private static final Pattern REMOVE_HTTP_PATTERN = Pattern.compile("^https?:\\/\\/", Pattern.CASE_INSENSITIVE);
  private static final Pattern NORMALIZER = Pattern.compile("[^\\p{IsAlphabetic}\\p{N}©]+");
  protected final ASCIIParser asciiParser = ASCIIParser.getInstance();
  private static LicenseUriParser singletonObject = null;

  private LicenseUriParser() {
    super(false);
    init(LicenseUriParser.class.getResourceAsStream(LICENSE_FILEPATH), COMMENT_MARKER);
  }

  /**
   * @param source To build the dictionary from
   */
  @Override
  public void init(Iterator<KeyValue<String, URI>> source) {
    while (source.hasNext()) {
      KeyValue<String, URI> kvp = source.next();
      add(kvp.getKey(), kvp.getValue());
      // Also adds the value, to save defining all of them.
      add(kvp.getValue().toString(), kvp.getValue());
    }
  }

  @Override
  protected String normalize(String value) {
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }
    ParseResult<String> ascii = asciiParser.parse(value);
    String noHttp = REMOVE_HTTP_PATTERN.matcher(ascii.getPayload()).replaceAll("").toLowerCase();
    return super.normalize(NORMALIZER.matcher(noHttp).replaceAll(""));
  }

  public static LicenseUriParser getInstance() {
    synchronized (LicenseUriParser.class) {
      if (singletonObject == null) {
        singletonObject = new LicenseUriParser();
      }
    }
    return singletonObject;
  }

  @Override
  protected URI fromDictFile(String value) {
    try {
      return URI.create(value);
    } catch (RuntimeException e) {
      return null;
    }
  }
}
