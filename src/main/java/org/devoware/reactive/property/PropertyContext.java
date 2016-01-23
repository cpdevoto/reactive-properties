package org.devoware.reactive.property;

public interface PropertyContext {
  public <V> V get(PropertyIdentifier<V> id);
}
