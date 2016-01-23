package org.devoware.reactive.property;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.devoware.reactive.property.ModifierOrderingRules.apply;

import java.util.Iterator;
import java.util.Optional;

import org.devoware.reactive.property.BasicPropertyManager.Builder;

class BasicProperty<V> implements Property<V> {

  private final BasicPropertyManager manager;
  private final PropertyIdentifier<V> identifier;
  private final Modifiers<V> modifiers = Modifiers.create();
  private ValueSource<V> valueSource;
  private V cachedValue;
  private Optional<Validator<V>> validator;

  public BasicProperty(Builder<V> builder) {
    this.manager = builder.getPropertyManager();
    this.identifier = builder.getIdentifier();
    this.validator = builder.getValidator();
    this.valueSource = builder.getValueSource();
    validate(this.valueSource, false);
    this.cachedValue = get(this.valueSource);
    validate(this.valueSource, true);
  }

  @Override
  public PropertyIdentifier<V> getId() {
    return identifier;
  }

  @Override
  public Property<V> set(V value) {
    set(new LiteralValueSource<>(value));
    return this;
  }

  @Override
  public Property<V> set(ValueFunction<V> function) {
    set(new ValueFunctionSource<>(function));
    return this;
  }

  @Override
  public V get() {
    return this.cachedValue;
  }

  @Override
  public Property<V> addPropertyChangeListener(PropertyChangeListener<V> listener) {
    manager.addPropertyChangeListener(this, listener);
    return this;
  }

  @Override
  public Property<V> removePropertyChangeListener(PropertyChangeListener<V> listener) {
    manager.removePropertyChangeListener(this, listener);
    return this;
  }

  @Override
  public Property<V> setValidator(Validator<V> validator) {
    checkNotNull(validator, "validator cannot be null");
    Optional<Validator<V>> v = Optional.of(validator);
    validate(v, this.valueSource, this.modifiers, false);
    this.validator = v;
    validate(v, this.valueSource, this.modifiers, true);
    return this;
  }

  @Override
  public Property<V> removeValidator() {
    this.validator = Optional.empty();
    return this;
  }

  @Override
  public Identifier addModifier(Modifier<V> modifier) {
    return addModifier(modifier, apply());
  }
  
  @Override
  public Identifier addModifier(Modifier<V> modifier, ModifierOrderingRule<V> rule) {
    checkNotNull(modifier, "modifier cannot be null");
    Identifier id = new Identifier () {};
    addModifier(id, modifier, rule);
    return id;
  }

  @Override
  public Property<V> addModifier(Identifier id, Modifier<V> modifier) {
    addModifier(id, modifier, apply());
    return this;
  }
  
  @Override
  public Property<V> addModifier(Identifier id, Modifier<V> modifier, ModifierOrderingRule<V> rule) {
    checkNotNull(id, "id cannot be null");
    checkNotNull(modifier, "modifier cannot be null");
    checkNotNull(rule, "rule cannot be null");
    Modifiers<V> modifiers = Modifiers.create(this.modifiers);
    rule.insert(modifiers, id, modifier);
    validate(this.valueSource, modifiers,false);
    V oldValue = get();
    rule.insert(this.modifiers, id, modifier);
    this.cachedValue = get(this.valueSource);
    validate(this.valueSource, modifiers,true);
    if (!oldValue.equals(this.cachedValue)) {
      manager.firePropertyValueChange(this);
    }
    return this;
  }

  @Override
  public Property<V> removeModifier(Identifier id) {
    checkNotNull(id, "id cannot be null");
    if (!modifiers.containsKey(id)) {
      return this;
    }
    Modifiers<V> modifiers = Modifiers.create(this.modifiers); 
    modifiers.remove(id);
    validate(this.valueSource, modifiers, false);
    V oldValue = get();
    this.modifiers.remove(id);
    this.cachedValue = get(this.valueSource);
    validate(this.valueSource, modifiers, false);
    if (!oldValue.equals(this.cachedValue)) {
      manager.firePropertyValueChange(this);
    }
    return this;
  }

  @Override
  public Iterator<Identifier> getModifierIdentifiers() {
    return this.modifiers.keySet().iterator();
  }
  
  void set(ValueSource<V> source) {
    checkNotNull(source, "source cannot be null");
    validate(source, false);
    V oldValue = get();
    manager.unbind(this);
    this.cachedValue = get(source);
    this.valueSource = source;
    validate(source, true);
    if (!oldValue.equals(this.cachedValue)) {
      manager.firePropertyValueChange(this);
    }
  }

  void onProducerPropertyValueChange() {
    validate(this.valueSource, false);    
    V oldValue = get();
    this.cachedValue = get(this.valueSource);
    validate(this.valueSource, true);    
    if (!oldValue.equals(this.cachedValue)) {
      manager.firePropertyValueChange(this);
    }
  }
  
  private V get(ValueSource<V> source) {
    return get(source, true);
  }
  
  private V get(ValueSource<V> source, boolean createBindings) {
    return get(source, createBindings, this.modifiers);
  }

  private V get(ValueSource<V> source, boolean createBindings, Modifiers<V> modifiers) {
    PropertyContext context = manager.getPropertyContextFor(this, createBindings);
    V value = source.apply(context);
    value = modifiers.applyModifiers(context, value);
    return value;
  }
  
  private void validate(ValueSource<V> source, boolean createBindings) {
    validate(source, this.modifiers, createBindings);
  }

  private void validate(ValueSource<V> source, Modifiers<V> modifiers, boolean createBindings) {
    validate(this.validator, source, modifiers, createBindings);
  }

  private void validate(Optional<Validator<V>> validator, ValueSource<V> source, Modifiers<V> modifiers, boolean createBindings) {
    validator.ifPresent((v) -> {
      PropertyContext context = manager.getPropertyContextFor(this, createBindings);
      V value = get(source, false);
      v.validate(context, value);
    });
  }
  

}
