/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.collections.setlist;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import conexp.fx.core.collections.ListIterators;
import conexp.fx.core.collections.pair.Pair;

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
        return ListIterators.concat(
            s1.listIterator(),
            ListIterators.filter(s2.listIterator(), Predicates.not(Predicates.in(s1))),
            i);
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
