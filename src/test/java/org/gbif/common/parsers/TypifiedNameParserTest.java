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

import org.junit.jupiter.api.Test;

public class TypifiedNameParserTest extends ParserTestBase<String> {

  public TypifiedNameParserTest() {
    super(TypifiedNameParser.getInstance());
  }

  @Test
  public void testParse() throws Exception {
    assertParseSuccess("Abies alba", null, " Holotype of Abies alba");
    assertParseSuccess("Dianthus fruticosus subsp. amorginus Runemark", null, "Holotype of Dianthus fruticosus ssp. amorginus Runemark");
    assertParseSuccess("Abies alba", null, " Holotype of: Abies alba");
    assertParseSuccess("Abies alba", null, " Holotype of  Abies alba.");

    assertParseFailure("Part of Holotype");
    assertParseFailure("Figured Specimen");
//    assertParseFailure("Cast of Figured Specimen");
    assertParseFailure("Cast of holotype");
    assertParseFailure("Cast of syntype");
    assertParseFailure("mark");
  }

}
