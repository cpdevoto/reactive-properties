package org.devoware.reactive.property;

public interface Validator<V> {

  public void validate(PropertyContext context, V value);

}
