package org.devoware.reactive.property;

import static com.google.common.base.Preconditions.checkNotNull;

public class ModifierOrderingRules {

  public static <V> ModifierOrderingRule<V> applyFirst() {
    return new ApplyFirst<>();
  }
  
  public static <V> ModifierOrderingRule<V> apply() {
    return new Apply<>();
  }
  
  public static <V> ModifierOrderingRule<V> applyLast() {
    return new ApplyLast<>();
  }
  
  private static <V> void checkArguments(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
    checkNotNull(modifiers, "modifiers cannot be null");
    checkNotNull(id, "id cannot be null");
    checkNotNull(modifier, "modifier cannot be null");
  }
  
  private ModifierOrderingRules() {}
  
  private static class ApplyFirst<V> implements ModifierOrderingRule<V> {

    @Override
    public void insert(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
      checkArguments(modifiers, id, modifier);
      modifiers.applyFirst(id, modifier);
    }
  }

  private static class Apply<V> implements ModifierOrderingRule<V> {

    @Override
    public void insert(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
      checkArguments(modifiers, id, modifier);
      modifiers.apply(id, modifier);
    }
  }

  private static class ApplyLast<V> implements ModifierOrderingRule<V> {

    @Override
    public void insert(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
      checkArguments(modifiers, id, modifier);
      modifiers.applyLast(id, modifier);
    }
  }
}
