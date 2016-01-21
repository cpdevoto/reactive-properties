package org.devoware.reactive.property;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.devoware.reactive.property.BasicPropertyManager.Builder;

import com.google.common.collect.Lists;

class BasicProperty<V> implements Property<V> {

  private final BasicPropertyManager manager;
  private final Identifier<V> identifier;
  private final Modifiers<V> modifiers = Modifiers.create();
  private ValueSource<V> valueSource;
  private V cachedValue;
  private Optional<Validator<V>> validator;

  public BasicProperty(Builder<V> builder) {
    this.manager = builder.getPropertyManager();
    this.identifier = builder.getIdentifier();
    this.validator = builder.getValidator();
    this.valueSource = builder.getValueSource();
    validate(this.valueSource);
    this.cachedValue = get(this.valueSource);
  }

  @Override
  public Identifier<V> getId() {
    return identifier;
  }

  @Override
  public void set(V value) {
    set(new LiteralValueSource<>(value));
    
  }

  @Override
  public void set(ValueFunction<V> function) {
    set(new ValueFunctionSource<>(function));
  }

  @Override
  public V get() {
    return this.cachedValue;
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener<V> listener) {
    manager.addPropertyChangeListener(this, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener<V> listener) {
    manager.removePropertyChangeListener(this, listener);
  }

  @Override
  public void setValidator(Validator<V> validator) {
    checkNotNull(validator, "validator cannot be null");
    Optional<Validator<V>> v = Optional.of(validator);
    validate(v, this.valueSource, this.modifiers.values());
    this.validator = v;
  }

  @Override
  public void removeValidator() {
    this.validator = Optional.empty();
  }

  @Override
  public ModifierIdentifier addModifier(Modifier<V> modifier) {
    return addModifier(modifier, ModifierOrderingRules.last());
  }
  
  @Override
  public ModifierIdentifier addModifier(Modifier<V> modifier, ModifierOrderingRule<V> rule) {
    checkNotNull(modifier, "modifier cannot be null");
    ModifierIdentifier id = new ModifierIdentifier () {};
    addModifier(id, modifier, rule);
    return id;
  }

  @Override
  public void addModifier(ModifierIdentifier id, Modifier<V> modifier) {
    addModifier(id, modifier, ModifierOrderingRules.last());
  }
  
  @Override
  public void addModifier(ModifierIdentifier id, Modifier<V> modifier, ModifierOrderingRule<V> rule) {
    checkNotNull(id, "id cannot be null");
    checkNotNull(modifier, "modifier cannot be null");
    checkNotNull(rule, "rule cannot be null");
    Modifiers<V> modifiers = Modifiers.create(this.modifiers);
    rule.insert(modifiers, id, modifier);
    validate(this.valueSource, modifiers.values());
    V oldValue = get();
    rule.insert(this.modifiers, id, modifier);
    this.cachedValue = get(this.valueSource);
    if (!oldValue.equals(this.cachedValue)) {
      manager.firePropertyValueChange(this);
    }
   }

  @Override
  public void removeModifier(ModifierIdentifier id) {
    checkNotNull(id, "id cannot be null");
    if (!modifiers.containsKey(id)) {
      return;
    }
    Modifiers<V> modifiers = Modifiers.create(this.modifiers); 
    modifiers.remove(id);
    validate(this.valueSource, modifiers.values());
    V oldValue = get();
    this.modifiers.remove(id);
    this.cachedValue = get(this.valueSource);
    if (!oldValue.equals(this.cachedValue)) {
      manager.firePropertyValueChange(this);
    }
  }

  @Override
  public Iterator<ModifierIdentifier> getModifierIdentifiers() {
    return this.modifiers.keySet().iterator();
  }
  
  void set(ValueSource<V> source) {
    checkNotNull(source, "source cannot be null");
    validate(source);
    V oldValue = get();
    manager.unbind(this);
    this.cachedValue = get(source);
    this.valueSource = source;
    if (!oldValue.equals(this.cachedValue)) {
      manager.firePropertyValueChange(this);
    }
  }

  void onProducerPropertyValueChange() {
    validate(this.valueSource);    
    V oldValue = get();
    this.cachedValue = get(this.valueSource);
    if (!oldValue.equals(this.cachedValue)) {
      manager.firePropertyValueChange(this);
    }
  }
  
  private V get(ValueSource<V> source) {
    return get(source, true);
  }
  
  private V get(ValueSource<V> source, boolean createBindings) {
    return get(source, createBindings, Lists.newLinkedList(this.modifiers.values()));
  }

  private V get(ValueSource<V> source, boolean createBindings, List<Modifier<V>> modifiers) {
    PropertyContext<V> context = manager.getPropertyContextFor(this, createBindings);
    V value = source.apply(context);
    value = applyModifiers(context, value, modifiers);
    return value;
  }
  
  private void validate(ValueSource<V> source) {
    validate(source, this.modifiers.values());
  }

  private void validate(ValueSource<V> source, List<Modifier<V>> modifiers) {
    validate(this.validator, source, modifiers);
  }

  private void validate(Optional<Validator<V>> validator, ValueSource<V> source, List<Modifier<V>> modifiers) {
    if (validator.isPresent()) {
      V value = get(source, false);
      validator.get().validate(value);
    }
  }
  
  private V applyModifiers(PropertyContext<V> context, V value, List<Modifier<V>> modifiers2) {
    V adjustedValue = value;
    for (Modifier<V> wrapper : modifiers.values()) {
      adjustedValue = wrapper.onBoundValueChanged(context, adjustedValue);
    }
    return adjustedValue;
  }

}
