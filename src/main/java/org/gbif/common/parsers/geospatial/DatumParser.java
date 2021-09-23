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
package org.gbif.common.parsers.geospatial;

import org.gbif.common.parsers.core.ASCIIParser;
import org.gbif.common.parsers.core.FileBasedDictionaryParser;
import org.gbif.common.parsers.core.ParseResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Parser for geodetic datum strings into EPSG integer codes.
 * These codes are suitable for instantiating spatial reference systems (SRS) in spatial libraries like geotools.
 * See <a href="http://docs.geotools.org/latest/userguide/library/referencing/epsg.html">EPSG on Geotools</a>.
 *
 * For mapping common names to EPSG codes use the code registry search here:
 * <ul>
 *   <li>http://georepository.com/search/by-name/?query=samoa</li>
 *   <li>http://epsg.io/</li>
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
    init(DatumParser.class.getResourceAsStream("/dictionaries/parse/datum.tsv"));
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
    if (StringUtils.isEmpty(input)) {
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
    if (StringUtils.isEmpty(value)) return null;
    // convert to ascii
    ParseResult<String> asci = ascii.parse(value);
    return NORMALIZER.matcher(asci.getPayload()).replaceAll("").toUpperCase();
  }

  @Override
  protected Integer fromDictFile(String value) {
    if (StringUtils.isEmpty(value)) {
      return null;
    }
    return Integer.valueOf(value);
  }

}
