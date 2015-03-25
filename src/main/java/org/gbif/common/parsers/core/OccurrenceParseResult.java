package org.gbif.common.parsers.core;

import org.gbif.api.vocabulary.OccurrenceIssue;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 *
 */
public class OccurrenceParseResult<T> extends ParseResult<T> {
  private final Set<OccurrenceIssue> issues = EnumSet.noneOf(OccurrenceIssue.class);

  public OccurrenceParseResult(STATUS status, CONFIDENCE confidence, T payload, Throwable error, Collection<OccurrenceIssue> issues) {
    super(status, confidence, payload, error);
    if (issues != null) {
      // add non nulls only
      Iterables.addAll(this.issues, Iterables.filter(issues, Predicates.notNull()));
    }
  }

  public OccurrenceParseResult(STATUS status, CONFIDENCE confidence, T payload, Throwable error) {
    super(status, confidence, payload, error);
  }

  public OccurrenceParseResult(ParseResult<T> result) {
    super(result.getStatus(), result.getConfidence(), result.getPayload(), result.getError());
  }

  /**
   * @param <T>       The generic type of the payload
   * @param confidence The confidence in the result
   * @param payload    The payload of the parse result
   *
   * @return The new ParseResult which has no error and status of SUCCESS
   */
  public static <T> OccurrenceParseResult<T> success(CONFIDENCE confidence, T payload) {
    return new OccurrenceParseResult<T>(STATUS.SUCCESS, confidence, payload, null);
  }

  /**
   * @return A new parse response with only the status set to FAIL
   */
  public static <T> OccurrenceParseResult<T> fail() {
    return new OccurrenceParseResult<T>(STATUS.FAIL, null, null, null);
  }

  /**
   * @return A new parse response configured to indicate an error
   */
  public static <T> OccurrenceParseResult<T> error() {
    return new OccurrenceParseResult<T>(STATUS.ERROR, null, null, null);
  }

  /**
   * @param cause The cause of the error
   *
   * @return A new parse response configured with error and the cause
   */
  public static <T> OccurrenceParseResult<T> error(Throwable cause) {
    return new OccurrenceParseResult<T>(STATUS.ERROR, null, null, cause);
  }

  public static <T> OccurrenceParseResult<T> success(CONFIDENCE confidence, T payload, OccurrenceIssue issue) {
    return new OccurrenceParseResult<T>(STATUS.SUCCESS, confidence, payload, null, Lists.newArrayList(issue));
  }

  public static <T> OccurrenceParseResult<T> success(CONFIDENCE confidence, T payload, OccurrenceIssue ... issues) {
    return new OccurrenceParseResult<T>(STATUS.SUCCESS, confidence, payload, null, Lists.newArrayList(issues));
  }

  public static <T> OccurrenceParseResult<T> success(CONFIDENCE confidence, T payload, Collection<OccurrenceIssue> issues) {
    return new OccurrenceParseResult<T>(STATUS.SUCCESS, confidence, payload, null, issues);
  }

  public static <T> OccurrenceParseResult<T> fail(OccurrenceIssue issue) {
    return new OccurrenceParseResult<T>(STATUS.FAIL, null, null, null, Lists.newArrayList(issue));
  }

  public static <T> OccurrenceParseResult<T> fail(OccurrenceIssue ... issues) {
    return new OccurrenceParseResult<T>(STATUS.FAIL, null, null, null, Lists.newArrayList(issues));
  }

  public static <T> OccurrenceParseResult<T> fail(Collection<OccurrenceIssue> issues) {
    return new OccurrenceParseResult<T>(STATUS.FAIL, null, null, null, issues);
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
    return new OccurrenceParseResult<T>(STATUS.FAIL, null, payload, null, issues);
  }

  public static <T> OccurrenceParseResult<T> fail(T payload, OccurrenceIssue ... issues) {
    return new OccurrenceParseResult<T>(STATUS.FAIL, null, payload, null, Lists.newArrayList(issues));
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
    return new OccurrenceParseResult<T>(STATUS.FAIL, null, payload, null, Lists.newArrayList(issue));
  }


  public Set<OccurrenceIssue> getIssues() {
    return issues;
  }

  public void addIssue(OccurrenceIssue issue) {
    Preconditions.checkNotNull(issue);
    issues.add(issue);
  }

}
