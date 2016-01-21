package org.devoware.reactive.property;

import java.util.Iterator;

public interface Property<V> extends Identifiable<V> {

  public void set(V value);
  
  public void set(ValueFunction<V> function);
  
  public V get();
  
  public void addPropertyChangeListener(PropertyChangeListener<V> listener);
  
  public void removePropertyChangeListener(PropertyChangeListener<V> listener);
 
  public void setValidator(Validator<V> validator);

  public void removeValidator();

  public ModifierIdentifier addModifier(Modifier<V> modifier);

  public ModifierIdentifier addModifier(Modifier<V> modifier, ModifierOrderingRule<V> rule);

  public void addModifier(ModifierIdentifier id, Modifier<V> modifier);

  public void addModifier(ModifierIdentifier id, Modifier<V> modifier, ModifierOrderingRule<V> rule);

  public void removeModifier(ModifierIdentifier id);

  public Iterator<ModifierIdentifier> getModifierIdentifiers();
  
}
