package org.devoware.reactive.property;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

class BasicPropertyManager implements PropertyManager {
  private final Map<PropertyIdentifier<?>, BasicProperty<?>> properties = Maps.newConcurrentMap();
  private final Multimap<PropertyIdentifier<?>, PropertyIdentifier<?>> consumerBindings = LinkedHashMultimap.create();
  private final Multimap<PropertyIdentifier<?>, PropertyIdentifier<?>> producerBindings = LinkedHashMultimap.create();
  private final Multimap<PropertyIdentifier<?>, PropertyChangeListener<?>> listeners =
      LinkedListMultimap.create();

  @Override
  public <V> PropertyManager.Builder<V> create(PropertyIdentifier<V> id) {
    checkNotNull(id, "id cannot be null");
    BasicProperty<V> property = getBasicProperty(id);
    if (property != null) {
      return new Updater<V>(property);
    }
    return new Builder<>(id);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> Property<V> get(PropertyIdentifier<V> id) {
    checkNotNull(id, "id cannot be null");
    return (Property<V>) properties.get(id);
  }
  
  @Override
  public Set<PropertyIdentifier<?>> getConsumerBindings(Property<?> consumer) {
    return ImmutableSet.copyOf(consumerBindings.get(consumer.getId()));
  }
  
  @Override
  public Set<PropertyIdentifier<?>> getProducerBindings(Property<?> producer) {
    return ImmutableSet.copyOf(producerBindings.get(producer.getId()));
  }

  <V> PropertyContext<V> getPropertyContextFor(BasicProperty<V> consumer, boolean createBindings) {
    checkNotNull(consumer, "requestorProperty cannot be null");
    return new Context<>(consumer, createBindings);
  }

  <V> void addPropertyChangeListener(BasicProperty<V> property,
      PropertyChangeListener<V> listener) {
    checkNotNull(property, "property cannot be null");
    checkNotNull(listener, "listener cannot be null");
    listeners.put(property.getId(), listener);
  }

  <V> void removePropertyChangeListener(BasicProperty<V> property,
      PropertyChangeListener<V> listener) {
    checkNotNull(property, "property cannot be null");
    checkNotNull(listener, "listener cannot be null");
    listeners.remove(property.getId(), listener);
  }

  <V> void unbind(Property<V> consumer) {
    checkNotNull(consumer, "consumer cannot be null");
    consumerBindings.get(consumer.getId()).forEach((producer) -> {
      producerBindings.remove(producer, consumer.getId());
    });
    consumerBindings.removeAll(consumer.getId());
  }
  
  @SuppressWarnings("unchecked")
  <V> void firePropertyValueChange(Property<V> producer) {
    listeners.get(producer.getId()).forEach((listener) -> {
      PropertyChangeListener<V> typedListener = (PropertyChangeListener<V>) listener;
      typedListener.onValueChanged(producer.getId(), producer.get());;
    });

    producerBindings.get(producer.getId()).forEach((consumer) -> {
      getBasicProperty(consumer).onProducerPropertyValueChange();
    });
  }

  private <V> void register(BasicProperty<V> property) {
    this.properties.put(property.getId(), property);
  }
  
  @SuppressWarnings("unchecked")
  private <V> BasicProperty<V> getBasicProperty(PropertyIdentifier<V> id) {
    checkNotNull(id, "id cannot be null");
    return (BasicProperty<V>) properties.get(id);
  }

  
  private <V> void bind(PropertyIdentifier<V> consumer, PropertyIdentifier<V> producer) {
    checkNotNull(consumer, "consumer cannot be null");
    checkNotNull(producer, "producer cannot be null");
    checkForCycles(consumer, producer);
    consumerBindings.put(consumer, producer);
    producerBindings.put(producer, consumer);
  }
  
  private <V> V getValue(PropertyIdentifier<V> id) {
    checkNotNull(id, "id cannot be null");
    Property<V> property = get(id);
    if (property == null) {
      return id.getDefaultValue();
    }
    return property.get();
  }
  
  private void checkForCycles(PropertyIdentifier<?> consumer, PropertyIdentifier<?> producer) {
    if (producer == consumer) {
      throw new CyclicBindingException();
    }
    producerBindings.get(consumer).forEach((c) -> checkForCycles(c, producer));
  }
  
  private class Context <V> implements PropertyContext<V> {
    private final BasicProperty<V> consumer;
    private final boolean createBindings;
    
    private Context(BasicProperty<V> consumer, boolean createBindings) {
      checkNotNull(consumer, "consumer cannot be null");
      this.consumer = consumer;
      this.createBindings = createBindings;
    }

    public V get(PropertyIdentifier<V> id) {
      checkNotNull(id, "id cannot be null");
      if(consumer.getId().equals(id)) {
        throw new CyclicBindingException("A value function cannot reference itself");
      }
      if (createBindings && !properties.containsKey(id)) {
        create(id).withValue(id.getDefaultValue()).build();
      }
      if (createBindings && !consumerBindings.containsEntry(consumer.getId(), id)) {
        bind(consumer.getId(), id);
      }
      return getValue(id);
    }  
  }

  class Builder<V> implements PropertyManager.Builder<V> {
    private final PropertyIdentifier<V> identifier;
    private ValueSource<V> valueSource;
    private Optional<Validator<V>> validator = Optional.empty();
    private BuilderState state = BuilderState.OPEN;

    private Builder(PropertyIdentifier<V> identifier) {
      checkNotNull(identifier, "identifier cannot be null");
      this.identifier = identifier;
    }
    
    @Override
    public Builder<V> withValue(V value) {
      checkNotNull(value, "value cannot be null");
      checkState();
      this.valueSource = new LiteralValueSource<>(value);
      return this;
    }

    @Override
    public Builder<V> withValue(ValueFunction<V> function) {
      checkNotNull(function, "function cannot be null");
      checkState();
      this.valueSource = new ValueFunctionSource<>(function);
      return this;
    }

    @Override
    public Builder<V> withValidator(Validator<V> validator) {
      checkNotNull(validator, "validator cannot be null");
      checkState();
      this.validator = Optional.of(validator);
      return this;
    }
    
    PropertyIdentifier<V> getIdentifier() {
      return identifier;
    }
    
    BasicPropertyManager getPropertyManager() {
      return BasicPropertyManager.this;
    }
    
    ValueSource<V> getValueSource() {
      return valueSource;
    }

    Optional<Validator<V>> getValidator() {
      return validator;
    }

    private void checkState () {
      if (state == BuilderState.CLOSED) {
        throw new IllegalStateException("builder is closed");
      }
    }
    
    @Override
    public Property<V> build() {
      checkNotNull(valueSource, "must specify a value");
      BasicProperty<V> property = new BasicProperty<>(this); 
      register(property);
      state = BuilderState.CLOSED;
      return property;
    }
  }
  
  private class Updater<V> implements PropertyManager.Builder<V> {
    private final BasicProperty<V> property;
    private ValueSource<V> valueSource;
    private Optional<Validator<V>> validator = Optional.empty();
    private BuilderState state = BuilderState.OPEN;
    
    private Updater(BasicProperty<V> property) {
      checkNotNull(property);
      this.property = property;
    }
    
    @Override
    public Updater<V> withValue(V value) {
      checkNotNull(value, "value cannot be null");
      checkState();
      this.valueSource = new LiteralValueSource<>(value);
      return this;
    }

    @Override
    public Updater<V> withValue(ValueFunction<V> function) {
      checkNotNull(function, "function cannot be null");
      checkState();
      this.valueSource = new ValueFunctionSource<>(function);
      return this;
    }

    @Override
    public Updater<V> withValidator(Validator<V> validator) {
      checkNotNull(validator, "validator cannot be null");
      checkState();
      this.validator = Optional.of(validator);
      return this;
    }
    
    private void checkState () {
      if (state == BuilderState.CLOSED) {
        throw new IllegalStateException("builder is closed");
      }
    }

    @Override
    public Property<V> build() {
      checkNotNull(valueSource, "must specify a value");
      try {
        if (validator.isPresent()) {
          property.setValidator(validator.get());
        } else {
          property.removeValidator();
        }
      } catch (RuntimeException re) {
        // We can safely ignore this, 
        //since we will be changing the value below
      }
      property.set(valueSource);
      state = BuilderState.CLOSED;
      return property;
    }
  }
  
  private static enum BuilderState {
    OPEN,
    CLOSED
  }


}
