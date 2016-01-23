package org.devoware.reactive.property;

import java.util.Iterator;

public interface Property<V> {

  public PropertyIdentifier<V> getId();

  public Property<V> set(V value);
  
  public Property<V> set(ValueFunction<V> function);
  
  public V get();
  
  public Property<V> addPropertyChangeListener(PropertyChangeListener<V> listener);
  
  public Property<V> removePropertyChangeListener(PropertyChangeListener<V> listener);
 
  public Property<V> setValidator(Validator<V> validator);

  public Property<V> removeValidator();

  public Identifier addModifier(Modifier<V> modifier);

  public Identifier addModifier(Modifier<V> modifier, ModifierOrderingRule<V> rule);

  public Property<V> addModifier(Identifier id, Modifier<V> modifier);

  public Property<V> addModifier(Identifier id, Modifier<V> modifier, ModifierOrderingRule<V> rule);

  public Property<V> removeModifier(Identifier id);

  public Iterator<Identifier> getModifierIdentifiers();
  
}
