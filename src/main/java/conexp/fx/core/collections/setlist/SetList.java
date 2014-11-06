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

import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

import com.google.common.base.Predicate;

import conexp.fx.core.collections.IntegerSet;
import conexp.fx.core.math.Isomorphism;

public interface SetList<E> extends Set<E>, List<E>, Collection<E>, Iterable<E>, Cloneable {

  public boolean set(Object o, E e);

  public Collection<E> getAll(Collection<Integer> c, boolean includeNull);

  public Collection<Integer> indicesOf(Collection<?> c, boolean includeMinusOne);

  @Deprecated
  public int lastIndexOf(Object o);

  public SetList<E> filter(Predicate<? super E> p);

  public SetList<E> subList(int from, int to);

  public BitSet subBitSet(Collection<?> c);

  public HashSetArrayList<E> clone();

  public Isomorphism<E, Integer> index();

  @Override
  default Spliterator<E> spliterator() {
    return Spliterators.spliterator(this, Spliterator.ORDERED);
  }
}
