package conexp.fx.core.collections;

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
