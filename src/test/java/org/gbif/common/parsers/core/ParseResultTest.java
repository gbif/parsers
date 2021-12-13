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
package org.gbif.common.parsers.core;


import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ParseResultTest {

  @Test
  public void testSuccess() {
    // check with basic string payload
    assertNotNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")));
    assertNotNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")).getPayload());
    assertEquals(String.class,
      ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")).getPayload().getClass());
    assertEquals(ParseResult.CONFIDENCE.DEFINITE,
      ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")).getConfidence());
    assertEquals(ParseResult.STATUS.SUCCESS,
      ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")).getStatus());
    assertNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new String("Bingo")).getError());

    // check generics with Date payload
    assertNotNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()));
    assertNotNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()).getPayload());
    assertEquals(Date.class, ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()).getPayload().getClass());
    assertEquals(ParseResult.CONFIDENCE.DEFINITE,
      ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()).getConfidence());
    assertEquals(ParseResult.STATUS.SUCCESS,
      ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()).getStatus());
    assertNull(ParseResult.success(ParseResult.CONFIDENCE.DEFINITE, new Date()).getError());

  }

  @Test
  public void testFail() {
    assertNotNull(ParseResult.fail());
    assertEquals(ParseResult.STATUS.FAIL, ParseResult.fail().getStatus());
    assertNull(ParseResult.fail().getConfidence());
    assertNull(ParseResult.fail().getError());
    assertNull(ParseResult.fail().getPayload());
  }

  @Test
  public void testUnknownError() {
    assertNotNull(ParseResult.error());
    assertEquals(ParseResult.STATUS.ERROR, ParseResult.error().getStatus());
    assertNull(ParseResult.error().getConfidence());
    assertNull(ParseResult.error().getError());
    assertNull(ParseResult.error().getPayload());
  }

  @Test
  public void testError() {
    assertNotNull(ParseResult.error(new RuntimeException("Bingo")));
    assertEquals(ParseResult.STATUS.ERROR, ParseResult.error(new RuntimeException("Bingo")).getStatus());
    assertNull(ParseResult.error(new RuntimeException("Bingo")).getConfidence());
    assertNotNull(ParseResult.error(new RuntimeException("Bingo")).getError());
    assertEquals(RuntimeException.class, ParseResult.error(new RuntimeException("Bingo")).getError().getClass());
    assertEquals("Bingo", ParseResult.error(new RuntimeException("Bingo")).getError().getMessage());
    assertNull(ParseResult.error(new RuntimeException("Bingo")).getPayload());
  }
}
