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
