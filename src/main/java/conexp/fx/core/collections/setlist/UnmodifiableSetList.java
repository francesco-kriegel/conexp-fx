package conexp.fx.core.collections.setlist;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.util.Collection;

public abstract class UnmodifiableSetList<E> extends AbstractSetList<E> {

  public final boolean add(final E e) {
    throw new UnsupportedOperationException();
  }

  public final void add(final int i, final E e) {
    throw new UnsupportedOperationException();
  }

  public final boolean addAll(final Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  public final boolean addAll(final int i, final Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  public final void clear() {
    throw new UnsupportedOperationException();
  }

  public final boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  public final E remove(final int i) {
    throw new UnsupportedOperationException();
  }

  public final boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  public final boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  public final E set(final int i, final E e) {
    throw new UnsupportedOperationException();
  }

  public final boolean set(final Object o, final E e) {
    throw new UnsupportedOperationException();
  }
}
