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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiPredicate;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.ListIterators;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.collections.SimpleListIterator;
import conexp.fx.core.collections.setlist.AbstractSetList;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.collections.setlist.SetLists;

public abstract class AbstractRelation<R, C> implements Relation<R, C> {

  public static <R, C> AbstractRelation<R, C>
      fromPredicate(final SetList<R> rowHeads, final SetList<C> colHeads, final BiPredicate<R, C> predicate) {
    return new AbstractRelation<R, C>(rowHeads, colHeads, false) {

      @Override
      public final boolean contains(final Object o1, final Object o2) {
        try {
          return predicate.test((R) o1, (C) o2);
        } catch (ClassCastException e) {
          return false;
        }
      }

    };
  }

  public static <R> AbstractRelation<R, R> fromPredicate(final SetList<R> heads, final BiPredicate<R, R> predicate) {
    return new AbstractRelation<R, R>(heads, heads, true) {

      @Override
      public final boolean contains(final Object o1, final Object o2) {
        try {
          return predicate.test((R) o1, (R) o2);
        } catch (ClassCastException e) {
          return false;
        }
      }

    };
  }

  protected final boolean homogen;
  protected SetList<R>    rowHeads;
  protected SetList<C>    colHeads;

  @SuppressWarnings("unchecked")
  protected AbstractRelation(final SetList<R> rowHeads, final SetList<C> colHeads, final boolean homogen) {
    this(homogen);
    if (homogen) {
      if (!rowHeads.equals(colHeads))
        throw new NoHomogenRelationException();
      this.rowHeads = rowHeads;
      this.colHeads = (SetList<C>) this.rowHeads;
    } else {
      this.rowHeads = rowHeads;
      this.colHeads = colHeads;
    }
  }

  protected AbstractRelation(final boolean homogen) {
    super();
    this.homogen = homogen;
  }

  public SetList<R> rowHeads() {
    return rowHeads;
  }

  public SetList<C> colHeads() {
    return colHeads;
  }

  public abstract boolean contains(Object o1, Object o2);

  public boolean containsAll(final Relation<?, ?> r) {
    for (Pair<?, ?> p : r)
      if (!contains(p.x(), p.y()))
        return false;
    return true;
  }

  public Set<C> row(final Object o) {
    return rowAnd(o);
  }

  public Set<R> col(final Object o) {
    return colAnd(o);
  }

  public final Set<C> rowAnd(final Object... rows) {
    return rowAnd(Arrays.asList(rows));
  }

  public final Set<R> colAnd(final Object... cols) {
    return colAnd(Arrays.asList(cols));
  }

  public Set<C> rowAnd(final Collection<?> rows) {
    return Sets.filter(colHeads(), new Predicate<C>() {

      public final boolean apply(C col) {
        for (Object row : rows)
          if (!AbstractRelation.this.contains(row, col))
            return false;
        return true;
      }
    });
  }

  public Set<R> colAnd(final Collection<?> cols) {
    return Sets.filter(rowHeads(), new Predicate<R>() {

      public final boolean apply(final R row) {
        for (Object col : cols)
          if (!AbstractRelation.this.contains(row, col))
            return false;
        return true;
      }
    });
  }

  public Relation<R, C> subRelation(final Collection<?> rows, final Collection<?> cols) {
    return new AbstractRelation<R, C>(
        SetLists.intersection(rowHeads, rows),
        SetLists.intersection(colHeads, cols),
        false) {

      public final boolean contains(final Object o1, final Object o2) {
        return rowHeads().contains(o1) && colHeads().contains(o2) && AbstractRelation.this.contains(o1, o2);
      }
    };
  }

  public Relation<R, C> filter(
      final Predicate<? super R> rowPredicate,
      final Predicate<? super C> colPredicate,
      final Predicate<Pair<R, C>> relationPredicate) {
    return new AbstractRelation<R, C>(rowHeads.filter(rowPredicate), colHeads.filter(colPredicate), false) {

      @SuppressWarnings("unchecked")
      public final boolean contains(final Object o1, final Object o2) {
        return rowHeads().contains(o1) && colHeads().contains(o2) && AbstractRelation.this.contains(o1, o2)
            && relationPredicate.apply(new Pair<R, C>((R) o1, (C) o2));
      }
    };
  }

  public boolean equals(final Object o) {
    return this == o
        || (o instanceof Relation && size() == ((Relation<?, ?>) o).size() && containsAll((Relation<?, ?>) o));
  }

  public final boolean smallerEq(final Relation<R, C> r) {
    return r.containsAll(this);
  }

  public final boolean smaller(final Relation<R, C> r) {
    return size() < r.size() && smallerEq(r);
  }

  public final boolean greaterEq(final Relation<R, C> r) {
    return r.smallerEq(this);
  }

  public final boolean greater(final Relation<R, C> r) {
    return r.smaller(this);
  }

  public final boolean uncomparable(final Relation<R, C> r) {
    return !smallerEq(r) && !greaterEq(r);
  }

