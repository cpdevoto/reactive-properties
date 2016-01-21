package org.devoware.reactive.testutil;

import static org.devoware.reactive.testutil.AttributeModifierIdentifier.STRENGTH_MOD;

import org.devoware.reactive.property.Identifier;

public enum AttributeIdentifier implements Identifier<Integer> {
  /* @formatter: off */
  STRENGTH(STRENGTH_MOD);
  /* @formatter: on */

  AttributeModifierIdentifier attributeModifierId;

  private AttributeIdentifier(AttributeModifierIdentifier attributeModifierId) {
    this.attributeModifierId = attributeModifierId;
  }

  public Identifier<Integer> getAttributeModifierId() {
    return attributeModifierId;
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
