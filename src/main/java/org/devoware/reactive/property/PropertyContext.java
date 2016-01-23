package org.devoware.reactive.property;

public interface PropertyContext<V> {
  public V get(PropertyIdentifier<V> id);
}
