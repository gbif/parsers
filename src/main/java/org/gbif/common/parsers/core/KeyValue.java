package org.gbif.common.parsers.core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Simple container object.
 */
public class KeyValue<K, V> {

  private final K key;
  private final V value;

  public KeyValue(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof KeyValue) {
      KeyValue t = (KeyValue) obj;
      return new EqualsBuilder().append(key, t.getKey()).append(value, t.getValue()).isEquals();
    }
    return false;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(key).append(value).toString();
  }


}
