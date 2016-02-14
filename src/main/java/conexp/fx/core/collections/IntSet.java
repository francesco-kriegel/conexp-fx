package conexp.fx.core.collections;

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
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.UnmodifiableIterator;

public final class IntSet extends BitSet {

  public final Set<Integer> toSet() {
    return new Set<Integer>() {

      @Override
      public int size() {
        return IntSet.this.size();
      }

      @Override
      public boolean isEmpty() {
        return IntSet.this.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
        return IntSet.this.contains(o);
      }

      @Override
      public Iterator<Integer> iterator() {
        return IntSet.this.iterator();
      }

      @Override
      public Object[] toArray() {
        throw new UnsupportedOperationException();
      }

      @Override
      public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean add(Integer e) {
        return IntSet.this.add(e);
      }

      @Override
      public boolean remove(Object o) {
        return IntSet.this.remove(o);
      }

      @Override
      public boolean containsAll(Collection<?> c) {
        return IntSet.this.containsAll(c);
      }

      @Override
      public boolean addAll(Collection<? extends Integer> c) {
        return IntSet.this.addAll(c);
      }

      @Override
      public boolean retainAll(Collection<?> c) {
        return IntSet.this.retainAll(c);
      }

      @Override
      public boolean removeAll(Collection<?> c) {
        return IntSet.this.removeAll(c);
      }

      @Override
      public void clear() {
        IntSet.this.clear();
      }

      @Override
      public Stream<Integer> stream() {
        return IntSet.this.stream().mapToObj(__ -> __);
      }

      @Override
      public Stream<Integer> parallelStream() {
        return stream();
      }

    };
  }

  private static final long serialVersionUID = 1139942237055133038L;

  public IntSet() {
    super();
  }

  public IntSet(final int nbits, final boolean initialValue) {
    super(nbits);
    if (initialValue)
      set(0, nbits);
  }

  public IntSet(final Collection<? extends Integer> c) {
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
}
