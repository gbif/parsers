package org.gbif.common.parsers.geospatial;

import org.gbif.api.vocabulary.OccurrenceValidationRule;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * Simple container class.
 */
public class LongPrecisionIssue {

  private final Long value;
  private final Long precision;
  private final Set<OccurrenceValidationRule> issues;

  public LongPrecisionIssue(Long value, Long precision, Set<OccurrenceValidationRule> issues) {
    this.value = value;
    this.precision = precision;
    this.issues = issues;
  }

  public LongPrecisionIssue(Long value, Long precision, OccurrenceValidationRule ... issue) {
    this(value, precision, Sets.newHashSet(issue));
  }

  public Long getValue() {
    return value;
  }

  public Long getPrecision() {
    return precision;
  }

  public Set<OccurrenceValidationRule> getIssues() {
    return issues;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("value", value)
      .add("issues", issues)
      .add("precision", precision)
      .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value, precision, issues);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof LongPrecisionIssue)) {
      return false;
    }

    LongPrecisionIssue that = (LongPrecisionIssue) object;
    return Objects.equal(this.value, that.value)
           && Objects.equal(this.issues, that.issues)
           && Objects.equal(this.precision, that.precision);
  }

}
