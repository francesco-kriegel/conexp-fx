/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.collections.setlist;

/*
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

import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import conexp.fx.core.collections.ListIterators;
import conexp.fx.core.collections.Pair;

public final class SetLists {

  public static final <E> SetList<E> empty() {
    return new UnmodifiableSetList<E>() {

      public final ListIterator<E> listIterator(final int i) {
        return ListIterators.<E> empty();
      }
    };
  }

  public static final <E> HashSetArrayList<E> create(@SuppressWarnings("unchecked") final E... e) {
    return create(Arrays.asList(e));
  }

  public static final <E> HashSetArrayList<E> create(final Collection<? extends E> c) {
    return new HashSetArrayList<E>(c);
  }

  public static final <E> SetList<E> unmodifiable(final SetList<E> s) {
    if (s instanceof UnmodifiableSetList)
      return (UnmodifiableSetList<E>) s;
    return new UnmodifiableSetList<E>() {

      public final ListIterator<E> listIterator(final int i) {
        return ListIterators.unmodifiable(s.listIterator(i));
      }
    };
  }

  public static final <E> SetList<E> filter(final SetList<? extends E> s, final Predicate<? super E> p) {
    return new UnmodifiableSetList<E>() {

      public final ListIterator<E> listIterator(final int i) {
        return ListIterators.filter(s.listIterator(), p, i);
      }
    };
  }

  public static final <T, E> SetList<E> transform(final SetList<? extends T> s, final Function<? super T, E> f) {
    return new UnmodifiableSetList<E>() {

      public final ListIterator<E> listIterator(final int i) {
        return ListIterators.transform(s.listIterator(i), f);
      }
    };
  }

  public static final SetList<Integer> integers(final int size) {
    return new UnmodifiableSetList<Integer>() {

      public final ListIterator<Integer> listIterator(final int i) {
        return ListIterators.integers(i, size);
      }

      @Override
      public final int size() {
        return size;
      }
    };
  }

  public static final <T, E> SetList<Pair<T, E>> disjointUnion(final SetList<T> s1, final SetList<E> s2) {
    return new UnmodifiableSetList<Pair<T, E>>() {

      public final ListIterator<Pair<T, E>> listIterator(final int i) {
        return ListIterators.disjointUnion(s1.listIterator(), s2.listIterator(), i);
      }
    };
  }

  public static final <T, E> SetList<Pair<T, E>> cartesianProduct(final SetList<T> s1, final SetList<E> s2) {
    return new UnmodifiableSetList<Pair<T, E>>() {

      public final ListIterator<Pair<T, E>> listIterator(final int i) {
        return ListIterators.cartesianProduct(s1.listIterator(), s2.listIterator(), i);
      }
    };
  }

  public static final <E> SetList<E> union(final SetList<? extends E> s1, final SetList<? extends E> s2) {
    return new UnmodifiableSetList<E>() {

      public final ListIterator<E> listIterator(final int i) {
        return ListIterators
            .concat(s1.listIterator(), ListIterators.filter(s2.listIterator(), Predicates.not(Predicates.in(s1))), i);
      }
    };
  }

  public static final <E> SetList<E> intersection(final SetList<E> s, final Collection<?> c) {
    return new UnmodifiableSetList<E>() {

      public final ListIterator<E> listIterator(final int i) {
        return ListIterators.filter(s.listIterator(), Predicates.in(c), i);
      }
    };
  }

  public static final <E> SetList<E> difference(final SetList<E> s, final Collection<?> c) {
    return new UnmodifiableSetList<E>() {

      public final ListIterator<E> listIterator(final int i) {
        return ListIterators.filter(s.listIterator(), Predicates.not(Predicates.in(c)), i);
      }
    };
  }

  public static final <E> SetList<SetList<E>> powerSet(final SetList<E> s) {
    return new UnmodifiableSetList<SetList<E>>() {

      private final int size = 1 << s.size();

      public final int size() {
        return size;
      }

      public final boolean isEmpty() {
        return false;
      }

      public final SetList<E> get(final int i) {
        if (i < 0 || i > size)
          throw new IndexOutOfBoundsException();
        return new UnmodifiableSetList<E>() {

          public ListIterator<E> listIterator(final int j) {
            return ListIterators.filter(s.listIterator(), new Predicate<E>() {

              public final boolean apply(final E e) {
                return (i >> s.indexOf(e)) % 2 == 1;
              }
            }, j);
          }
        };
      }

      public final ListIterator<SetList<E>> listIterator(final int i) {
        return ListIterators.transform(ListIterators.integers(i, size), new Function<Integer, SetList<E>>() {

          public final SetList<E> apply(final Integer j) {
            return get(j);
          }
        });
      }
    };
  }
}
