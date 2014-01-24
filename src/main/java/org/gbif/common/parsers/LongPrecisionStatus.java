package org.gbif.common.parsers;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Simple container class.
 */
public class LongPrecisionStatus {

  private final Long value;
  private final Long precision;
  private final int status;

  public LongPrecisionStatus(Long value, Long precision, int status) {
    this.value = value;
    this.precision = precision;
    this.status = status;
  }

  public Long getValue() {
    return value;
  }

  public Long getPrecision() {
    return precision;
  }

  public int getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("value", value).append("status", status)
      .append("precision", precision).toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(value).append(precision).append(status).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LongPrecisionStatus) {
      return new EqualsBuilder().append(value, ((LongPrecisionStatus) obj).getValue())
        .append(precision, ((LongPrecisionStatus) obj).getPrecision())
        .append(status, ((LongPrecisionStatus) obj).getStatus()).isEquals();
    } else {
      return false;
    }
  }
}
