package org.gbif.common.parsers;

import org.gbif.api.model.common.MediaObject;
import org.gbif.api.vocabulary.MediaType;

import java.net.URI;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.apache.tika.Tika;
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
  // mime types which we consider as html links instead of real media file uris
  private static final Set<String> HTML_MIME_TYPES = ImmutableSet
    .of("text/x-coldfusion", "text/x-php", "text/asp", "text/aspdotnet", "text/x-cgi", "text/x-jsp", "text/x-perl",
      HTML_TYPE, MIME_TYPES.OCTET_STREAM);
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
    if (Strings.isNullOrEmpty(mo.getFormat())) {
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

    if (!Strings.isNullOrEmpty(mo.getFormat())) {
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
   * Parses a mime type using apache tika which can handle the following:
   * http://svn.apache.org/repos/asf/tika/trunk/tika-core/src/main/resources/org/apache/tika/mime/tika-mimetypes.xml
   */
  public String parseMimeType(@Nullable String format) {
    if (format != null) {
      format = Strings.emptyToNull(format.trim().toLowerCase());
    }

    try {
      MimeType mime = MIME_TYPES.getRegisteredMimeType(format);
      if (mime != null) {
        return mime.getName();
      }

    } catch (MimeTypeException e) {
    }

    // verify this is a reasonable mime type
    return format == null || MimeType.isValid(format) ? format : null;
  }

  /**
   * Parses a mime type using apache tika which can handle the following:
   * http://svn.apache.org/repos/asf/tika/trunk/tika-core/src/main/resources/org/apache/tika/mime/tika-mimetypes.xml
   */
  public String parseMimeType(@Nullable URI uri) {
    if (uri != null) {
      String mime = TIKA.detect(uri.toString());
      if (mime != null && HTML_MIME_TYPES.contains(mime.toLowerCase())) {
        // We may have something like http://example.org/imageServer?img=test.jpg, so re-run the detection on the last
        // part of the URL query string.
        if (uri.getQuery() != null) {
          mime = TIKA.detect(uri.getQuery());
          if (mime != null && !HTML_MIME_TYPES.contains(mime.toLowerCase())) {
            return mime;
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
