package org.devoware.reactive.property;

import static com.google.common.base.Preconditions.checkNotNull;

class FixedValueSource<V> implements ValueSource<V> {

  private final V value;
  
  FixedValueSource(V value) {
    checkNotNull(value, "value cannot be null");
    this.value = value;
  }

  @Override
  public V apply(PropertyContext<V> context) {
    return value;
  }

  
}
