package org.gbif.common.parsers.geospatial;

import org.gbif.api.vocabulary.OccurrenceIssue;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * Simple container class for integer based values with an accurracy (e.g. depth in meters).
 */
public class IntPrecisionIssue {

  private final Integer value;
  private final Integer precision;
  private final Set<OccurrenceIssue> issues;

  public IntPrecisionIssue(Integer value, Integer precision, Set<OccurrenceIssue> issues) {
    this.value = value;
    this.precision = precision;
    this.issues = issues;
  }

  public IntPrecisionIssue(Integer value, Integer precision, OccurrenceIssue... issue) {
    this(value, precision, Sets.newHashSet(issue));
  }

  public Integer getValue() {
    return value;
  }

  public Integer getPrecision() {
    return precision;
  }

  public Set<OccurrenceIssue> getIssues() {
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
    if (!(object instanceof IntPrecisionIssue)) {
      return false;
    }

    IntPrecisionIssue that = (IntPrecisionIssue) object;
    return Objects.equal(this.value, that.value)
           && Objects.equal(this.issues, that.issues)
           && Objects.equal(this.precision, that.precision);
  }

}
