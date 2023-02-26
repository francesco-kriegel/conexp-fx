/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.collections.setlist;

/*-
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.BitSetFX;
import conexp.fx.core.math.GuavaIsomorphism;
import conexp.fx.core.math.Isomorphism;

public interface SetList<E> extends Set<E>, List<E>, Collection<E>, Iterable<E>, Cloneable {

  public boolean set(Object o, E e);

  public Collection<E> getAll(Collection<Integer> c, boolean includeNull);

  public Collection<Integer> indicesOf(Collection<?> c, boolean includeMinusOne);

  @Deprecated
  public int lastIndexOf(Object o);

  public SetList<E> filter(Predicate<? super E> p);

  public SetList<E> subList(int from, int to);

  public BitSetFX subBitSet(Collection<?> c);

  public default HashSetArrayList<E> clone() {
    return new HashSetArrayList<E>(this);
  }

  public default GuavaIsomorphism<E, Integer> indexGuava() {
    return new GuavaIsomorphism<E, Integer>() {

      @Override
      public final Integer apply(final E e) {
        return indexOf(e);
      }

      @Override
      public final E invert(final Integer i) {
        return get(i);
      }
    };
  }

  public default Isomorphism<E, Integer> index() {
    return new Isomorphism<E, Integer>(this::indexOf, this::get);
  }

  public default Isomorphism<E, Integer> currentIndex() {
    final Map<E, Integer> indexOfMap = new ConcurrentHashMap<>();
    final Map<Integer, E> getMap = new ConcurrentHashMap<>();
    int i = 0;
    for (E e : this) {
      indexOfMap.put(e, i);
      getMap.put(i, e);
      i++;
    }
    return new Isomorphism<E, Integer>(indexOfMap::get, getMap::get);
  }

  @Override
  default Spliterator<E> spliterator() {
    return Spliterators.spliterator(this, Spliterator.ORDERED);
  }

  default LecticOrder<E> getLecticOrder() {
    return new LecticOrder<E>(this);
  }

  public class LecticOrder<E> {

    private final SetList<E> base;

    private LecticOrder(final SetList<E> base) {
      super();
      this.base = base;
    }

    public static final <T> Comparator<T> toComparator(final BiPredicate<T, T> isSmallerPredicate) {
      return (x, y) -> x.equals(y) ? 0 : isSmallerPredicate.test(x, y) ? -1 : 1;
    }

    public final Comparator<E> getIndexComparator() {
      return (x, y) -> {
        final int i = base.indexOf(x);
        final int j = base.indexOf(y);
        if (i == j)
          return 0;
        if (i < j)
          return -1;
        return 1;
      };
    }

    public final Comparator<Set<E>> getLecticComparator() {
      return toComparator(this::isSmaller);
    }

    public final Comparator<Set<E>> getLecticComparator(final E element) {
      return toComparator((x, y) -> isSmaller(x, y, element));
    }

    public final boolean isSmaller(final Set<E> set1, final Set<E> set2) {
      return !set1.equals(set2) && Sets
          .symmetricDifference(set1, set2)
          .stream()
          .sorted(getIndexComparator())
          .findFirst()
          .map(set2::contains)
          .get();
    }

    public final boolean isSmaller(final Set<E> set1, final Set<E> set2, final E element) {
//      if (set1.equals(set2))
//        return false;
//      if (!set2.contains(element))
//        return false;
//      return Stream
//          .concat(
//              set1.parallelStream().filter(
//                  s -> !set2.contains(
//                      s)),
//              set2.parallelStream().filter(
//                  s -> !set1.contains(
//                      s)))
//          .min(
//              getSetListComparator(
//                  base))
//          .get()
//          .equals(
//              element);
      return !set1.equals(set2) && set2.contains(element) && Sets
          .symmetricDifference(set1, set2)
          .stream()
          .sorted(getIndexComparator())
          .findFirst()
          .get()
          .equals(element);
    }

    public final Set<E> oplus(final Set<E> set, final E m) {
      final Set<E> result = new HashSet<E>(set);
      result.retainAll(base.subList(0, base.indexOf(m)));
      result.add(m);
      return result;
    }

  }
}
