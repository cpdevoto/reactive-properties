package org.devoware.reactive.testutil;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

public class ValueMaps {

  /* @formatter: off */
  public static RangeMap<Integer, Integer> PROFICIENCY_BONUS_BY_LEVEL =
      ImmutableRangeMap.<Integer, Integer>builder()
      .put(Range.closed(1, 4), 2)
      .put(Range.closed(5, 8), 3)
      .put(Range.closed(9, 12), 4)
      .put(Range.closed(13, 16), 5)
      .put(Range.closed(17, 20), 6)
      .build();
  /* @formatter: off */

  private ValueMaps() {}

}
