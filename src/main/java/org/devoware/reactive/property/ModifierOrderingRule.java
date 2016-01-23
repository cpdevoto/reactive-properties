package org.devoware.reactive.property;

public interface ModifierOrderingRule<V> {
  
  public void insert(Modifiers<V> modifiers, Identifier id, Modifier<V> modifier);

}
