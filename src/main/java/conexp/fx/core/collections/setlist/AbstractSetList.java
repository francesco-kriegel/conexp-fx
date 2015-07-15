/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.collections.setlist;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.lang.reflect.Array;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;

import conexp.fx.core.math.Isomorphism;

public abstract class AbstractSetList<E> implements SetList<E> {

  public AbstractSetList() {
    super();
  }

  public AbstractSetList(final Collection<? extends E> c) {
    super();
    addAll(c);
  }

  public boolean add(final E e) {
    try {
      add(size(), e);
      return true;
    } catch (IllegalArgumentException x) {
      return false;
    }
  }

  public void add(final int i, final E e) {
    listIterator(i).add(e);
  }

  public boolean addAll(final Collection<? extends E> c) {
    return addAll(size(), c);
  }

  public boolean addAll(final int i, final Collection<? extends E> c) {
    boolean changed = false;
    final ListIterator<E> it = listIterator(i);
    for (E e : c)
      try {
        it.add(e);
        changed = true;
      } catch (IllegalArgumentException x) {}
    return changed;
  }

  public boolean set(final Object o, final E e) {
    if ((o == null && e == null) || o.equals(e))
      return false;
    final ListIterator<E> it = listIterator();
    while (it.hasNext())
      if (it.next().equals(o)) {
        it.set(e);
        return true;
      }
    return false;
  }

  public E set(final int i, final E e) {
    final ListIterator<E> it = listIterator(i);
    if (it.hasNext()) {
      final E oldElement = it.next();
      it.set(e);
      return oldElement;
    }
    throw new IndexOutOfBoundsException();
  }

  public E remove(int i) {
    final ListIterator<E> it = listIterator(i);
    if (it.hasNext()) {
      final E e = it.next();
      it.remove();
      return e;
    }
    throw new IndexOutOfBoundsException();
  }

  public boolean remove(final Object o) {
    final Iterator<E> it = iterator();
    while (it.hasNext())
      if (it.next().equals(o)) {
        it.remove();
        return true;
      }
    return false;
  }

  public boolean removeAll(final Collection<?> c) {
    boolean changed = false;
    final Iterator<E> it = iterator();
    while (it.hasNext())
      if (c.contains(it.next())) {
        it.remove();
        changed = true;
      }
    return changed;
  }

  public boolean retainAll(final Collection<?> c) {
    boolean changed = false;
    final Iterator<E> it = iterator();
    while (it.hasNext())
      if (!c.contains(it.next())) {
        it.remove();
        changed = true;
      }
    return changed;
  }

  public boolean contains(final Object o) {
    return indexOf(o) != -1;
  }

  public boolean containsAll(final Collection<?> c) {
    for (Object o : c)
      if (!contains(o))
        return false;
    return true;
  }

  public E get(final int i) {
    return listIterator(i).next();
  }

  public final Collection<E> getAll(final Collection<Integer> c, final boolean includeNull) {
    if (includeNull)
      return Collections2.transform(c, new Function<Integer, E>() {

        public final E apply(final Integer i) {
          return get(i);
        }
      });
    else
      return Collections2.filter(Collections2.transform(c, new Function<Integer, E>() {

        public final E apply(final Integer i) {
          return get(i);
        }
      }), Predicates.notNull());
  }

  @Override
  public Isomorphism<E, Integer> index() {
    return new Isomorphism<E, Integer>() {

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

  public int indexOf(final Object o) {
    int i = 0;
    for (E e : this) {
      if (e.equals(o))
        return i;
      i++;
    }
    return -1;
  }

  public final Collection<Integer> indicesOf(final Collection<?> c, final boolean includeMinusOne) {
    if (includeMinusOne)
      return Collections2.transform(c, new Function<Object, Integer>() {

        public final Integer apply(final Object o) {
          return indexOf(o);
        }
      });
    else
      return Collections2.filter(Collections2.transform(c, new Function<Object, Integer>() {

        public final Integer apply(final Object o) {
          return indexOf(o);
        }
      }), Predicates.not(Predicates.equalTo(-1)));
  }

  @Deprecated
  public final int lastIndexOf(final Object o) {
    return indexOf(o);
  }

  public final SetList<E> subList(final int from, final int to) {
    return filter(new Predicate<E>() {

      public final boolean apply(final E e) {
        final int i = indexOf(e);
        return from <= i && i < to;
      }
    });
  }

  public final BitSet subBitSet(final Collection<?> c) {
    final BitSet b = new BitSet(size());
    for (int i : indicesOf(c, true))
      b.flip(i);
    return b;
  }

  public final SetList<E> filter(final Predicate<? super E> p) {
    return SetLists.filter(this, p);
  }

  public final Iterator<E> iterator() {
    return listIterator();
  }

  public final ListIterator<E> listIterator() {
    return listIterator(0);
  }

  public abstract ListIterator<E> listIterator(final int i);

  public boolean isEmpty() {
    return iterator().hasNext();
  }

  public int size() {
    return Iterators.size(iterator());
  }

  public void clear() {
    final Iterator<E> it = iterator();
    while (it.hasNext()) {
      it.next();
      it.remove();
    }
  }

  public final HashSetArrayList<E> clone() {
    return new HashSetArrayList<E>(this);
  }

  public final boolean equals(final Object o) {
    return o != null
        && (this == o || (o instanceof SetList && size() == ((SetList<?>) o).size() && containsAll((SetList<?>) o)));
  }

  public int hashCode() {
    int hashCode = 1;
    for (E e : this)
      hashCode = 23 * hashCode + (e == null ? 0 : e.hashCode());
    return hashCode;
  }

  public Object[] toArray() {
    final Object[] a = new Object[size()];
    int i = 0;
    for (E e : this)
      a[i++] = e;
    return a;
  }

  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    if (a == null)
      throw new NullPointerException();
    try {
      if (a.length < size())
        a = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
      int i = 0;
      for (E e : this)
        a[i++] = (T) e;
      if (a.length > size())
        a[size()] = null;
      return a;
    } catch (ClassCastException x) {
      throw new ArrayStoreException();
    }
  }

  public final String toString() {
    final StringBuilder s = new StringBuilder();
    s.append("{");
    final Iterator<E> it = iterator();
    if (it.hasNext())
      s.append(it.next().toString());
    while (it.hasNext())
      s.append(", " + it.next().toString());
    s.append("}");
    return s.toString();
  }
}
