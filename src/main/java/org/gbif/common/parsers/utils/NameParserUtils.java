package org.gbif.common.parsers.utils;

import org.gbif.api.service.checklistbank.NameParser;
import org.gbif.nameparser.NameParserGbifV1;

/**
 * Mostly a singleton name parser to be shared.
 * The name parser uses a background thread so avoid creating new parsers unless needed.
 */
public class NameParserUtils {
  public static final NameParser PARSER = new NameParserGbifV1(1000);
}
