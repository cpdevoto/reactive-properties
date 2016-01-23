package org.devoware.reactive;

import static com.google.common.base.Preconditions.checkArgument;
import static org.devoware.reactive.property.ModifierOrderingRules.*;
import static org.devoware.reactive.testutil.Attribute.STRENGTH;
import static org.devoware.reactive.testutil.AttributeModifier.STRENGTH_MOD;
import static org.devoware.reactive.testutil.BasicProperty.LEVEL;
import static org.devoware.reactive.testutil.BasicProperty.MELEE_ATTACK_MOD;
import static org.devoware.reactive.testutil.BasicProperty.PROFICIENCY_BONUS;
import static org.devoware.reactive.testutil.Sense.DARKVISION;
import static org.devoware.reactive.testutil.SenseDistance.DARKVISION_DISTANCE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.devoware.reactive.property.CyclicBindingException;
import org.devoware.reactive.property.PropertyIdentifier;
import org.devoware.reactive.property.Identifier;
import org.devoware.reactive.property.Property;
import org.devoware.reactive.property.PropertyChangeListener;
import org.devoware.reactive.property.PropertyManager;
import org.devoware.reactive.property.PropertyManagers;
import org.devoware.reactive.testutil.Sense;
import org.devoware.reactive.testutil.SenseDistance;
import org.devoware.reactive.testutil.ValueMaps;
import org.junit.Before;
import org.junit.Test;

public class PropertyTest {
  
  private PropertyManager manager;
  
  @Before
  public void setup () {
    manager = PropertyManagers.create();
  }

  @Test
  public void test_bindings() {
    /* @formatter: off */
    Property<Integer> level = manager.create(LEVEL)
        .withValidator(
            (context, value) -> checkArgument(value > 0 && value < 21, "level must be between 1 and 20"))
        .withValue(1)
        .build();

    Property<Integer> proficiencyBonus = manager.create(PROFICIENCY_BONUS)
         .withValue(
             (context) -> ValueMaps.PROFICIENCY_BONUS_BY_LEVEL.get(context.get(LEVEL)))
         .build();

    Property<Integer> strength = manager.create(STRENGTH)
        .withValidator(
            (context, value) -> checkArgument(value > 0 && value < 21, "strength must be between 1 and 20"))
        .withValue(8)
        .build();

    strength.set(17);

    Property<Integer> strengthModifier = manager.create(STRENGTH_MOD)
        .withValue((context) -> (context.get(STRENGTH) - 10) / 2)
        .build();

    Property<Integer> meleeAttackModifier = manager.create(MELEE_ATTACK_MOD)
            .withValue(
                (context) -> context.get(STRENGTH_MOD) + context.get(PROFICIENCY_BONUS))
        .build();
    /* @formatter: on */
    
    assertTrue(manager.getConsumerBindings(level).isEmpty());
    assertThat(manager.getProducerBindings(level).size(), equalTo(1));
    assertTrue(manager.getProducerBindings(level).contains(proficiencyBonus.getId()));

    assertThat(manager.getConsumerBindings(proficiencyBonus).size(), equalTo(1));
    assertTrue(manager.getConsumerBindings(proficiencyBonus).contains(level.getId()));
    assertThat(manager.getProducerBindings(proficiencyBonus).size(), equalTo(1));
    assertTrue(manager.getProducerBindings(proficiencyBonus).contains(meleeAttackModifier.getId()));

    assertTrue(manager.getConsumerBindings(strength).isEmpty());
    assertThat(manager.getProducerBindings(strength).size(), equalTo(1));
    assertTrue(manager.getProducerBindings(strength).contains(strengthModifier.getId()));

    assertThat(manager.getConsumerBindings(strengthModifier).size(), equalTo(1));
    assertTrue(manager.getConsumerBindings(strengthModifier).contains(strength.getId()));
    assertThat(manager.getProducerBindings(strengthModifier).size(), equalTo(1));
    assertTrue(manager.getProducerBindings(strengthModifier).contains(meleeAttackModifier.getId()));

    assertThat(manager.getConsumerBindings(meleeAttackModifier).size(), equalTo(2));
    assertTrue(manager.getConsumerBindings(meleeAttackModifier).contains(strengthModifier.getId()));
    assertTrue(manager.getConsumerBindings(meleeAttackModifier).contains(proficiencyBonus.getId()));
    assertTrue(manager.getProducerBindings(meleeAttackModifier).isEmpty());
    
    assertThat(level.get(), equalTo(1));
    assertThat(proficiencyBonus.get(), equalTo(2));
    assertThat(strength.get(), equalTo(17));
    assertThat(strengthModifier.get(), equalTo(3));
    assertThat(meleeAttackModifier.get(), equalTo(5));

    strength.set(18);

    assertThat(level.get(), equalTo(1));
    assertThat(proficiencyBonus.get(), equalTo(2));
    assertThat(strength.get(), equalTo(18));
    assertThat(strengthModifier.get(), equalTo(4));
    assertThat(meleeAttackModifier.get(), equalTo(6));

    level.set(12);

    assertThat(level.get(), equalTo(12));
    assertThat(proficiencyBonus.get(), equalTo(4));
    assertThat(strength.get(), equalTo(18));
    assertThat(strengthModifier.get(), equalTo(4));
    assertThat(meleeAttackModifier.get(), equalTo(8));

    level.set(20);
    strength.set(20);

    assertThat(level.get(), equalTo(20));
    assertThat(proficiencyBonus.get(), equalTo(6));
    assertThat(strength.get(), equalTo(20));
    assertThat(strengthModifier.get(), equalTo(5));
    assertThat(meleeAttackModifier.get(), equalTo(11));
    
    // Let's reset the strength modifier to a fixed value
    strengthModifier.set(3);
    
    assertThat(manager.getConsumerBindings(strength).size(), equalTo(0));
    assertThat(manager.getConsumerBindings(strengthModifier).size(), equalTo(0));
    assertThat(manager.getProducerBindings(strengthModifier).size(), equalTo(1));
    assertTrue(manager.getProducerBindings(strengthModifier).contains(meleeAttackModifier.getId()));

    assertThat(strengthModifier.get(), equalTo(3));
    assertThat(meleeAttackModifier.get(), equalTo(9));
    
    // Let's reset the strength modifier to a different function
    strengthModifier.set((context) -> context.get(PROFICIENCY_BONUS));
    
    assertThat(strengthModifier.get(), equalTo(6));
    assertThat(meleeAttackModifier.get(), equalTo(12));
  }

