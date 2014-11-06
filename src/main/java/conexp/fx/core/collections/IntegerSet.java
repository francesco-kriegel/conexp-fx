package conexp.fx.core.collections;

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

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.UnmodifiableIterator;

public final class IntegerSet extends BitSet { // implements Set<Integer> {

  private static final long serialVersionUID = 1139942237055133038L;

  public IntegerSet() {
    super();
  }

  public IntegerSet(final int nbits, final boolean initialValue) {
    super(nbits);
    if (initialValue)
      set(0, nbits);
  }

  public IntegerSet(final Collection<? extends Integer> c) {
    super();
    addAll(c);
  }

  @Override
  public int size() {
    return super.cardinality();
  }

  public final Iterator<Integer> iterator() {
    return new UnmodifiableIterator<Integer>() {

      private int i = 0;

      public final boolean hasNext() {
        return nextSetBit(i) != -1;
      }

      public final Integer next() {
        i = nextSetBit(i);
        return i;
      }
    };
  }

  public final boolean contains(final Object o) {
    return get((int) o);
  }

  public final boolean add(final Integer i) {
    if (contains(i))
      return false;
    set(i);
    return true;
  }

  public final boolean remove(final Object o) {
    if (!contains(o))
      return false;
    clear((int) o);
    return true;
  }

  public final boolean containsAll(final Collection<?> c) {
    for (Object o : c)
      if (!contains(o))
        return false;
    return true;
  }

  public final boolean addAll(final Collection<? extends Integer> c) {
    boolean changed = false;
    for (int i : c)
      changed |= add(i);
    return changed;
  }

  public final boolean removeAll(final Collection<?> c) {
    boolean changed = false;
    for (Object o : c)
      changed |= remove(o);
    return changed;
  }

  public final boolean retainAll(final Collection<?> c) {
    boolean changed = false;
    for (int bit = nextSetBit(0); bit != -1; bit = nextSetBit(bit))
      if (!c.contains(bit)) {
        changed = true;
        clear(bit);
      }
    return changed;
  }

  public final Object[] toArray() {
    throw new UnsupportedOperationException();
  }

  public final <T> T[] toArray(T[] a) {
    throw new UnsupportedOperationException();
  }
}
