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

import org.gbif.api.vocabulary.DistributionStatus;

import org.junit.jupiter.api.Test;

public class DistributionStatusParserTest extends ParserTestBase<DistributionStatus> {

  public DistributionStatusParserTest() {
    super(DistributionStatusParser.getInstance());
  }

  @Test
  public void testParseAllEnumValues() {
    for (DistributionStatus c : DistributionStatus.values()) {
      assertParseSuccess(c, c.name());
      assertParseSuccess(c, c.name().toLowerCase());
    }
  }

  @Test
  public void testParseFail() {
    assertParseFailure("Really great!");
    assertParseFailure("Padua");
  }

  @Test
  public void testParsing() {
    assertParseSuccess(DistributionStatus.PRESENT, "present");
    assertParseSuccess(DistributionStatus.PRESENT, "endemic!");
    assertParseSuccess(DistributionStatus.RARE, "Uncommon");
  }
}
