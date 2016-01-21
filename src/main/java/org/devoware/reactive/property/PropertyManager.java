package org.devoware.reactive.property;

import java.util.Set;

public interface PropertyManager {

  public <V> Builder<V> create(Identifier<V> id);

  public <V> Property<V> get(Identifier<V> id);
  
  public Set<Identifier<?>> getConsumerBindings(Property<?> consumer);
 
  public Set<Identifier<?>> getProducerBindings(Property<?> producer);
  
  public interface Builder<V> {
     
    public Builder<V> withValue(V value);

    public Builder<V> withValue(ValueFunction<V> function);

    public Builder<V> withValidator(Validator<V> validator);

    public Property<V> build();

  }

}
