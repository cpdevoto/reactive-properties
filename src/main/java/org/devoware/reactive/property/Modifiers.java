package org.devoware.reactive.property;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class Modifiers<V> implements Iterable<Modifier<V>> {
  private Map<Identifier, LinkedList<Modifier<V>>> listsById = Maps.newHashMap();
  private Map<Identifier, Modifier<V>> modifiersById = Maps.newHashMap();
  private LinkedList<Modifier<V>> firstModifiers = Lists.newLinkedList();
  private LinkedList<Modifier<V>> modifiers = Lists.newLinkedList();
  private LinkedList<Modifier<V>> lastModifiers = Lists.newLinkedList();
  
  static <V> Modifiers<V> create() {
    return new Modifiers<>();
  }
  
  static <V> Modifiers<V> create(Modifiers<V> modifiers) {
    checkNotNull(modifiers, "modifiers cannot be null");
    return new Modifiers<>(modifiers);
  }

  private Modifiers() {}

  private Modifiers(Modifiers<V> modifiers) {
    this.listsById.putAll(modifiers.listsById);
    this.modifiersById.putAll(modifiersById);
    this.firstModifiers.addAll(modifiers.firstModifiers);
    this.modifiers.addAll(modifiers.modifiers);
    this.lastModifiers.addAll(modifiers.lastModifiers);
  }
  
  Set<Identifier> keySet() {
    return listsById.keySet();
  }
  
  boolean containsKey(Identifier id) {
    check(id);
    return listsById.containsKey(id);
  }
  
  int size() {
    return listsById.size();
  }
  
  boolean isEmpty() {
    return listsById.isEmpty();
  }
  
  Modifier<V> get(Identifier id) {
    check(id);
    return modifiersById.get(id);
  }
  
  Modifiers<V> applyFirst(Identifier id, Modifier<V> modifier) {
    check(id).check(modifier);
    removeIfPresent(id, modifier);
    index(firstModifiers, id, modifier);
    firstModifiers.addFirst(modifier);
    return this;
  }
  
  Modifiers<V> apply(Identifier id, Modifier<V> modifier) {
    check(id).check(modifier);
    removeIfPresent(id, modifier);
    index(modifiers, id, modifier);
    modifiers.add(modifier);
    return this;
  }
  
  Modifiers<V> applyLast(Identifier id, Modifier<V> modifier) {
    check(id).check(modifier);
    removeIfPresent(id, modifier);
    index(lastModifiers, id, modifier);
    lastModifiers.addLast(modifier);
    return this;
  }
  
  Modifiers<V> remove(Identifier id) {
    check(id);
    removeIfPresent(id);
    return this;
  }
  
  V applyModifiers(PropertyContext<V> context, V value) {
    checkNotNull(context, "context cannot be null");
    checkNotNull(value, "value cannot be null");
    V adjustedValue = value;
    for (Modifier<V> modifier: this) {
      adjustedValue = modifier.onBoundValueChanged(context, adjustedValue);
    }
    return adjustedValue;
  }
  
  @Override
  public Iterator<Modifier<V>> iterator() {
    return Iterators.concat(
        firstModifiers.iterator(),
        modifiers.iterator(),
        lastModifiers.iterator());
  }
  
  private void index(LinkedList<Modifier<V>> list, Identifier id, Modifier<V> modifier) {
    listsById.put(id, list);
    modifiersById.put(id, modifier);
  }
  
  private void removeIfPresent(Identifier id) {
    LinkedList<Modifier<V>> list = listsById.get(id);
    if (list == null) {
      return;
    }
    Modifier<V> modifier = modifiersById.get(id);
    remove(list, id, modifier);
  }

  private void removeIfPresent(Identifier id, Modifier<V> modifier) {
    LinkedList<Modifier<V>> list = listsById.get(id);
    if (list == null) {
      return;
    }
    remove(list, id, modifier);
  }

  private void remove(LinkedList<Modifier<V>> list, Identifier id, Modifier<V> modifier) {
    list.remove(modifier);
    listsById.remove(id);
    modifiersById.remove(id);
  }

  private Modifiers<V> check(Identifier id) {
    checkNotNull(id, "id cannot be null");
    return this;
  }
  
  private Modifiers<V> check(Modifier<V> modifier) {
    checkNotNull(modifier, "modifier cannot be null");
    return this;
  }

}
