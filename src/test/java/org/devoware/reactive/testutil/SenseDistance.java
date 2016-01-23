package org.devoware.reactive.testutil;

import org.devoware.reactive.property.PropertyIdentifier;

public enum SenseDistance implements PropertyIdentifier<Integer> {
  DARKVISION_DISTANCE;

  @Override
  public Integer getDefaultValue() {
    return 0;
  }

  @Override
  public Class<Integer> getType() {
    return Integer.class;
  }

}
