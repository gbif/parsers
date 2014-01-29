package org.gbif.common.parsers.core;

import org.gbif.api.vocabulary.OccurrenceIssue;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * This represents the response of a generic parse operation.
 */
public class ParseResult<T> {

  // the result code of the parse operation
  public enum STATUS {
    SUCCESS, FAIL, ERROR
  }

  // the confidence of the result, applicable if status=SUCCESS
  public enum CONFIDENCE {
    DEFINITE, PROBABLE, POSSIBLE
  }

  // the details of the response
  protected STATUS status;
  protected CONFIDENCE confidence;
  protected T payload;
  protected Throwable error;
  private final Set<OccurrenceIssue> issues = EnumSet.noneOf(OccurrenceIssue.class);

  /**
   * @param <T1>       The generic type of the payload
   * @param confidence The confidence in the result
   * @param payload    The payload of the parse result
   *
   * @return The new ParseResult which has no error and status of SUCCESS
   */
  public static <T1> ParseResult<T1> success(CONFIDENCE confidence, T1 payload) {
    return new ParseResult<T1>(STATUS.SUCCESS, confidence, payload, null, null);
  }

  public static <T1> ParseResult<T1> success(CONFIDENCE confidence, T1 payload, OccurrenceIssue issue) {
    return new ParseResult<T1>(STATUS.SUCCESS, confidence, payload, null, Lists.newArrayList(issue));
  }

  public static <T1> ParseResult<T1> success(CONFIDENCE confidence, T1 payload, Collection<OccurrenceIssue> issues) {
    return new ParseResult<T1>(STATUS.SUCCESS, confidence, payload, null, issues);
  }
  /**
   * @return A new parse response with only the status set to FAIL
   */
  public static <T1> ParseResult<T1> fail() {
    return new ParseResult<T1>(STATUS.FAIL, null, null, null, null);
  }

  public static <T1> ParseResult<T1> fail(OccurrenceIssue issue) {
    return new ParseResult<T1>(STATUS.FAIL, null, null, null, Lists.newArrayList(issue));
  }

  public static <T1> ParseResult<T1> fail(Collection<OccurrenceIssue> issues) {
    return new ParseResult<T1>(STATUS.FAIL, null, null, null, issues);
  }

  /**
   * This creates a ParseResult indicating a parse failure but it also has a payload. Depending on the context this
   * may provide additional information about the failure.
   *
   * @param payload the payload of the parse result
   * @param <T1>    the generic type of the payload
   *
   * @return the new parse response which has a status of FAIL and an additional payload.
   */
  public static <T1> ParseResult<T1> fail(T1 payload, OccurrenceIssue issue) {
    return new ParseResult<T1>(STATUS.FAIL, null, payload, null, Lists.newArrayList(issue));
  }

  /**
   * @return A new parse response configured to indicate an error
   */
  public static <T1> ParseResult<T1> error() {
    return new ParseResult<T1>(STATUS.ERROR, null, null, null, null);
  }

  /**
   * @param cause The cause of the error
   *
   * @return A new parse response configured with error and the cause
   */
  public static <T1> ParseResult<T1> error(Throwable cause) {
    return new ParseResult<T1>(STATUS.ERROR, null, null, cause, null);
  }

  /**
   * Forces all fields to be provided
   *
   * @param status     The status of the response
   * @param confidence The confidence in the result
   * @param payload    The payload of the response
   * @param error      The error in the response
   * @param issues     Optional issues to report
   */
  public ParseResult(STATUS status, CONFIDENCE confidence, T payload, Throwable error, Collection<OccurrenceIssue> issues) {
    this.status = status;
    this.confidence = confidence;
    this.payload = payload;
    this.error = error;
    if (issues != null) {
      // add non nulls only
      Iterables.addAll(this.issues, Iterables.filter(issues, Predicates.notNull()));
    }
  }

  public STATUS getStatus() {
    return status;
  }

  public CONFIDENCE getConfidence() {
    return confidence;
  }

  public T getPayload() {
    return payload;
  }

  public Throwable getError() {
    return error;
  }

  public Set<OccurrenceIssue> getIssues() {
    return issues;
  }

  /**
   * Returns true if {@link #getStatus()} returns SUCCESS.
   *
   * @return true if the parse operation was successful.
   */
  public boolean isSuccessful() {
    return status == STATUS.SUCCESS;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("status", getStatus())
      .append("confidence", getConfidence())
      .append("payload", getPayload())
      .append("issues", getIssues())
      .append("error", getError()).toString();
  }
}
