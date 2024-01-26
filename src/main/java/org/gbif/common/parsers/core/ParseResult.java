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

import java.util.List;

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
    DEFINITE,
    PROBABLE,
    POSSIBLE;

    public static CONFIDENCE lowerOf(CONFIDENCE c1, CONFIDENCE c2) {
      if (c1 == null) {
        return c2;
      }

      if (c2 == null) {
        return c1;
      }

      if (c1.compareTo(c2) > 0) {
        // Higher enum value is lower confidence
        return c1;
      } else {
        return c2;
      }
    }
  }

  // the details of the response
  protected final STATUS status;
  protected final CONFIDENCE confidence;
  protected final T payload;
  protected final List<T> alternativePayloads;
  protected final Throwable error;

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

  /**
   * @return A new parse response with only the status set to FAIL
   */
  public static <T1> ParseResult<T1> fail() {
    return new ParseResult<T1>(STATUS.FAIL, null, null, null, null);
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
    return new ParseResult<T1>(STATUS.ERROR, null, null, null, cause);
  }

  /**
   * Forces all fields to be provided
   *
   * @param status     The status of the response
   * @param confidence The confidence in the result
   * @param payload    The payload of the response
   * @param error      The error in the response
   */
  public ParseResult(STATUS status, CONFIDENCE confidence, T payload, List<T> alternativePayloads, Throwable error) {
    this.status = status;
    this.confidence = confidence;
    this.alternativePayloads = alternativePayloads;
    this.payload = payload;
    this.error = error;
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

  public List<T> getAlternativePayloads() {
    return alternativePayloads;
  }

  public Throwable getError() {
    return error;
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
      .append("alternativePayloads", getAlternativePayloads())
      .append("error", getError()).toString();
  }
}
