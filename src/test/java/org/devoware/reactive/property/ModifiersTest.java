package org.devoware.reactive.property;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

public class ModifiersTest {
  
  private static enum Identifiers implements ModifierIdentifier {
    ID_1, ID_2, ID_3, ID_4, ID_5;
  }
  
  private static final Modifier<Integer> modifier1 = (context, value) -> value + 1;
  private static final Modifier<Integer> modifier2 = (context, value) -> value + 2;
  private static final Modifier<Integer> modifier3 = (context, value) -> value + 3;
  private static final Modifier<Integer> modifier4 = (context, value) -> value + 3;
  private static final Modifier<Integer> modifier5 = (context, value) -> value + 3;
  
  private Modifiers<Integer> modifiers;
  
  @Before
  public void setup () {
    modifiers = Modifiers.create();  
  }
  
  @Test
  public void test () {
    modifiers.apply(Identifiers.ID_1, modifier1);   
    modifiers.apply(Identifiers.ID_2, modifier2);
    
    assertTrue(modifiers.containsKey(Identifiers.ID_1));
    assertTrue(modifiers.containsKey(Identifiers.ID_2));
    assertFalse(modifiers.containsKey(Identifiers.ID_3));
    
    assertThat(modifiers.size(), equalTo(2));
    
    assertThat(modifiers.get(Identifiers.ID_1), equalTo(modifier1));
    assertThat(modifiers.get(Identifiers.ID_2), equalTo(modifier2));
    assertNull(modifiers.get(Identifiers.ID_3));
    
    modifiers.applyLast(Identifiers.ID_4, modifier4);
    modifiers.apply(Identifiers.ID_5, modifier5);
    modifiers.applyFirst(Identifiers.ID_3, modifier3);
    
    Iterator<Modifier<Integer>> it = modifiers.iterator();
    assertThat(it.next(), equalTo(modifier3));
    assertThat(it.next(), equalTo(modifier1));
    assertThat(it.next(), equalTo(modifier2));
    assertThat(it.next(), equalTo(modifier5));
    assertThat(it.next(), equalTo(modifier4));
    assertFalse(it.hasNext());
    
    modifiers.applyFirst(Identifiers.ID_2, modifier2);

    it = modifiers.iterator();
    assertThat(it.next(), equalTo(modifier2));
    assertThat(it.next(), equalTo(modifier3));
    assertThat(it.next(), equalTo(modifier1));
    assertThat(it.next(), equalTo(modifier5));
    assertThat(it.next(), equalTo(modifier4));
    assertFalse(it.hasNext());
    
    modifiers.applyLast(Identifiers.ID_2, modifier2);

    it = modifiers.iterator();
    assertThat(it.next(), equalTo(modifier3));
    assertThat(it.next(), equalTo(modifier1));
    assertThat(it.next(), equalTo(modifier5));
    assertThat(it.next(), equalTo(modifier4));
    assertThat(it.next(), equalTo(modifier2));
    assertFalse(it.hasNext());

    modifiers.apply(Identifiers.ID_2, modifier2);

    it = modifiers.iterator();
    assertThat(it.next(), equalTo(modifier3));
    assertThat(it.next(), equalTo(modifier1));
    assertThat(it.next(), equalTo(modifier5));
    assertThat(it.next(), equalTo(modifier2));
    assertThat(it.next(), equalTo(modifier4));
    assertFalse(it.hasNext());
  }

}
