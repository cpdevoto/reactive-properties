package org.devoware.reactive.property;

import java.util.Set;

public interface PropertyManager {

  public <V> Builder<V> create(PropertyIdentifier<V> id);

  public <V> PropertyManager remove(PropertyIdentifier<V> id);

  public <V> Property<V> get(PropertyIdentifier<V> id);
  
  public Set<PropertyIdentifier<?>> getConsumerBindings(Property<?> consumer);
 
  public Set<PropertyIdentifier<?>> getProducerBindings(Property<?> producer);
  
  public interface Builder<V> {
     
    public Builder<V> withValue(V value);

    public Builder<V> withValue(ValueFunction<V> function);

    public Builder<V> withValidator(Validator<V> validator);

    public Property<V> build();

  }

}
