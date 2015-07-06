package org.gbif.common.parsers;

import org.gbif.api.vocabulary.MediaType;
import org.gbif.common.parsers.core.EnumParser;
import org.gbif.dwc.terms.DcTerm;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/media_type.txt.
 */
public class MediaTypeParser extends EnumParser<MediaType> {

  private static MediaTypeParser singletonObject = null;

  private MediaTypeParser() {
    super(MediaType.class, false);
    // make sure we have media_type from the enum
    for (MediaType c : MediaType.values()) {
      add(c.name(), c);
    }
    for (MediaType c : MediaType.values()) {
    }
    // use dict file last
    init(MediaTypeParser.class.getResourceAsStream("/dictionaries/parse/media_type.txt"));
  }

  public static MediaTypeParser getInstance() {
    synchronized (MediaTypeParser.class) {
      if (singletonObject == null) {
        singletonObject = new MediaTypeParser();
      }
    }
    return singletonObject;
  }

}
