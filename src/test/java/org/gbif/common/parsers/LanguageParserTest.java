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


import org.gbif.api.vocabulary.Language;

import org.junit.jupiter.api.Test;

public class LanguageParserTest extends ParserTestBase<Language> {

  public LanguageParserTest() {
    super(LanguageParser.getInstance());
  }

  @Test
  public void testParseFail() {
    assertParseFailure("[West Indian Ocean]");
  }

  @Test
  public void testParseSuccess() {
    assertParseSuccess(Language.NORWEGIAN_BOKMAL, "NORWEGIAN_BOKMAL");
    assertParseSuccess(Language.NORWEGIAN_BOKMAL, "norwegian bokmal");
    assertParseSuccess(Language.NORWEGIAN_BOKMAL, "bokmal");
    assertParseSuccess(Language.GERMAN, "German");
    assertParseSuccess(Language.GERMAN, "deutsch");
    assertParseSuccess(Language.GERMAN, "de");
    assertParseSuccess(Language.GERMAN, "ger");
    assertParseSuccess(Language.BASQUE, "baq");
    assertParseSuccess(Language.GERMAN, "German");
    assertParseSuccess(Language.GERMAN, "Deutsch");
    assertParseSuccess(Language.GERMAN, "deutsch");
    assertParseSuccess(Language.GERMAN, "GER");
    assertParseSuccess(Language.GERMAN, "DEU");
    assertParseSuccess(Language.GERMAN, "de");
    assertParseSuccess(Language.GERMAN, "De ");
    assertParseSuccess(Language.ENGLISH, "en_US");
    assertParseSuccess(Language.ENGLISH, "eng_US");
    assertParseSuccess(Language.ENGLISH, "english");
    assertParseSuccess(Language.PORTUGUESE, "PORTUGUES");

  }

}
