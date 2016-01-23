package org.devoware.reactive.testutil;

import org.devoware.reactive.property.PropertyIdentifier;

public enum BasicProperty implements PropertyIdentifier<Integer> {
  /* @formatter: off */
  PROFICIENCY_BONUS, 
  LEVEL, 
  MELEE_ATTACK_MOD;
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
