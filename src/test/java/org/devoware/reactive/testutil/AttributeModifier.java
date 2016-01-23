package org.devoware.reactive.testutil;

import org.devoware.reactive.property.PropertyIdentifier;

public enum AttributeModifier implements PropertyIdentifier<Integer> {
  /* @formatter: off */
  STRENGTH_MOD;
  /* @formatter: on */

  @Override
  public Integer getDefaultValue() {
    return 0;
  }

  @Override
  public Class<Integer> getType() {
    return Integer.class;
  }
  
}
