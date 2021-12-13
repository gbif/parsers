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

/**
 * Generic interface to allow multiple parser implementations.
 *
 * @param <T> The output type of the parse operation
 */
public interface Parsable<T> {

  /**
   * Tries to parse the input and returns a {@link ParseResult} object.
   * This should never return <code>null</code> as parse errors will be indicated in the returned object.
   *
   * @param input To parse
   *
   * @return The output result of the operation
   */
  ParseResult<T> parse(String input);
}
