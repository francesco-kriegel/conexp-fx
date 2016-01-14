/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.collections.setlist;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

import com.google.common.base.Predicate;

import conexp.fx.core.collections.IntegerSet;
import conexp.fx.core.math.Isomorphism;

public interface SetList<E> extends Set<E>, List<E>, Collection<E>, Iterable<E>, Cloneable {

  public boolean set(Object o, E e);

  public Collection<E> getAll(Collection<Integer> c, boolean includeNull);

  public Collection<Integer> indicesOf(Collection<?> c, boolean includeMinusOne);

  @Deprecated
  public int lastIndexOf(Object o);

  public SetList<E> filter(Predicate<? super E> p);

  public SetList<E> subList(int from, int to);

  public BitSet subBitSet(Collection<?> c);

  public HashSetArrayList<E> clone();

  public Isomorphism<E, Integer> index();

  @Override
  default Spliterator<E> spliterator() {
    return Spliterators.spliterator(this, Spliterator.ORDERED);
  }
}
