package org.devoware.reactive.property;

import java.util.Iterator;

public interface Property<V> extends Identifiable<V> {

  public Property<V> set(V value);
  
  public Property<V> set(ValueFunction<V> function);
  
  public V get();
  
  public Property<V> addPropertyChangeListener(PropertyChangeListener<V> listener);
  
  public Property<V> removePropertyChangeListener(PropertyChangeListener<V> listener);
 
  public Property<V> setValidator(Validator<V> validator);

  public Property<V> removeValidator();

  public ModifierIdentifier addModifier(Modifier<V> modifier);

  public ModifierIdentifier addModifier(Modifier<V> modifier, ModifierOrderingRule<V> rule);

  public Property<V> addModifier(ModifierIdentifier id, Modifier<V> modifier);

  public Property<V> addModifier(ModifierIdentifier id, Modifier<V> modifier, ModifierOrderingRule<V> rule);

  public Property<V> removeModifier(ModifierIdentifier id);

  public Iterator<ModifierIdentifier> getModifierIdentifiers();
  
}
