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

import org.gbif.api.vocabulary.TypeStatus;
import org.gbif.common.parsers.core.EnumParser;
import org.gbif.common.parsers.core.ParseResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Singleton implementation of the dictionary that uses the file /dictionaries/parse/typeStatus.txt.
 */
public class TypeStatusParser extends EnumParser<TypeStatus> {

  private static TypeStatusParser singletonObject = null;
  private static final Pattern NAME_SEPARATOR = Pattern.compile("^(.+) (OF|FOR) ");

  private TypeStatusParser() {
    super(TypeStatus.class, false);
    init(TypeStatusParser.class.getResourceAsStream("/dictionaries/parse/typeStatus.tsv"));
  }

  @Override
  protected String normalize(String value) {
    if (StringUtils.isEmpty(value)) {
      return null;
    }
    // uppercase
    value = value.toUpperCase().trim();

    // keep only words before the typifiedName if existing, e.g. Holotype for "Holotype of Dianthus fruticosus ssp. amorginus Runemark"
    Matcher m = NAME_SEPARATOR.matcher(value);
    if (m.find()) {
      value = m.group(1);
    }
    // remove whitespace and non letters
    ParseResult<String> ascii = asciiParser.parse(value);

    // remove all non-letters
    return ascii.getPayload().chars()
        .filter(p -> Character.isLetter((char) p))
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }


  public static TypeStatusParser getInstance()
    throws ClassCastException, AbstractMethodError, ArithmeticException, ArrayIndexOutOfBoundsException {
    synchronized (TypeStatusParser.class) {
      if (singletonObject == null) {
        singletonObject = new TypeStatusParser();
      }
    }
    return singletonObject;
  }
}
