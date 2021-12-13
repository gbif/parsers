/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.common.parsers;

import org.gbif.api.model.common.MediaObject;
import org.gbif.api.vocabulary.MediaType;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaParser {
  private static final Logger LOG = LoggerFactory.getLogger(MediaParser.class);
  private static final Tika TIKA = new Tika();
  private static final MimeTypes MIME_TYPES = MimeTypes.getDefaultMimeTypes();
  private static final String HTML_TYPE = "text/html";
  // MIME types which we consider as HTML links instead of real media file URIs
  private static final Set<String> HTML_MIME_TYPES = Collections.unmodifiableSet(
      new HashSet<>(
          Arrays.asList("text/x-coldfusion", "text/x-php", "text/asp", "text/aspdotnet", "text/x-cgi",
              "text/x-jsp", "text/x-perl", HTML_TYPE, MimeTypes.OCTET_STREAM)));

  // List of exceptions, could be read from a file if it grows. URLs matching this return a media file.
  private static final Map<Pattern, String> knownUrlPatterns;

  // Add missing alias types.
  static {
    knownUrlPatterns = new HashMap<>();
    knownUrlPatterns.put(Pattern.compile("http://www\\.unimus\\.no/felles/bilder/web_hent_bilde\\.php\\?id=\\d+&type=jpeg"), "image/jpeg");
    knownUrlPatterns.put(Pattern.compile("http://www\\.jacq\\.org/image\\.php\\?filename=\\d+&method=europeana"), "image/jpeg");
    knownUrlPatterns.put(Pattern.compile("https://images\\.ala\\.org\\.au/image/proxyImageThumbnailLarge\\?imageId=[0-9a-f-]{36}"), "image/jpeg");
    knownUrlPatterns.put(Pattern.compile("http://[a-zA-Z0-9-]+\\.wildlifemonitoring\\.ru/get_photo\\.php\\?id=\\d+"), "image/jpeg");
    knownUrlPatterns.put(Pattern.compile("http://procyon\\.acadiau\\.ca/ecsmith/cgi-bin/image\\.cgi\\?[0-9A-Z]+,jpeg"), "image/jpeg");
    knownUrlPatterns.put(Pattern.compile("http://www\\.biologie\\.uni-ulm\\.de/cgi-bin/perl/sound\\.pl\\?sid=T&objid=\\d+"), "audio/vnd.wave");
    knownUrlPatterns.put(Pattern.compile("https://dofbasen\\.dk/sound_proxy\\.php\\?referer=gbif&mode=o&snd=[0-9_]+.mp3&raw=1"), "audio/mpeg");

    MediaTypeRegistry mediaTypeRegistry = MIME_TYPES.getMediaTypeRegistry();
    mediaTypeRegistry.addAlias(org.apache.tika.mime.MediaType.audio("mpeg"), org.apache.tika.mime.MediaType.audio("mp3"));
    mediaTypeRegistry.addAlias(org.apache.tika.mime.MediaType.audio("mpeg"), org.apache.tika.mime.MediaType.audio("mpeg3"));
  }

  private static MediaParser instance = null;

  public static MediaParser getInstance() {
    synchronized (MediaParser.class) {
      if (instance == null) {
        instance = new MediaParser();
      }
    }
    return instance;
  }

  public MediaObject detectType(MediaObject mo) {
    if (StringUtils.isEmpty(mo.getFormat())) {
      // derive from URI
      mo.setFormat(parseMimeType(mo.getIdentifier()));
    }

    // if MIME type is text/html make it a references link instead
    if (HTML_TYPE.equalsIgnoreCase(mo.getFormat()) && mo.getIdentifier() != null) {
      // make file URI the references link URL instead
      mo.setReferences(mo.getIdentifier());
      mo.setIdentifier(null);
      mo.setFormat(null);
    }

    if (StringUtils.isNotEmpty(mo.getFormat())) {
      if (mo.getFormat().startsWith("image")) {
        mo.setType(MediaType.StillImage);
      } else if (mo.getFormat().startsWith("audio")) {
        mo.setType(MediaType.Sound);
      } else if (mo.getFormat().startsWith("video")) {
        mo.setType(MediaType.MovingImage);
      } else {
        LOG.debug("Unsupported media format {}", mo.getFormat());
      }
    }
    return mo;
  }

  /**
   * Parses a MIME type using Apache Tika which can handle the following:
   * https://github.com/apache/tika/blob/master/tika-core/src/main/resources/org/apache/tika/mime/tika-mimetypes.xml
   * https://tika.apache.org/1.19.1/formats.html#Full_list_of_Supported_Formats
   */
  public String parseMimeType(@Nullable String format) {
    if (format != null) {
      format = StringUtils.trimToNull(format.trim().toLowerCase());
    }
    if (format == null) {
      return null;
    }

    try {
      MimeType mime = MIME_TYPES.getRegisteredMimeType(format);
      if (mime != null) {
        return mime.getName();
      }

    } catch (MimeTypeException e) {
    }

    // Failed, but return the input if it's a reasonable MIME type
    return MimeType.isValid(format) ? format : null;
  }

  /**
   * Parses a MIME type using Apache Tika which can handle the following:
   * https://github.com/apache/tika/blob/master/tika-core/src/main/resources/org/apache/tika/mime/tika-mimetypes.xml
   * https://tika.apache.org/1.19.1/formats.html#Full_list_of_Supported_Formats
   */
  public String parseMimeType(@Nullable URI uri) {
    if (uri != null) {
      String uriString = uri.toString();
      String mime = TIKA.detect(uriString);
      if (mime != null && HTML_MIME_TYPES.contains(mime.toLowerCase())) {
        // We may have something like http://example.org/imageServer?img=test.jpg, so re-run the detection on the last
        // part of the URL query string.
        if (uri.getQuery() != null) {
          mime = TIKA.detect(uri.getQuery());
          if (mime != null && !HTML_MIME_TYPES.contains(mime.toLowerCase())) {
            return mime;
          }
        }

        // First check the dictionary for known exceptions.
        for (Map.Entry<Pattern, String> p : knownUrlPatterns.entrySet()) {
          if (p.getKey().matcher(uriString).matches()) {
            return p.getValue();
          }
        }

        // links without any suffix default to OCTET STREAM, see:
        // http://dev.gbif.org/issues/browse/POR-2066
        return HTML_TYPE;
      }
      return mime;
    }
    return null;
  }
}
