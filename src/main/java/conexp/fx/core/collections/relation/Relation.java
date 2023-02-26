/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.collections.relation;

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
import java.util.Set;

import com.google.common.base.Predicate;

import conexp.fx.core.collections.Pair;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.math.PartialComparable;

public interface Relation<R, C> extends Iterable<Pair<R, C>>, PartialComparable<Relation<R, C>>, Cloneable {

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

  default public double density() {
    return ((double) size()) / ((double) (rowHeads().size() * colHeads().size()));
  }

  public boolean isEmpty();

  public boolean isFull();

  public void empty();

  public void fill();

  public void dispose();

  public MatrixRelation<R, C> clone();

  public boolean[][] toArray();

  public static final class NoHomogenRelationException extends RuntimeException {

    private static final long serialVersionUID = -3920949710978936537L;

    public NoHomogenRelationException() {
      super();
    }
  }

  public boolean isHomogen();

  public MatrixRelation<R, R> neighborhood() throws NoHomogenRelationException;

  public MatrixRelation<R, R> order() throws NoHomogenRelationException;

  public SetList<Set<R>> equivalenceClasses() throws NoHomogenRelationException;
}
