package org.devoware.reactive.property;

public interface ModifierOrderingRule<V> {
  
  int insert(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier);

}
