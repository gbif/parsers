package org.gbif.common.parsers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Greedy URL parser assuming HTTP URIs in case no schema was given.
 * Modified version of the registry-metadata GreedyUriConverter.
 */
public class UrlParser {
  private static final Logger LOG = LoggerFactory.getLogger(UrlParser.class);
  private static final String[] MULTI_VALUE_DELIMITERS = {"|#DELIMITER#|", "|", ",", ";"};
  private static final String HTTP_SCHEME = "http://";

  // Pattern for things that are probably domains followed by a slash, without a protocol.
  // Doesn't match IDNs etc, but this is just for people who forgot the http:// anyway.
  // The longest TLD currently in existence is 24 characters long, but can be up to 63 according to specs.
  private static final Pattern DOMAIN_ISH = Pattern.compile("^[A-Za-z0-9.-]{1,60}\\.[A-Za-z]{2,10}(?:/.*)?");

  private UrlParser() {
  }

  /**
   * Convert a String into a java.net.URI.
   * In case its missing the protocol prefix, it is prefixed with the default protocol.
   *
   * @param value The input value to be converted
   *
   * @return The converted value, or null if not parsable or exception occurred
   */
  public static URI parse(String value) {
    value = CharMatcher.WHITESPACE.trimFrom(Strings.nullToEmpty(value));
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }

    URI uri = null;
    try {
      uri = URI.create(value);
      if (!uri.isAbsolute() && DOMAIN_ISH.matcher(value).matches()) {
        // make into an HTTP address
        try {
          uri = URI.create(HTTP_SCHEME + value);
        } catch (IllegalArgumentException e) {
          // keep the previous scheme-less result
        }
      }

      // verify that we have a domain
      if (Strings.isNullOrEmpty(uri.getHost())) {

        // If not, try the Galimatias parser.
        try {
          uri = URL.parse(value).toJavaURI();
        } catch (GalimatiasParseException | URISyntaxException ex) {
          // Non-recoverable parsing error
        }

        if (Strings.isNullOrEmpty(uri.getHost())) {
          return null;
        }
      }
    } catch (IllegalArgumentException e) {
    }

    return uri;
  }


  /**
   * Parses a single string with null, one or many URIs concatenated together as found in dwc:associatedMedia.
   */
  public static List<URI> parseUriList(String uris) {
    List<URI> result = Lists.newArrayList();

    if (!Strings.isNullOrEmpty(uris)) {
      // first try to use the entire string
      URI uri = UrlParser.parse(uris);
      if (uri != null) {
        result.add(uri);

      } else {
        // try common delimiters
        int maxValidUrls = 0;
        for (String delimiter : MULTI_VALUE_DELIMITERS) {
          Splitter splitter = Splitter.on(delimiter).omitEmptyStrings().trimResults();
          String[] urls = Iterables.toArray(splitter.split(uris), String.class);
          // avoid parsing if we haven' actually split anything
          if (urls.length > 1) {
            List<URI> tmp = Lists.newArrayList();
            for (String url : urls) {
              uri = UrlParser.parse(url);
              if (uri != null) {
                tmp.add(uri);
              }
            }
            if (tmp.size() > maxValidUrls) {
              result = tmp;
              maxValidUrls = tmp.size();
            } else if (maxValidUrls > 0 && tmp.size() == maxValidUrls) {
              LOG.info("Unclear what delimiter is being used for concatenated URIs = {}", uris);
            }
          }
        }
      }
    }
    return result;
  }

}
