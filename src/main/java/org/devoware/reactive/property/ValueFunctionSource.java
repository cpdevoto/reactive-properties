package org.devoware.reactive.property;

import static com.google.common.base.Preconditions.checkNotNull;

class ValueFunctionSource<V> implements ValueSource<V> {

  private final ValueFunction<V> function;
  
  ValueFunctionSource(ValueFunction<V> function) {
    checkNotNull(function, "function cannot be null");
    this.function = function;
  }

  @Override
  public V apply(PropertyContext<V> context) {
    return function.onBoundValueChanged(context);
  }

  
}
