package org.devoware.reactive.property;

public interface Identifier<V> {

  public V getDefaultValue();
  
  public Class<V> getType();

}
