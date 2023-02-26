/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.collections;

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

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.UnmodifiableListIterator;

public final class ListIterators {

  public static final <E> ListIterator<E> empty() {
    return new UnmodifiableListIterator<E>() {

      public final boolean hasNext() {
        return false;
      }

      public final E next() {
        return null;
      }

      public final boolean hasPrevious() {
        return false;
      }

      public final E previous() {
        return null;
      }

      public final int nextIndex() {
        return 0;
      }

      public final int previousIndex() {
        return -1;
      }
    };
  }

  public static final ListIterator<Integer> integers(final int size) {
    return integers(0, size);
  }

  public static final ListIterator<Integer> integers(final int i, final int size) {
    return new UnmodifiableListIterator<Integer>() {

      private int j = i - 1;

      public synchronized final boolean hasNext() {
        return j < size - 1;
      }

      public synchronized final Integer next() {
        if (!hasNext())
          throw new NoSuchElementException();
        return ++j;
      }

      public final boolean hasPrevious() {
        return j > -1;
      }

      public final Integer previous() {
        if (!hasPrevious())
          throw new NoSuchElementException();
        return j--;
      }

      public synchronized final int nextIndex() {
        return j + 1;
      }

      public final int previousIndex() {
        return j;
      }
    };
  }

  public static final <E> UnmodifiableListIterator<E> unmodifiable(final ListIterator<E> it) {
    if (it instanceof UnmodifiableListIterator)
      return (UnmodifiableListIterator<E>) it;
    return new UnmodifiableListIterator<E>() {

      public final boolean hasNext() {
        return it.hasNext();
      }

      public final E next() {
        return it.next();
      }

      public final boolean hasPrevious() {
        return it.hasPrevious();
      }

      public final E previous() {
        return it.previous();
      }

      public final int nextIndex() {
        return it.nextIndex();
      }

      public final int previousIndex() {
        return it.previousIndex();
      }
    };
  }

  public static final <T, E> ListIterator<E>
      transform(final ListIterator<? extends T> it, final Function<? super T, E> f) {
    return new UnmodifiableListIterator<E>() {

      public final boolean hasNext() {
        return it.hasNext();
      }

      public final E next() {
        return f.apply(it.next());
      }

      public final boolean hasPrevious() {
        return it.hasPrevious();
      }

      public final E previous() {
        return f.apply(it.previous());
      }

      public final int nextIndex() {
        return it.nextIndex();
      }

      public final int previousIndex() {
        return it.previousIndex();
      }
    };
  }

  public static final <E> ListIterator<E> filter(final ListIterator<? extends E> it, final Predicate<? super E> p) {
    return filter(it, p, 0);
  }

  public static final <E> ListIterator<E>
      filter(final ListIterator<? extends E> it, final Predicate<? super E> p, final int i) {
    return new SimpleListIterator<E>(i) {

      protected final E createNext() {
        while (it.hasNext()) {
          final E next = it.next();
          if (p.apply(next))
            return next;
        }
        return null;
      }

      protected final E createPrevious() {
        while (it.hasPrevious()) {
          final E prev = it.previous();
          if (p.apply(prev))
            return prev;
        }
        return null;
      }
    };
  }

  public static final <E> ListIterator<E>
      concat(final ListIterator<? extends E> it1, final ListIterator<? extends E> it2, final int i) {
    return new SimpleListIterator<E>(i) {

      protected final E createNext() {
        if (it1.hasNext())
          return it1.next();
        if (it2.hasNext())
          return it2.next();
        return null;
      }

      protected final E createPrevious() {
        if (it2.hasPrevious())
          return it2.previous();
        if (it1.hasPrevious())
          return it1.previous();
        return null;
      }
    };
  }

