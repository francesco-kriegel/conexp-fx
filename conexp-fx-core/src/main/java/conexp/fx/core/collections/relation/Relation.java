/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.collections.relation;

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


import java.util.Collection;
import java.util.Set;

import com.google.common.base.Predicate;

import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.math.PartialComparable;

public interface Relation<R, C> extends Iterable<Pair<R, C>>, PartialComparable<Relation<R, C>>, Cloneable
{

  public SetList<R> rowHeads();

  public SetList<C> colHeads();

  public boolean add(R row, C col);

  public boolean addFast(Object o1, Object o2);

  public boolean addAll(Relation<? extends R, ? extends C> r);

  public boolean addAllFast(Relation<?, ?> r);

  public boolean contains(Object o1, Object o2);

  public boolean containsAll(Relation<?, ?> r);

  public boolean remove(Object o1, Object o2);

  public boolean removeAll(Relation<?, ?> r);

  public boolean retainAll(Relation<?, ?> r);

  public Set<C> row(Object o);

  public Set<R> col(Object o);

  public Set<C> rowAnd(Object... o);

  public Set<R> colAnd(Object... o);

  public Set<C> rowAnd(Collection<?> c);

  public Set<R> colAnd(Collection<?> c);

  public Relation<R, C> subRelation(Collection<?> rowHeads, Collection<?> colHeads);

  public Relation<R, C> filter(
      Predicate<? super R> rowPredicate,
      Predicate<? super C> colPredicate,
      Predicate<Pair<R, C>> relationPredicate);

  public int size();

  public boolean isEmpty();

  public boolean isFull();

  public void empty();

  public void fill();

  public void dispose();

  public MatrixRelation<R, C> clone();

  public boolean[][] toArray();

  public static final class NoHomogenRelationException extends RuntimeException
  {

    private static final long serialVersionUID = -3920949710978936537L;

    public NoHomogenRelationException()
    {
      super();
    }
  }

  public boolean isHomogen();

  public MatrixRelation<R, R> neighborhood() throws NoHomogenRelationException;

  public MatrixRelation<R, R> order() throws NoHomogenRelationException;

  public SetList<Set<R>> equivalenceClasses() throws NoHomogenRelationException;
}
