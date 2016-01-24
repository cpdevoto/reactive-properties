package org.devoware.reactive.property;

public interface PropertyIdentifier<V> extends TypedIdentifier<V> {

  public V getDefaultValue();
  
  public Class<V> getType();

}
