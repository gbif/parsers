package org.gbif.common.parsers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
    value = StringUtils.trimToEmpty(value);
    if (StringUtils.isEmpty(value)) {
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
      if (StringUtils.isEmpty(uri.getHost())) {
        return null;
      }

    } catch (IllegalArgumentException e) {
    }

    return uri;
  }


  /**
   * Parses a single string with null, one or many URIs concatenated together as found in dwc:associatedMedia.
   */
  public static List<URI> parseUriList(String uris) {
    List<URI> result = new ArrayList<>();

    if (StringUtils.isNotEmpty(uris)) {
      // first try to use the entire string
      URI uri = UrlParser.parse(uris);
      if (uri != null) {
        result.add(uri);

      } else {
        // try common delimiters
        int maxValidUrls = 0;
        for (String delimiter : MULTI_VALUE_DELIMITERS) {
          List<String> urls = Arrays.stream(StringUtils.splitByWholeSeparator(uris, delimiter))
              .filter(StringUtils::isNotBlank)
              .map(String::trim)
              .collect(Collectors.toList());

          // avoid parsing if we haven't actually split anything
          if (urls.size() > 1) {
            List<URI> tmp = new ArrayList<>();
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
