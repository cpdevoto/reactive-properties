package org.devoware.reactive.property;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.Lists;

public class ModifierOrderingRules {

  public static <V> ModifierOrderingRule<V> first() {
    return new First<>();
  }
  
  public static <V> ModifierOrderingRule<V> last() {
    return new Last<>();
  }
  
  public static <V> ModifierOrderingRule<V> atIndex(int idx) {
    return new AtIndex<>(idx);
  }
  
  public static <V> ModifierOrderingRule<V> before(ModifierIdentifier id, ModifierIdentifier ... ids) {
    if (ids.length == 0) {
      return new Before<>(id);
    }
    return new BeforeList<>(Lists.asList(id, ids));
  }
  
  public static <V> ModifierOrderingRule<V> before(List<ModifierIdentifier> ids) {
    return new BeforeList<>(ids);
  }
 
  public static <V> ModifierOrderingRule<V> after(ModifierIdentifier id, ModifierIdentifier ... ids) {
    if (ids.length == 0) {
      return new After<>(id);
    }
    return new AfterList<>(Lists.asList(id, ids));
  }

  public static <V> ModifierOrderingRule<V> after(List<ModifierIdentifier> ids) {
    return new AfterList<>(ids);
  }

  private static <V> void checkArguments(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
    checkNotNull(modifiers, "modifiers cannot be null");
    checkNotNull(id, "id cannot be null");
    checkNotNull(modifier, "modifier cannot be null");
  }
  
  private ModifierOrderingRules() {}
  
  private static class First<V> implements ModifierOrderingRule<V> {

    @Override
    public int insert(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
      checkArguments(modifiers, id, modifier);
      modifiers.put(id, modifier, 0);
      return 0;
    }
  }

  private static class Last<V> implements ModifierOrderingRule<V> {

    @Override
    public int insert(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
      checkArguments(modifiers, id, modifier);
      int idx = modifiers.size();
      modifiers.put(id, modifier);
      return idx;
    }
  }

  private static class AtIndex<V> implements ModifierOrderingRule<V> {
    private final int idx;
    
    private AtIndex(int idx) {
      checkArgument(idx >= 0, "idx cannot be negative");
      this.idx = idx;
    }
    
    @Override
    public int insert(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
      checkArguments(modifiers, id, modifier);
      modifiers.put(id, modifier, idx);
      return idx;
    }
  }
  
  private static class Before<V> implements ModifierOrderingRule<V> {
    private final ModifierIdentifier id;
    
    private Before(ModifierIdentifier id) {
      checkNotNull(id, "id cannot be null");
      this.id = id;
    }
    
    @Override
    public int insert(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
      checkArguments(modifiers, id, modifier);
      int idx = modifiers.indexOf(this.id);
      if (idx == -1) {
        idx = 0;
      }
      modifiers.put(id, modifier, idx);
      return idx;
    }
  }
  
  private static class BeforeList<V> implements ModifierOrderingRule<V> {
    private final List<ModifierIdentifier> ids = Lists.newLinkedList();
    
    private BeforeList(List<ModifierIdentifier> ids) {
      checkNotNull(ids, "ids cannot be null");
      checkArgument(!ids.isEmpty(), "At least one id must be set");
      this.ids.addAll(ids);
    }
    
    @Override
    public int insert(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
      checkArguments(modifiers, id, modifier);
      int idx = getIndex(modifiers);
      modifiers.put(id, modifier, idx);
      return idx;
    }

    private int getIndex(Modifiers<V> modifiers) {
      int result = Integer.MAX_VALUE;
      for (ModifierIdentifier id : ids) {
        int idx = modifiers.indexOf(id);
        if (idx != -1 && idx < result) {
          result = idx;
        }
      }
      if (result == Integer.MAX_VALUE) {
        result = 0;
      }
      return result;
    }
  }

  private static class After<V> implements ModifierOrderingRule<V> {
    private final ModifierIdentifier id;
    
    private After(ModifierIdentifier id) {
      checkNotNull(id, "id cannot be null");
      this.id = id;
    }
    
    @Override
    public int insert(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
      checkArguments(modifiers, id, modifier);
      int idx = modifiers.indexOf(this.id);
      if (idx == -1) {
        idx = modifiers.size();
      } else {
        idx += 1;
      }
      modifiers.put(id, modifier, idx);
      return idx;
    }
  }
  
  private static class AfterList<V> implements ModifierOrderingRule<V> {
    private final List<ModifierIdentifier> ids = Lists.newLinkedList();
    
    private AfterList(List<ModifierIdentifier> ids) {
      checkNotNull(ids, "ids cannot be null");
      checkArgument(!ids.isEmpty(), "At least one id must be set");
      this.ids.addAll(ids);
    }
    
    @Override
    public int insert(Modifiers<V> modifiers, ModifierIdentifier id, Modifier<V> modifier) {
      checkArguments(modifiers, id, modifier);
      int idx = getIndex(modifiers);
      modifiers.put(id, modifier, idx);
      return idx;
    }

    private int getIndex(Modifiers<V> modifiers) {
      int result = Integer.MIN_VALUE;
      for (ModifierIdentifier id : ids) {
        int idx = modifiers.indexOf(id);
        if (idx != -1 && idx > result) {
          result = idx;
        }
      }
      if (result == Integer.MIN_VALUE) {
        result = modifiers.size();
      } else {
        result += 1;
      }
      return result;
    }
  }
  
  
}