  public static final <T, E> ListIterator<Pair<T, E>>
      disjointUnion(final ListIterator<T> it1, final ListIterator<E> it2, final int i) {
    return new SimpleListIterator<Pair<T, E>>(i) {

      protected final Pair<T, E> createNext() {
        if (it1.hasNext())
          return new Pair<T, E>(it1.next(), null);
        if (it2.hasNext())
          return new Pair<T, E>(null, it2.next());
        return null;
      }

      protected final Pair<T, E> createPrevious() {
        if (it2.hasPrevious())
          return new Pair<T, E>(null, it2.previous());
        if (it1.hasPrevious())
          return new Pair<T, E>(it1.previous(), null);
        return null;
      }
    };
  }

  public static final <T, E> ListIterator<Pair<T, E>>
      cartesianProduct(final ListIterator<T> it1, final ListIterator<E> it2, final int i) {
    return new SimpleListIterator<Pair<T, E>>(true) {

      private T t = it1.hasNext() ? it1.next() : null;
      {
        createFirst(i);
      }

      protected final Pair<T, E> createNext() {
        if (t != null && it2.hasNext())
          return new Pair<T, E>(t, it2.next());
        else if (it1.hasNext()) {
          t = it1.next();
          while (it2.hasPrevious())
            it2.previous();
          if (it2.hasNext())
            return new Pair<T, E>(t, it2.next());
        }
        return null;
      }

      protected final Pair<T, E> createPrevious() {
        if (t != null && it2.hasPrevious())
          return new Pair<T, E>(t, it2.previous());
        else if (it1.hasPrevious()) {
          t = it1.previous();
          while (it2.hasNext())
            it2.next();
          if (it2.hasPrevious())
            return new Pair<T, E>(t, it2.previous());
        }
        return null;
      }
    };
  }

  public static final <E> Iterable<Pair<E, E>> upperCartesianDiagonal(final Iterable<E> it) {
    return new Iterable<Pair<E, E>>() {

      @Override
      public final Iterator<Pair<E, E>> iterator() {
        if (!it.iterator().hasNext())
          return empty();
//          return Iterators.emptyIterator();
        return new UnmodifiableIterator<Pair<E, E>>() {

          private final Iterator<E> it1   = it.iterator();
          private Iterator<E>       it2   = it.iterator();
          private E                 e1    = it1.next();
          private int               skip2 = 0;

          @Override
          public final boolean hasNext() {
            return e1 != null;
          }

          @Override
          public final Pair<E, E> next() {
            if (!it2.hasNext()) {
              e1 = it1.next();
              it2 = it.iterator();
              ++skip2;
              for (int i = 0; i < skip2; i++)
                it2.next();
            }
            final Pair<E, E> p = Pair.of(e1, it2.next());
            if (!it2.hasNext() && !it1.hasNext())
              e1 = null;
            return p;
          }
        };
      }
    };
  }

  public static final <E> Iterable<Pair<E, E>> upperCartesianDiagonalStrict(final Iterable<E> it) {
    return new Iterable<Pair<E, E>>() {

      @Override
      public final Iterator<Pair<E, E>> iterator() {
        Iterator<E> _it = it.iterator();
        if (!_it.hasNext())
          return empty();
//          return Iterators.emptyIterator();
        _it.next();
        if (!_it.hasNext())
          return empty();
//          return Iterators.emptyIterator();
        return new UnmodifiableIterator<Pair<E, E>>() {

          private final Iterator<E> it1   = it.iterator();
          private Iterator<E>       it2   = it.iterator();
          private E                 e1    = it1.next();
          private int               skip2 = 1;
          {
            it2.next();
          }

          @Override
          public final boolean hasNext() {
            return e1 != null;
          }

          @Override
          public final Pair<E, E> next() {
            final Pair<E, E> p = Pair.of(e1, it2.next());
            if (!it2.hasNext()) {
              if (it1.hasNext()) {
                e1 = it1.next();
                if (!it1.hasNext())
                  e1 = null;
                else {
                  it2 = it.iterator();
                  ++skip2;
                  for (int i = 0; i < skip2; i++)
                    it2.next();
                }
              } else {
                e1 = null;
              }
            }
            return p;
          }
        };
      }
    };
  }
}
