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


import java.util.NoSuchElementException;

import com.google.common.collect.UnmodifiableListIterator;

public abstract class SimpleListIterator<E> extends UnmodifiableListIterator<E> {

  private int i = 0;
  private E   n = null;
  private E   p = null;

  public SimpleListIterator() {
    this(0);
  }

  public SimpleListIterator(final int i) {
    super();
    createFirst(i);
  }

  public SimpleListIterator(final boolean dontCreateFirst) {
    super();
    if (!dontCreateFirst)
      createFirst(0);
  }

  protected final void createFirst(final int i) {
    int j = 0;
    n = createNext();
    while (j++ < i && hasNext())
      next();
  }

  protected abstract E createNext();

  protected abstract E createPrevious();

  public final boolean hasNext() {
    return n != null;
  }

  public final boolean hasPrevious() {
    return p != null;
  }

  public final E next() {
    if (n == null)
      throw new NoSuchElementException();
    ++i;
    p = n;
    n = createNext();
    return p;
  }

  public final E previous() {
    if (p == null)
      throw new NoSuchElementException();
    --i;
    n = p;
    p = createPrevious();
    return n;
  }

  public final int nextIndex() {
    return i;
  }

  public final int previousIndex() {
    return i - 1;
  }
}
