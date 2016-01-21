package org.devoware.reactive.property;

public interface ValueFunction<V> {
  public V onBoundValueChanged(PropertyContext<V> context);
}