  public final int compareTo(final Relation<R, C> r) {
    if (equals(r))
      return 0;
    if (smallerEq(r))
      return -1;
    if (greaterEq(r))
      return 1;
    return Integer.MAX_VALUE;
  }

  public Iterator<Pair<R, C>> iterator() {
    return Iterators
        .filter(
            ListIterators.<R, C> cartesianProduct(rowHeads().listIterator(), colHeads().listIterator(), 0),
            new Predicate<Pair<R, C>>() {

              public final boolean apply(final Pair<R, C> p) {
                return contains(p.x(), p.y());
              }
            });
  }

  public int size() {
    int size = 0;
    for (R row : rowHeads())
      for (C col : colHeads())
        if (contains(row, col))
          size++;
    return size;
  }

  public boolean isFull() {
    for (R row : rowHeads())
      for (C col : colHeads())
        if (!contains(row, col))
          return false;
    return true;
  }

  public boolean isEmpty() {
    for (R row : rowHeads())
      for (C col : colHeads())
        if (contains(row, col))
          return false;
    return true;
  }

  public MatrixRelation<R, C> clone() {
    final MatrixRelation<R, C> clone = new MatrixRelation<R, C>(rowHeads(), colHeads(), homogen);
    clone.addAllFast(this);
    return clone;
  }

  public boolean[][] toArray() {
    final boolean[][] a = new boolean[rowHeads().size()][colHeads().size()];
    for (int i = 0; i < rowHeads().size(); i++)
      for (int j = 0; j < colHeads().size(); j++)
        a[i][j] = contains(rowHeads().get(i), colHeads().get(j));
    return a;
  }

  private static final int colspan = 8;

  public String toString() {
    final StringBuilder s = new StringBuilder();
    s.append(getClass().getName() + "@" + hashCode() + "\r\n");
    s.append(rowHeads().size() + " domain elements: " + rowHeads() + "\r\n");
    s.append(colHeads().size() + " codomain elements. " + colHeads() + "\r\n");
    String spaces = "";
    while (spaces.length() < colspan)
      spaces += " ";
    s.append(spaces);
    spaces = spaces.substring(1);
    String c;
    for (C col : colHeads()) {
      c = col.toString().substring(0, Math.min(colspan, col.toString().length()));
      while (c.length() < colspan)
        c += " ";
      s.append("\t" + c);
    }
    s.append("\r\n");
    String r;
    for (R row : rowHeads()) {
      r = row.toString().substring(0, Math.min(colspan, row.toString().length()));
      while (r.length() < colspan)
        r += " ";
      s.append(r);
      for (C col : colHeads())
        if (contains(row, col))
          s.append("\tX" + spaces);
        else
          s.append("\t." + spaces);
      s.append("\r\n");
    }
    return s.toString();
  }

  public void empty() {
    throw new UnsupportedOperationException();
  }

  public void fill() {
    throw new UnsupportedOperationException();
  }

  public void dispose() {
    throw new UnsupportedOperationException();
  }

  public boolean add(final R row, final C col) {
    throw new UnsupportedOperationException();
  }

  public boolean addFast(final Object o1, final Object o2) {
    throw new UnsupportedOperationException();
  }

  public boolean addAll(final Relation<? extends R, ? extends C> r) {
    throw new UnsupportedOperationException();
  }

  public boolean addAllFast(final Relation<?, ?> r) {
    throw new UnsupportedOperationException();
  }

  public boolean remove(final Object o1, final Object o2) {
    throw new UnsupportedOperationException();
  }

  public boolean removeAll(final Relation<?, ?> r) {
    throw new UnsupportedOperationException();
  }

  public boolean retainAll(final Relation<?, ?> r) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isHomogen() {
    return homogen;
  }

  protected final void checkHomogen() throws NoHomogenRelationException {
    if (!isHomogen())
      throw new NoHomogenRelationException();
  }

  public MatrixRelation<R, R> neighborhood() {
    checkHomogen();
    return clone().neighborhood();
  }

  public MatrixRelation<R, R> order() {
    checkHomogen();
    return clone().order();
  }

  public SetList<Set<R>> equivalenceClasses() {
    checkHomogen();
    return new AbstractSetList<Set<R>>() {

      @Override
      public final ListIterator<Set<R>> listIterator(final int i) {
        return new SimpleListIterator<Set<R>>(true) {

          private final HashSet<R> available = new HashSet<R>(rowHeads());

          {
            createFirst(i);
          }

          @Override
          protected final Set<R> createNext() {
            try {
              final R head = Collections3.firstElement(available);
              final Set<R> eq = new HashSet<R>(col(head));
              available.removeAll(eq);
              return eq;
            } catch (NoSuchElementException e) {
              return null;
            }
          }

          @Override
          protected final Set<R> createPrevious() {
            // TODO Auto-generated method stub
            return null;
          }
        };
      }
    };
  }
}
