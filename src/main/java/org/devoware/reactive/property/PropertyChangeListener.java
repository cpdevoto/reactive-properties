package org.devoware.reactive.property;

public interface PropertyChangeListener<V> {
  
  public void onValueChanged (Identifier<V> sourceId, V value);

}
