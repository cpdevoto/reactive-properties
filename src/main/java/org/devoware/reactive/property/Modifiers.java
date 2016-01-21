package org.devoware.reactive.property;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class Modifiers<V> {
  
  private final Map<ModifierIdentifier, Integer> indecesByIdentifier = Maps.newHashMap();
  private final List<Modifier<V>> modifiers = Lists.newArrayList();
  
  public static <V> Modifiers<V> create() {
    return new Modifiers<>();
  }
  
  public static <V> Modifiers<V> create(Modifiers<V> modifiers) {
    return new Modifiers<>(modifiers);
  }

  private Modifiers() {}

  private Modifiers(Modifiers<V> modifiers) {
    this.modifiers.addAll(modifiers.modifiers);
    this.indecesByIdentifier.putAll(modifiers.indecesByIdentifier);
  }
  
  public void put(final ModifierIdentifier id, final Modifier<V> modifier) {
    put(id, modifier, size());
  }

  public void put(final ModifierIdentifier id, final Modifier<V> modifier, final int idx) {
    checkNotNull(id, "id cannot be null");
    checkNotNull(id, "modifier cannot be null");
    int tempIdx = checkIndexOnPut(idx);
    int oldIdx = -1;
    final int adjustedIdx;
    if (indecesByIdentifier.containsKey(id)) {
      oldIdx = indecesByIdentifier.get(id);
      if (oldIdx != -1) {
        modifiers.remove(oldIdx);
        indecesByIdentifier.remove(id);
        shift(oldIdx, -1);
        if (oldIdx < idx) {
          tempIdx -= 1;
        }
      }
      adjustedIdx = tempIdx;
    } else {
      adjustedIdx = idx;
    }
    modifiers.add(adjustedIdx, modifier);
    shift(adjustedIdx, 1);
    indecesByIdentifier.put(id, adjustedIdx);
    
  }

  public boolean remove(final ModifierIdentifier id) {
    final int idx = indexOf(id);
    if (idx == -1) {
      return false;
    }
    modifiers.remove(idx);
    indecesByIdentifier.remove(id);
    shift(idx, -1);
    return true;
  }
  
  public void remove(final int idx) {
    checkIndexOnGet(idx);
    ModifierIdentifier id = getIdentifier(idx);
    remove(id);
  }
  
  public int indexOf(final ModifierIdentifier id) {
    checkNotNull(id, "id cannot be null");
    int idx = -1;
    if (indecesByIdentifier.containsKey(id)) {
      idx = indecesByIdentifier.get(id);
    }
    return idx;
  }
  
  public boolean containsKey(ModifierIdentifier id) {
    return indecesByIdentifier.containsKey(id);
  }
  
  public Modifier<V> get(final int idx) {
     checkIndexOnGet(idx);
     return modifiers.get(idx);
  }
  
  public Modifier<V> get(final ModifierIdentifier id) {
    int idx = indexOf(id);
    if (idx == -1) {
      return null;
    }
    return modifiers.get(idx);
  }
  
  private List<Modifier<V>> values() {
    return Lists.newArrayList(modifiers);
  }
  
  public Set<ModifierIdentifier> keySet() {
    return indecesByIdentifier.keySet();
  }
  
  public V applyModifiers(PropertyContext<V> context, V value) {
    V adjustedValue = value;
    for (Modifier<V> wrapper : modifiers) {
      adjustedValue = wrapper.onBoundValueChanged(context, adjustedValue);
    }
    return adjustedValue;
  }

  
  public int size () {
    return modifiers.size(); 
  }

  private int checkIndexOnPut(final int idx) {
    return checkIndex(idx, idx < 0 || idx > size());
  }
  
  private int checkIndexOnGet(final int idx) {
    return checkIndex(idx, idx < 0 || idx >= size());
  }
  
  private int checkIndex(final int idx, final boolean condition) {
    if (condition) {
      throw new ArrayIndexOutOfBoundsException("idx must be between 0 and " + size());
    }
    return idx;
  }

  private ModifierIdentifier getIdentifier(int idx) {
    for (Entry<ModifierIdentifier, Integer> entry : indecesByIdentifier.entrySet()) {
      if (entry.getValue() == idx) {
        return entry.getKey();
      }
    }
    throw new AssertionError("Expected to find a ModifierIdentifier at the specified index");
  }

  private void shift(final int idx, final int increment) {
    final Map<ModifierIdentifier, Integer> shiftedModifiers = Maps.newHashMap();
    indecesByIdentifier.entrySet().forEach((entry) -> {
      if (entry.getValue() >= idx) {
        shiftedModifiers.put(entry.getKey(), entry.getValue() + increment);
      }
    });
    shiftedModifiers.entrySet().forEach((entry) -> {
      indecesByIdentifier.put(entry.getKey(), entry.getValue());
    });
  }
  
}
