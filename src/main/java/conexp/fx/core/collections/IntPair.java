package conexp.fx.core.collections;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Comparator;

public class IntPair extends Pair<Integer, Integer> implements Cloneable {

  public static final IntPair valueOf(final int x, final int y) {
    return new IntPair(x, y);
  }

  public static final IntPair zero() {
    return new IntPair();
  }

  public static final Comparator<IntPair> CANTORIAN_COMPARATOR          = new Comparator<IntPair>() {

                                                                          @Override
                                                                          public int compare(IntPair o1, IntPair o2) {
                                                                            if (o1.equals(o2))
                                                                              return 0;
                                                                            if (cantorianSum(
                                                                                o1.x,
                                                                                o1.y) < cantorianSum(o2.x, o2.y))
                                                                              return -1;
                                                                            return 1;
                                                                          }
                                                                        };
  public static final Comparator<IntPair> POSITIVE_CANTORIAN_COMPARATOR = new Comparator<IntPair>() {

                                                                          @Override
                                                                          public int compare(IntPair o1, IntPair o2) {
                                                                            if (o1.equals(o2))
                                                                              return 0;
                                                                            if (positiveCantorianSum(
                                                                                o1.x,
                                                                                o1.y) < positiveCantorianSum(
                                                                                    o2.x,
                                                                                    o2.y))
                                                                              return -1;
                                                                            return 1;
                                                                          }
                                                                        };

  public static final int cantorianSum(final int m, final int n) {
    int shift = 0;
    if (m < 0)
      shift++;
    if (n < 0)
      shift += 2;
    // shift is 0 iff both are positive
    // shift is 1 iff only n is positive
    // shift is 2 iff only m is positive
    // shift is 3 iff none is positive
    return 4 * positiveCantorianSum(Math.abs(m), Math.abs(n)) + shift;
  }

  /**
   * cantorianSum is a bijection between positive integers and pairs of positive integers
   */
  public static final int positiveCantorianSum(final int m, final int n) {
    if (m < 0 || n < 0)
      throw new RuntimeException("positive cantorian sum can only be calculated for positive integers!");
    return m + ((m + n + 1) * (m + n + 2)) / 2;
//    int cantorianSum = m;
//    for (int i = 1; i < m + n + 1; i++)
//      cantorianSum += i;
//    return cantorianSum;
  }

  public static final IntPair findPositiveCantorianSum(final int sum) {
    int m = sum;
    for (int n = 0; n < Integer.MAX_VALUE; n++)
      if (positiveCantorianSum(m, n) == sum)
        return IntPair.valueOf(m, n);
    return null;
  }

  public IntPair(final int x, final int y) {
    super(Integer.valueOf(x), Integer.valueOf(y));
  }

  public IntPair() {
    super(Integer.valueOf(0), Integer.valueOf(0));
  }

  @Override
  public boolean equals(Object object) {
    if (object == null)
      return false;
    if (!(object instanceof IntPair))
      return false;
    final IntPair other = (IntPair) object;
    return other.x() == this.x() && other.y() == this.y();
  }

  @Override
  public int hashCode() {
    return cantorianSum(x(), y());
  }

//  public final int x() {
//    return x.intValue();
//  }
//
//  public final int y() {
//    return y.intValue();
//  }

  @Override
  public final IntPair clone() {
    return new IntPair(x(), y());
  }

  public final void setX(final int x) {
    this.x = Integer.valueOf(x);
  }

  public final void setY(final int y) {
    this.y = Integer.valueOf(y);
  }

  public final void set(final int x, final int y) {
    setX(x);
    setY(y);
  }

  public final void set(final IntPair coordinates) {
    set(coordinates.x(), coordinates.y());
  }

  public final IntPair negate() {
    setX(-x());
    setY(-y());
    return this;
  }

  public final IntPair add(final int x, final int y) {
    this.x += Integer.valueOf(x);
    this.y += Integer.valueOf(y);
    return this;
  }

  public final IntPair add(final IntPair coordinates) {
    add(coordinates.x(), coordinates.y());
    return this;
  }

  public final IntPair subtract(final int x, final int y) {
    this.x -= Integer.valueOf(x);
    this.y -= Integer.valueOf(y);
    return this;
  }

  public final IntPair subtract(final IntPair coordinates) {
    subtract(coordinates.x(), coordinates.y());
    return this;
  }

  public final IntPair delta(final int x, final int y) {
    return new IntPair(x, y).minus(this);
  }

  public final IntPair delta(final IntPair coordinates) {
    return coordinates.clone().minus(this);
  }

  public final IntPair plus(final int x, final int y) {
    return clone().add(x, y);
  }

  public final IntPair plus(final IntPair coordinates) {
    return clone().add(coordinates);
  }

  public final IntPair minus(final int x, final int y) {
    return clone().subtract(x, y);
  }

  public final IntPair minus(final IntPair coordinates) {
    return clone().subtract(coordinates);
  }

  public final IntPair negative() {
    return clone().negate();
  }
}
