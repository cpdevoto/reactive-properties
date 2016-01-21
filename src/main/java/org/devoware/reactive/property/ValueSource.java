package org.devoware.reactive.property;

import java.util.function.Function;

public interface ValueSource<V> extends Function<PropertyContext<V>, V> {

}
