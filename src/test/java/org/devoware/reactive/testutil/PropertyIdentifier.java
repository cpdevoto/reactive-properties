package org.devoware.reactive.testutil;

import org.devoware.reactive.property.Identifier;

public enum PropertyIdentifier implements Identifier<Integer> {
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
