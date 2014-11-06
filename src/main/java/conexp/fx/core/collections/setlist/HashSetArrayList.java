/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.collections.setlist;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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

import java.util.ArrayList;
import java.util.Collection;
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

  public boolean add(final E e) {
    return s.add(e) && l.add(e);
  }

  public void add(final int i, final E e) {
    if (i < 0 || i > size())
      throw new IndexOutOfBoundsException();
    if (s.add(e))
      l.add(i, e);
    else
      throw new IllegalArgumentException();
  }

  public boolean addAll(final Collection<? extends E> c) {
    boolean changed = false;
    for (E e : c)
      changed |= s.add(e) && l.add(e);
    return changed;
  }

  public boolean addAll(final int i, final Collection<? extends E> c) {
    if (i < 0 || i > size())
      throw new IndexOutOfBoundsException();
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
    return s.remove(o) && l.remove(o);
  }

  public E remove(final int i) {
    final E e = l.remove(i);
    s.remove(e);
    return e;
  }

  public boolean removeAll(final Collection<?> c) {
    return s.removeAll(c) && l.removeAll(c);
  }

  public boolean retainAll(final Collection<?> c) {
    return s.retainAll(c) && l.retainAll(c);
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
  public Spliterator<E> spliterator() {
    // TODO Auto-generated method stub
    return null;
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

  public Object[] toArray() {
    return l.toArray();
  }

  public <T> T[] toArray(final T[] a) {
    return l.toArray(a);
  }
}
