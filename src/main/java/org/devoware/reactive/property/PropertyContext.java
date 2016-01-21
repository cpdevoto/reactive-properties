package org.devoware.reactive.property;

public interface PropertyContext<V> {
  public V get(Identifier<V> id);
}
