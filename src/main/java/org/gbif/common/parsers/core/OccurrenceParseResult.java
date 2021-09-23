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
package org.gbif.common.parsers.core;

import org.gbif.api.vocabulary.OccurrenceIssue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class OccurrenceParseResult<T> extends ParseResult<T> {
  private final Set<OccurrenceIssue> issues = EnumSet.noneOf(OccurrenceIssue.class);

  public OccurrenceParseResult(STATUS status, CONFIDENCE confidence, T payload, List<T> alternativePayloads, Throwable error, Collection<OccurrenceIssue> issues) {
    super(status, confidence, payload, alternativePayloads, error);
    if (issues != null) {
      // add non nulls only
      issues.stream()
          .filter(Objects::nonNull)
          .forEach(this.issues::add);
    }
  }

  public OccurrenceParseResult(STATUS status, CONFIDENCE confidence, T payload, List<T> alternativePayloads, Throwable error) {
    super(status, confidence, payload, alternativePayloads, error);
  }

  public OccurrenceParseResult(ParseResult<T> result) {
    super(result.getStatus(), result.getConfidence(), result.getPayload(), result.getAlternativePayloads(), result.getError());
  }

  /**
   * @param <T>       The generic type of the payload
   * @param confidence The confidence in the result
   * @param payload    The payload of the parse result
   *
   * @return The new ParseResult which has no error and status of SUCCESS
   */
  public static <T> OccurrenceParseResult<T> success(CONFIDENCE confidence, T payload) {
    return new OccurrenceParseResult<>(STATUS.SUCCESS, confidence, payload, null, null);
  }

  /**
   * @return A new parse response with only the status set to FAIL
   */
  public static <T> OccurrenceParseResult<T> fail() {
    return new OccurrenceParseResult<>(STATUS.FAIL, null, null, null, null);
  }

  /**
   * @return A new parse response configured to indicate an error
   */
  public static <T> OccurrenceParseResult<T> error() {
    return new OccurrenceParseResult<>(STATUS.ERROR, null, null, null, null);
  }

  /**
   * @param cause The cause of the error
   *
   * @return A new parse response configured with error and the cause
   */
  public static <T> OccurrenceParseResult<T> error(Throwable cause) {
    return new OccurrenceParseResult<>(STATUS.ERROR, null, null, null, cause);
  }

  public static <T> OccurrenceParseResult<T> success(CONFIDENCE confidence, T payload, OccurrenceIssue issue) {
    return new OccurrenceParseResult<>(STATUS.SUCCESS, confidence, payload, null, null, new ArrayList<>(Collections.singletonList(issue)));
  }

  public static <T> OccurrenceParseResult<T> success(CONFIDENCE confidence, T payload, OccurrenceIssue ... issues) {
    return new OccurrenceParseResult<>(STATUS.SUCCESS, confidence, payload, null, null, new ArrayList<>(Arrays.asList(issues)));
  }

  public static <T> OccurrenceParseResult<T> success(CONFIDENCE confidence, T payload, Collection<OccurrenceIssue> issues) {
    return new OccurrenceParseResult<>(STATUS.SUCCESS, confidence, payload, null, null, issues);
  }

  public static <T> OccurrenceParseResult<T> fail(OccurrenceIssue issue) {
    return new OccurrenceParseResult<>(STATUS.FAIL, null, null, null, null, new ArrayList<>(Collections.singletonList(issue)));
  }

  public static <T> OccurrenceParseResult<T> fail(OccurrenceIssue ... issues) {
    return new OccurrenceParseResult<>(STATUS.FAIL, null, null, null, null, new ArrayList<>(Arrays.asList(issues)));
  }

  public static <T> OccurrenceParseResult<T> fail(Collection<OccurrenceIssue> issues) {
    return new OccurrenceParseResult<>(STATUS.FAIL, null, null, null, null, issues);
  }

  /**
   * This creates a ParseResult indicating a parse failure but it also has a payload. Depending on the context this
   * may provide additional information about the failure.
   *
   * @param payload the payload of the parse result
   * @param <T>    the generic type of the payload
   *
   * @return the new parse response which has a status of FAIL and an additional payload.
   */
  public static <T> OccurrenceParseResult<T> fail(T payload, Collection<OccurrenceIssue> issues) {
    return new OccurrenceParseResult<>(STATUS.FAIL, null, payload, null, null, issues);
  }

  public static <T> OccurrenceParseResult<T> fail(T payload, OccurrenceIssue ... issues) {
    return new OccurrenceParseResult<>(STATUS.FAIL, null, payload, null, null, new ArrayList<>(Arrays.asList(issues)));
  }

  /**
   * This creates a ParseResult indicating a parse failure but it also has a payload. Depending on the context this
   * may provide additional information about the failure.
   *
   * @param payload the payload of the parse result
   * @param <T>    the generic type of the payload
   *
   * @return the new parse response which has a status of FAIL and an additional payload.
   */
  public static <T> OccurrenceParseResult<T> fail(T payload, OccurrenceIssue issue) {
    return new OccurrenceParseResult<>(STATUS.FAIL, null, payload, null, null, new ArrayList<>(Collections.singletonList(issue)));
  }


  public Set<OccurrenceIssue> getIssues() {
    return issues;
  }

  public void addIssue(OccurrenceIssue issue) {
    Objects.requireNonNull(issue);
    issues.add(issue);
  }

}
