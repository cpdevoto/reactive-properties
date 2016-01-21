package org.devoware.reactive.property;

import java.util.function.Function;

interface ValueSource<V> extends Function<PropertyContext<V>, V> {

}
