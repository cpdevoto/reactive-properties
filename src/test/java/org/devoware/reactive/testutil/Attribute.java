package org.devoware.reactive.testutil;

import static org.devoware.reactive.testutil.AttributeModifier.STRENGTH_MOD;

import org.devoware.reactive.property.PropertyIdentifier;

public enum Attribute implements PropertyIdentifier<Integer> {
  /* @formatter: off */
  STRENGTH(STRENGTH_MOD);
  /* @formatter: on */

  AttributeModifier attributeModifier;

  private Attribute(AttributeModifier attributeModifier) {
    this.attributeModifier = attributeModifier;
  }

  public PropertyIdentifier<Integer> getAttributeModifier() {
    return attributeModifier;
  }

  @Override
  public Integer getDefaultValue() {
    return 0;
  }
  
  @Override
  public Class<Integer> getType() {
    return Integer.class;
  }

}
