package org.devoware.reactive.property;

public interface PropertyChangeListener<V> {
  
  public void onValueChanged (PropertyIdentifier<V> sourceId, V value);

}
