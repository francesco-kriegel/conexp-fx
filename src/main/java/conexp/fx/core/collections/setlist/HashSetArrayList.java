/*
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Spliterator;

public class HashSetArrayList<E> extends AbstractSetList<E> {

  private final HashSet<E>   s = new HashSet<E>();
  private final ArrayList<E> l = new ArrayList<E>();

  public HashSetArrayList() {
    super();
  }

  public HashSetArrayList(final Collection<? extends E> c) {
    super();
    addAll(c);
  }

  private final void checkIndex(final int i) throws IndexOutOfBoundsException {
    if (i < 0 || i > size())
      throw new IndexOutOfBoundsException();
  }

  public boolean add(final E e) {
    return s.add(e) && l.add(e);
  }

  public void add(final int i, final E e) {
    // checkIndex(i);
    // if (s.add(e))
    // l.add(i, e);
    // else
    if (!_add(i, e))
      throw new IllegalArgumentException();
  }

  public boolean _add(final int i, final E e) {
    checkIndex(i);
    if (s.add(e)) {
      l.add(i, e);
      return true;
    }
    return false;
  }

  public boolean addAll(final Collection<? extends E> c) {
    // return c.stream().map(this::add).reduce(false, Boolean::logicalOr);
    boolean changed = false;
    for (E e : c)
      changed |= s.add(e) && l.add(e);
    return changed;
  }

  public boolean addAll(final int i, final Collection<? extends E> c) {
    if (i < 0 || i > size())
      throw new IndexOutOfBoundsException();
    // final AtomicInteger j = new AtomicInteger(i);
    // return c.stream().map(e -> this._add(j.getAndIncrement(),
    // e)).reduce(false, Boolean::logicalOr);
    boolean changed = false;
    int j = i;
    for (E e : c)
      if (s.add(e)) {
        l.add(j++, e);
        changed = true;
      }
    return changed;
  }

  public E set(final int i, final E e) {
    if (i < 0 || i > size())
      throw new IndexOutOfBoundsException();
    if (s.add(e)) {
      final E x = l.set(i, e);
      s.remove(x);
      return x;
    }
    return e;
  }

  public boolean remove(final Object o) {
    return l.remove(o) && s.remove(o);
  }

  public E remove(final int i) {
    final E e = l.remove(i);
    s.remove(e);
    return e;
  }

  public boolean removeAll(final Collection<?> c) {
    return l.removeAll(c) && s.removeAll(c);
  }

  public boolean retainAll(final Collection<?> c) {
    return l.retainAll(c) && s.retainAll(c);
  }

  public boolean contains(final Object o) {
    return s.contains(o);
  }

  public boolean containsAll(final Collection<?> c) {
    return s.containsAll(c);
  }

  public E get(final int i) {
    return l.get(i);
  }

  public int indexOf(final Object o) {
    return l.indexOf(o);
  }

  public ListIterator<E> listIterator(final int i) {
    return new ListIterator<E>() {

      private final ListIterator<E> it      = l.listIterator(i);
      private E                     pointer = null;
      private boolean               illegal = true;

      public final boolean hasNext() {
        return it.hasNext();
      }

      public final E next() {
        pointer = it.next();
        illegal = false;
        return pointer;
      }

      public final boolean hasPrevious() {
        return it.hasPrevious();
      }

      public final E previous() {
        pointer = it.previous();
        illegal = false;
        return pointer;
      }

      public final int nextIndex() {
        return it.nextIndex();
      }

      public final int previousIndex() {
        return it.previousIndex();
      }

      public final void remove() {
        if (illegal)
          throw new IllegalStateException();
        s.remove(pointer);
        it.remove();
        illegal = true;
        pointer = null;
      }

      public final void set(final E e) {
        if (illegal)
          throw new IllegalStateException();
        if (pointer.equals(e))
          return;
        if (!s.add(e))
          throw new IllegalArgumentException();
        s.remove(pointer);
        it.set(e);
        pointer = e;
      }

      public final void add(final E e) {
        if (!s.add(e))
          throw new IllegalArgumentException();
        it.add(e);
        illegal = true;
        pointer = null;
      }
    };
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void sort(Comparator<? super E> c) {
    Object[] a = this.toArray();
    s.clear();
    Arrays.sort(a, (Comparator) c);
    ListIterator<E> i = this.listIterator();
    for (Object e : a) {
      i.next();
      i.set((E) e);
    }
  }

  @Override
  public Spliterator<E> spliterator() {
    return l.spliterator();
  }

  public boolean isEmpty() {
    return s.isEmpty();
  }

  public int size() {
    return l.size();
  }

  public void clear() {
    s.clear();
    l.clear();
  }

  @Override
  public int hashCode() {
    return s.hashCode() + l.hashCode();
  }

  public Object[] toArray() {
    return l.toArray();
  }

  public <T> T[] toArray(final T[] a) {
    return l.toArray(a);
  }
}
