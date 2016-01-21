# reactive-properties
A lightweight Java library and DSL for managing data flows and the propagation of change between different properties based on custom lambda functions, very similar to a spreadsheet (cf. [Reactive Programming](https://en.wikipedia.org/wiki/Reactive_programming)).

## Introduction

Suppose that you are creating a "character builder" application for your favorite roleplaying game. Within an application of this sort, a "character" typically consists of a collection of interrelated properties in which the values of certain properties are derived from the values of other properties. Often, these relationships form a complex network of data flows that needs to be re-evaluated every time a property changes. Let's imagine, for instance, that your character has the following properties:

1. **Level**: The character's level is a scalar value, and it ranges from 1 to 20.
2. **Proficiency Bonus**: The character's proficiency bonus is derived from the character's level. From levels 1 to 4, the proficiency bonus is +2. From levels 5 to 8, the proficiency bonus is +3. From levels 9 to 12, the proficiency bonus is +4. From levels 13 to 16, the proficiency bonus is +5. Finally, from levels 17 to 20, the proficiency bonus is +6.
3. **Strength**: The character's strength is a scalar, and it usually ranges from 3 to 20.
4. **Strength Modifier**: The character's strength modifier is derived from the character's strength, based on the following formula: (strength - 10) / 2.
5. **Melee Attack Modifier**: The character's melee attack modifier is derived from the character's strength modifier and proficiency bonus based on the following formula: strength modifier + proficiency bonus.

Any time the value of one of these properties changes, we need to ensure that all properties which are derived from it are updated as well. Suppose we have a character with the following base properies:

```
  Level: 1
  Strength: 17
```
The derived properties should automatically take on the following values:

```
  Proficiency Bonus: 2
  Strength Modifier: 3
  Melee Attack Modifier: 5
```
If we subsequently change the character's level to 12, and his/her strength to 18, the derived properties should be automatically adjusted as follows:

```
Proficiency Bonus: 4
Strength Modifier: 4
Melee Attack Modifier: 8
```
The `reactive-properties` library was designed to handle problem domains of this sort. All properties are modeled as reactive `Property` objects which can either be assigned a literal value or a value function which may reference the values of other property objects. When a function is specified, the framework automatically detects which other properties are referenced, and creates appropriate bindings to those properties. These bindings ensure that, whenever the value of a  referenced property changes, the property which references it will be updated as well (i.e. it's value function will be invoked).  

The creation and removal of bindings between properties is handled implicitly by the framework, so developers are freed from the burden of having to manage this tangled and dynamic network of observer/observable objects. They only need to ensure that each property is given a unique identifier. A collection of properties created with the `reactive-properties` framework is typically encapsulated within one or more objects that can serve as model components for an MVC (Model-View-Controller) application.  In such cases, developers will want to register their own property change listeners such that, whenever a property changes, the corresponding updates can be applied to the user interface. The framework `reactive-properties` framework fully supports this by allowing developers to add their own listeners to any given property.

The framework also supports the notion of nested value modifiers, which allow you to decorate a property's value instead of having to rewrite it from scratch every time you want to make a tweak to the value function. In the example cited above, a character might acquire a set of Gauntlets of Ogre Power that raise his strength property to a 19 if it is currently below 19, but have no effect if the character's strength is already 19 or higher. This can easily be implemented as a modifier function that takes the character's strength score as an input and returns the adjusted strength score. Modifiers can be chained, and you are provided with several ordering rules for precise control over the order in which the modifiers are executed (i.e. first(), last(), before(), after(), atIndex()). Modifiers are extremely useful in that they allow you work with the results of property value computations while treating those computations as black boxes. While working on your 'Gauntlets of Ogre Power' modifier, for instance, you need not concern yourself with the fact that a particular character's strength score is currently derived using a complex formula, and you do not have to edit this formula. 

## Getting Started
### Quick Configuration Guide
To use the `reactive-properties` library, simply add the following repository and dependency to your Gradle script:
```
repositories {
    mavenCentral() 
    mavenRepo {
      url 'https://github.com/cpdevoto/maven-repository/raw/master/'
    }
}
...
dependencies {
    compile 'com.google.guava:guava:18.0',
            'org.devoware:reactive-properties:1.0.+'
}
```
Maven users can easily adapt these configurations within their POM files.

### Quick Development Guide
`Identifier` objects are typically modeled as one or more `enums` that implement the `Identifier` interface, as shown below.  `Identifier` objects are used to uniquely identify each property within a given context so that these properties can be unambiguously referenced from within property value functions. They are analogous to spreadsheet cell coordinates.  They also store some metadata about the properties which they correspond to, such as the property type, and the default value which is used in cases where a binding is requested to a property which does not yet exist.

```java
public enum Attribute implements Identifier<Integer> {
  STRENGTH, 
  DEXTERITY, 
  CONSTITUTION, 
  INTELLIGENCE, 
  WISDOM, 
  CHARISMA;
  
  @Override
  public Integer getDefaultValue() {
    return 0;
  }
  
  @Override Class<Integer> getType {
    return Integer.class
  }
}
```
Once you have defined the `Identifier` objects for your domain, you can begin to define `Property` objects as shown below:

```java
     // First create a property manager to manage all properties
    // with a given scope (e.g. a single character in an RPG game)
    PropertyManager manager = PropertyManagers.create();

    // Now create some properties, some with scalar value, and some
    // with value functions that reference other properties.
    Property<Integer> level = manager.create(LEVEL)
        .withValidator(
            (value) -> checkArgument(value > 0 && value < 21, "level must be between 1 and 20"))
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
          (value) -> checkArgument(value >= 3 && value <= 20, "strength must be between 3 and 20"))
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
    ModifierIdentifier gauntlets = strength.addModifier((context, value) -> {
      if (value >= 19) {
        return value;
      }
      return 19;
    });
    
    assertThat(strength.get(), equalTo(19));
    assertThat(strengthModifier.get(), equalTo(4));
    assertThat(meleeAttackModifier.get(), equalTo(7));
    
    // Let's add another strength-boosting item, but let's ensure the modifier function
    // is resolved before the function for the gauntlets.
    
    ModifierIdentifier tome = strength.addModifier((context, value) -> {
      return value + 2;
    }, before(gauntlets));

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
```








