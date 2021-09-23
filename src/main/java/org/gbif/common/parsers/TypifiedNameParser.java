/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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

import org.gbif.api.exception.UnparsableException;
import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.common.parsers.core.Parsable;
import org.gbif.common.parsers.core.ParseResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.common.parsers.utils.NameParserUtils.PARSER;

/**
 * Singleton implementation using regex to extract a scientific name after a typestatus from a string.
 * For example given the input "Holotype of Dianthus fruticosus ssp. amorginus Runemark"
 * the parser will extract Dianthus fruticosus ssp. amorginus Runemark.
 */
public class TypifiedNameParser implements Parsable<String> {
  protected final Logger log = LoggerFactory.getLogger(getClass());
  private static TypifiedNameParser singletonObject = null;

  private static final Range<Integer> REASONABLE_NAME_SIZE_RANGE = Range.between(4, 40);
  private static final Pattern NAME_SEPARATOR = Pattern.compile("\\sOF\\W*\\s+\\W*(.+)\\W*\\s*$", Pattern.CASE_INSENSITIVE);
  private static final Pattern CLEAN_WHITESPACE = Pattern.compile("\\s+");

  private TypifiedNameParser() {
  }

  @Override
  public ParseResult<String> parse(String input) {
    if (StringUtils.isNotEmpty(input)) {
      Matcher m = NAME_SEPARATOR.matcher(input);
      if (m.find()) {
        String name = m.group(1);
        // make sure the name does not end with "type", see http://dev.gbif.org/issues/browse/POR-2703
        if (!name.endsWith("type")) {
          try {
            ParsedName pn = PARSER.parse(name,null);
            return ParseResult.success(ParseResult.CONFIDENCE.PROBABLE, pn.canonicalNameComplete());

          } catch (UnparsableException e) {
            log.debug("Cannot parse typified name: [{}] from input [{}]", name, input);
            name = CLEAN_WHITESPACE.matcher(name).replaceAll(" ").trim();
            if (REASONABLE_NAME_SIZE_RANGE.contains(name.length())) {
              return ParseResult.success(ParseResult.CONFIDENCE.POSSIBLE, name);
            }
          }
        }
      }
    }
    return ParseResult.fail();
  }

  public static TypifiedNameParser getInstance() {
    synchronized (TypifiedNameParser.class) {
      if (singletonObject == null) {
        singletonObject = new TypifiedNameParser();
      }
    }
    return singletonObject;
  }
}
