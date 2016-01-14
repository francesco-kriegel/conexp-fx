package conexp.fx.core.collections;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.lang.reflect.Field;
import java.util.BitSet;

import org.ujmp.core.collections.set.BitSetSet;

public final class BitSetSet2 extends BitSetSet {

  /**
   * 
   */
  private static final long serialVersionUID = -282408675520936742L;

  private final BitSet      _bitset;

  public BitSetSet2() {
    super();
    this._bitset = init_bitset();
  }

  public BitSetSet2(final BitSetSet source) {
    super(source);
    this._bitset = init_bitset();
  }

  private final BitSet init_bitset() {
    try {
      final Field field = BitSetSet.class.getDeclaredField("bitset");
      field.setAccessible(true);
      return ((BitSet) field.get((BitSetSet) this));
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  public final BitSet getBitSet() {
    return _bitset;
  }

  @Override
  public int hashCode() {
    return getBitSet().hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof BitSetSet2))
      return false;
    final BitSetSet2 other = (BitSetSet2) o;
    return this.containsAll(other) && other.containsAll(this);
  }

  @Override
  public BitSetSet2 clone() {
    return new BitSetSet2(this);
  }
}
