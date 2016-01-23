package org.devoware.reactive.property;

public interface Modifier<V> {
  
  public V onBoundValueChanged(PropertyContext context, V value);

}
