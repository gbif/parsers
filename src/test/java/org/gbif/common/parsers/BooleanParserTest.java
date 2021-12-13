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


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BooleanParserTest extends ParserTestBase<Boolean>  {

  public BooleanParserTest() {
    super(BooleanParser.getInstance());
  }


  @Test
  public void testParse() {
    // run a few basic tests to check it bootstraps and appears to work
    assertParseSuccess(true, "t");
    assertParseSuccess(true, "1");
    assertParseSuccess(true, "true");
    assertParseSuccess(true, "True");
    assertParseSuccess(true, "wahr");
    assertParseSuccess(true, "T");

    assertParseSuccess(Boolean.FALSE, "False");
    assertParseSuccess(false, "falsch");
    assertParseSuccess(false, "0");
    assertParseSuccess(false, "-1");
    assertParseSuccess(false, "no");
  }
}
