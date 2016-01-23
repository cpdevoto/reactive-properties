package org.devoware.reactive.testutil;

import org.devoware.reactive.property.PropertyIdentifier;

public enum Sense implements PropertyIdentifier<String> {
  DARKVISION;

  @Override
  public String getDefaultValue() {
    return "";
  }

  @Override
  public Class<String> getType() {
    return String.class;
  }
}
