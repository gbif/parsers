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

import org.gbif.api.vocabulary.TaxonomicStatus;

import org.junit.jupiter.api.Test;

public class TaxStatusParserTest extends ParserTestBase<TaxonomicStatus> {

  public TaxStatusParserTest() {
    super(TaxStatusParser.getInstance());
  }

  /**
   * This ensures that ALL enum values are at least parsable by the name they
   * are created with.
   */
  @Test
  public void testCompleteness() {
    for (TaxonomicStatus t : TaxonomicStatus.values()) {
      assertParseSuccess(t, t.name());
      assertParseSuccess(t, t.name().toLowerCase());
      assertParseSuccess(t, t.name().replace("_", "").toLowerCase());
    }
  }

  @Test
  public void testFailures() {
    assertParseFailure(null);
    assertParseFailure("");
    assertParseFailure("Tim");
  }


  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(TaxonomicStatus.ACCEPTED, "a");
    assertParseSuccess(TaxonomicStatus.SYNONYM, "INValid");
    assertParseSuccess(TaxonomicStatus.HOMOTYPIC_SYNONYM, "is homotypic synonym of");
    assertParseSuccess(TaxonomicStatus.ACCEPTED, "NOME_ACEITO");

  }

}
