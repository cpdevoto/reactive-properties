package org.devoware.reactive.property;

public interface PropertyIdentifier<V> {

  public V getDefaultValue();
  
  public Class<V> getType();

}