  @Test(expected = CyclicBindingException.class)
  public void test_cannot_bind_value_to_itself() {
    manager.create(PropertyId.PROPERTY1)
        .withValue((context) -> context.get(PropertyId.PROPERTY1))
        .build();
  }

  @Test
  public void test_binding_cycles() {
    manager.create(PropertyId.PROPERTY1)
        .withValue((context) -> context.get(PropertyId.PROPERTY2))
        .build();
    manager.create(PropertyId.PROPERTY2)
        .withValue((context) -> context.get(PropertyId.PROPERTY3))
        .build();
    try {
      manager.create(PropertyId.PROPERTY3)
          .withValue((context) -> context.get(PropertyId.PROPERTY1))
          .build();
      fail("Expected a CyclicBindingExcption");
    } catch (CyclicBindingException e) {}
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void test_listeners() {
    PropertyChangeListener<Integer> strengthListener = mock(PropertyChangeListener.class);
    PropertyChangeListener<Integer> strengthModifierListener = mock(PropertyChangeListener.class);
    
    Property<Integer> strength = manager.create(STRENGTH)
        .withValidator(
            (context, value) -> checkArgument(value > 0 && value < 21, "strength must be between 1 and 20"))
        .withValue(8)
        .build();
    strength.addPropertyChangeListener(strengthListener);

    Property<Integer> strengthModifier = manager.create(STRENGTH_MOD)
        .withValue((context) -> (context.get(STRENGTH) - 10) / 2)
        .build();
    strengthModifier.addPropertyChangeListener(strengthModifierListener);

    strength.set(17);
    
    assertThat(strength.get(), equalTo(17));
    assertThat(strengthModifier.get(), equalTo(3));
    
    verify(strengthListener, times(1)).onValueChanged(any(), any());
    verify(strengthListener, times(1)).onValueChanged(eq(STRENGTH), eq(17));

    verify(strengthModifierListener, times(1)).onValueChanged(any(), any());
    verify(strengthModifierListener, times(1)).onValueChanged(eq(STRENGTH_MOD), eq(3));

    strength.removePropertyChangeListener(strengthListener);
    strengthModifier.removePropertyChangeListener(strengthModifierListener);
    
    strength.set(10);

    assertThat(strength.get(), equalTo(10));
    assertThat(strengthModifier.get(), equalTo(0));
  
    verify(strengthListener, times(1)).onValueChanged(any(), any());
    verify(strengthModifierListener, times(1)).onValueChanged(any(), any());
  }
  
  public void test_validators() {
    
    try {
      manager.create(STRENGTH)
          .withValidator(
            (context, value) -> checkArgument(value > 0 && value < 21, "strength must be between 1 and 20"))
          .withValue(24)
          .build();
      fail("Expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {}

    Property<Integer> level = manager.create(LEVEL)
        .withValidator(
          (context, value) -> checkArgument(value > 0 && value < 21, "strength must be between 1 and 20"))
        .withValue(1)
        .build();
    Property<Integer> proficiencyBonus = manager.create(PROFICIENCY_BONUS)
        .withValue(
            (context) -> ValueMaps.PROFICIENCY_BONUS_BY_LEVEL.get(context.get(LEVEL)))
        .build();
    try {
    level.set(21);
    fail("Expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertThat(level.get(), equalTo(1));
      assertThat(proficiencyBonus.get(), equalTo(2));
    }
  }
  
  @Test
  public void test_modifiers () {
    Property<Integer> strength = manager.create(STRENGTH)
        .withValidator(
            (context, value) -> checkArgument(value > 0 && value < 21, "strength must be between 1 and 20"))
        .withValue(8)
        .build();

    Property<Integer> strengthModifier = manager.create(STRENGTH_MOD)
        .withValue((context) -> (context.get(STRENGTH) - 10) / 2)
        .build();
    
    assertThat(strength.get(), equalTo(8));
    assertThat(strengthModifier.get(), equalTo(-1));

    Identifier gauntletsOfOgrePower = strength.addModifier((context, value) -> {
      if (value >= 19) {
        return value;
      }
      return 19;
    }, applyLast());
    
    assertThat(strength.get(), equalTo(19));
    assertThat(strengthModifier.get(), equalTo(4));
    
    strength.set(20);
    assertThat(strength.get(), equalTo(20));
    assertThat(strengthModifier.get(), equalTo(5));
    
    strength.set(14);
    assertThat(strength.get(), equalTo(19));
    assertThat(strengthModifier.get(), equalTo(4));

    Identifier tomeOfMastery = strength.addModifier((context, value) -> {
      return value + 2;
    });

    assertThat(strength.get(), equalTo(19));
    assertThat(strengthModifier.get(), equalTo(4));
    
    strength.removeModifier(gauntletsOfOgrePower);
    
    assertThat(strength.get(), equalTo(16));
    assertThat(strengthModifier.get(), equalTo(3));

    strength.removeModifier(tomeOfMastery);
    
    assertThat(strength.get(), equalTo(14));
    assertThat(strengthModifier.get(), equalTo(2));
  }

  private static enum PropertyId implements PropertyIdentifier<Integer> {
    PROPERTY1, PROPERTY2, PROPERTY3;
  
    @Override
    public Integer getDefaultValue() {
      return 0;
    }
    
    @Override
    public Class<Integer> getType() {
      return Integer.class;
    }
  }
  
  @Test
  public void test_readme_sample() {
    // First create a property manager to manage all properties
    // with a given scope (e.g. a single character in an RPG game)
    PropertyManager manager = PropertyManagers.create();

    // Now create some properties, some with scalar value, and some
    // with value functions that reference other properties.
    Property<Integer> level = manager.create(LEVEL)
        .withValidator(
            (context, value) -> checkArgument(value > 0 && value < 21, "level must be between 1 and 20"))
        .withValue(1)
        .build();

    assertThat(level.get(), equalTo(1)); 
    
    Property<Integer> proficiencyBonus = manager.create(PROFICIENCY_BONUS)
        .withValue(
            (context) -> ValueMaps.PROFICIENCY_BONUS_BY_LEVEL.get(context.get(LEVEL)))
        .build();
    
    assertThat(proficiencyBonus.get(), equalTo(2));        

    Property<Integer> strength = manager.create(STRENGTH)
        .withValidator(
          (context, value) -> checkArgument(value >= 3 && value <= 20, "strength must be between 3 and 18"))
        .withValue(8)   
        .build();

    assertThat(strength.get(), equalTo(8));        

    Property<Integer> strengthModifier = manager.create(STRENGTH_MOD)
        .withValue((context) -> (context.get(STRENGTH) - 10) / 2)
        .build();

    assertThat(strengthModifier.get(), equalTo(-1));        

    Property<Integer> meleeAttackModifier = manager.create(MELEE_ATTACK_MOD)
        .withValue(
            (context) -> context.get(STRENGTH_MOD) + context.get(PROFICIENCY_BONUS))
        .build();
    
    assertThat(meleeAttackModifier.get(), equalTo(1));    
        
    // Now let's change the level and strength properties to see if the derived properties are
    // automatically updated.
    
    level.set(5);
    strength.set(14);
    
    assertThat(level.get(), equalTo(5));
    assertThat(proficiencyBonus.get(), equalTo(3));
    assertThat(strength.get(), equalTo(14));
    assertThat(strengthModifier.get(), equalTo(2));
    assertThat(meleeAttackModifier.get(), equalTo(5));
    
    // Let's add some Gauntlets of Ogre Power
    Identifier gauntlets = strength.addModifier((context, value) -> {
      if (value >= 19) {
        return value;
      }
      return 19;
    }, applyLast());
    
    assertThat(strength.get(), equalTo(19));
    assertThat(strengthModifier.get(), equalTo(4));
    assertThat(meleeAttackModifier.get(), equalTo(7));
    
    // Let's add another strength-boosting item, and ensure the modifier function
    // is resolved before the function for the gauntlets.
    
    Identifier tome = strength.addModifier((context, value) -> {
      return value + 2;
    });

    // Since the tome modifier is evaluated before the gauntlets modifier,
    // the strength score has the expected value 19 as opposed to 21. 
    // This is exactly what we want.
    
    assertThat(strength.get(), equalTo(19)); 
    assertThat(strengthModifier.get(), equalTo(4));
    assertThat(meleeAttackModifier.get(), equalTo(7));
    
    // Now let's remove the gauntlets; the strength should revert to
    // 16 instead of 14, because the tome modifier is still in effect.
    
    strength.removeModifier(gauntlets);
    
    assertThat(strength.get(), equalTo(16)); 
    assertThat(strengthModifier.get(), equalTo(3));
    assertThat(meleeAttackModifier.get(), equalTo(6));
    
    // Finally, let's remove the tome; the strength should revert
    // to it's current unmodified value.
    
    strength.removeModifier(tome);
    
    assertThat(strength.get(), equalTo(14)); 
    assertThat(strengthModifier.get(), equalTo(2));
    assertThat(meleeAttackModifier.get(), equalTo(5));
  }
  
  @Test
  public void test_remove() {
    manager.create(DARKVISION)
      .withValue((context) -> "Darkvision (" + context.get(DARKVISION_DISTANCE) + " ft.)")
      .build();

    Property<Integer> darkvisionDistance = manager.create(DARKVISION_DISTANCE)
      .withValue(60)
      .build();
    
    assertThat(manager.get(DARKVISION).get(), equalTo("Darkvision (60 ft.)"));
    
    darkvisionDistance.set(120);
    
    assertThat(manager.get(DARKVISION).get(), equalTo("Darkvision (120 ft.)"));
    
    manager.remove(DARKVISION_DISTANCE);

    assertThat(manager.get(DARKVISION).get(), equalTo("Darkvision (0 ft.)"));
  }
}
