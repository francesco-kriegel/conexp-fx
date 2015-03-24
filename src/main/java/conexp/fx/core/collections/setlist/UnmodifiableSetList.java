package conexp.fx.core.collections.setlist;

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
