package org.devoware.reactive.property;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ModifiersTest {
  
  private static enum Identifiers implements ModifierIdentifier {
    ID_1, ID_2, ID_3, ID_4;
  }
  
  private static final Modifier<Integer> modifier1 = (context, value) -> value + 1;
  private static final Modifier<Integer> modifier2 = (context, value) -> value + 2;
  private static final Modifier<Integer> modifier3 = (context, value) -> value + 3;
  private static final Modifier<Integer> modifier4 = (context, value) -> value + 4;
  
  private Modifiers<Integer> modifiers;
  
  @Before
  public void setup () {
    modifiers = Modifiers.create();  
  }
  
  @Test
  public void test () {
    modifiers.put(Identifiers.ID_1, modifier1);   
    modifiers.put(Identifiers.ID_2, modifier2);
    
    assertTrue(modifiers.containsKey(Identifiers.ID_1));
    assertTrue(modifiers.containsKey(Identifiers.ID_2));
    assertFalse(modifiers.containsKey(Identifiers.ID_3));
    
    assertThat(modifiers.indexOf(Identifiers.ID_1), equalTo(0));
    assertThat(modifiers.indexOf(Identifiers.ID_2), equalTo(1));
    assertThat(modifiers.indexOf(Identifiers.ID_3), equalTo(-1));
  
    assertThat(modifiers.size(), equalTo(2));
    
    assertThat(modifiers.get(Identifiers.ID_1), equalTo(modifier1));
    assertThat(modifiers.get(Identifiers.ID_2), equalTo(modifier2));
    assertNull(modifiers.get(Identifiers.ID_3));
    
    assertThat(modifiers.get(0), equalTo(modifier1));
    assertThat(modifiers.get(1), equalTo(modifier2));

    modifiers.put(Identifiers.ID_3, modifier3, 1);

    assertThat(modifiers.indexOf(Identifiers.ID_1), equalTo(0));
    assertThat(modifiers.indexOf(Identifiers.ID_2), equalTo(2));
    assertThat(modifiers.indexOf(Identifiers.ID_3), equalTo(1));
  
    assertThat(modifiers.size(), equalTo(3));
    
    modifiers.remove(Identifiers.ID_3);
    
    assertThat(modifiers.size(), equalTo(2));
    
    assertThat(modifiers.get(Identifiers.ID_1), equalTo(modifier1));
    assertThat(modifiers.get(Identifiers.ID_2), equalTo(modifier2));
    assertNull(modifiers.get(Identifiers.ID_3));
     
    modifiers.put(Identifiers.ID_3, modifier3, 0);

    assertThat(modifiers.indexOf(Identifiers.ID_1), equalTo(1));
    assertThat(modifiers.indexOf(Identifiers.ID_2), equalTo(2));
    assertThat(modifiers.indexOf(Identifiers.ID_3), equalTo(0));
 
    assertThat(modifiers.size(), equalTo(3));
   
    modifiers.remove(Identifiers.ID_3);
   
    assertThat(modifiers.size(), equalTo(2));
   
    assertThat(modifiers.get(Identifiers.ID_1), equalTo(modifier1));
    assertThat(modifiers.get(Identifiers.ID_2), equalTo(modifier2));
    assertNull(modifiers.get(Identifiers.ID_3));

    modifiers.put(Identifiers.ID_3, modifier3, 2);

    assertThat(modifiers.indexOf(Identifiers.ID_1), equalTo(0));
    assertThat(modifiers.indexOf(Identifiers.ID_2), equalTo(1));
    assertThat(modifiers.indexOf(Identifiers.ID_3), equalTo(2));

    assertThat(modifiers.size(), equalTo(3));
  
    modifiers.remove(Identifiers.ID_3);
  
    assertThat(modifiers.size(), equalTo(2));
    
    assertThat(modifiers.get(Identifiers.ID_1), equalTo(modifier1));
    assertThat(modifiers.get(Identifiers.ID_2), equalTo(modifier2));
    assertNull(modifiers.get(Identifiers.ID_3));
    
  }

}
