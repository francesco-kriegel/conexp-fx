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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.ujmp.core.booleanmatrix.BooleanMatrix;
import org.ujmp.core.booleanmatrix.BooleanMatrix2D;
import org.ujmp.core.calculation.Calculation.Ret;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import conexp.fx.core.collections.BitSetFX;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.ListIterators;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.math.BooleanMatrices;

public class MatrixRelation<R, C> extends AbstractRelation<R, C> {

  private final class RowHeads extends HashSetArrayList<R> {

    private RowHeads(final Collection<? extends R> c) {
      super(c);
    }

    public final boolean add(final R row) {
      final boolean wasEmpty = isEmpty();
      if (super.add(row)) {
        append(wasEmpty, 1);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_ADDED, row, null));
        return true;
      }
      return false;
    }

    public final void add(final int i, final R row) {
      final int size0 = size();
      super.add(i, row);
      if (size0 != size()) {
        insert(i, size0, 1);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_ADDED, row, null));
      }
    }

    public final boolean addAll(final Collection<? extends R> c) {
      final int size0 = size();
      @SuppressWarnings("unchecked")
      final Set<R> changes = new HashSet<R>(Collections3.<R> difference((Collection<R>) c, this));
      if (super.addAll(c)) {
        append(size0 == 0, size() - size0);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_ADDED, changes, null, null));
        return true;
      }
      return false;
    }

    public final boolean addAll(final int i, final Collection<? extends R> c) {
      final int size0 = size();
      @SuppressWarnings("unchecked")
      final Set<R> changes = new HashSet<R>(Collections3.<R> difference((Collection<R>) c, this));
      if (super.addAll(i, c)) {
        insert(i, size0, size() - size0);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_ADDED, changes, null, null));
        return true;
      }
      return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean set(final Object o, final R row) {
      if (super.set(o, row)) {
        push(new RelationEvent<R, C>(RelationEvent.ROWS_SET, new Pair<R, R>((R) o, row), null));
        return true;
      }
      return false;
    }

    @Override
    public final R set(final int i, final R row) {
      final R row0 = super.set(i, row);
      push(new RelationEvent<R, C>(RelationEvent.ROWS_SET, new Pair<R, R>(row0, row), null));
      return row0;
    }

    @SuppressWarnings("unchecked")
    public final boolean remove(final Object o) {
      final int i = indexOf(o);
      if (i == -1)
        return false;
      super.remove(o);
      if (isEmpty())
        matrix = BooleanMatrix2D.Factory.zeros(0, 0);
      else
        matrix = (BooleanMatrix) matrix.deleteRows(Ret.NEW, i);
      push(new RelationEvent<R, C>(RelationEvent.ROWS_REMOVED, (R) o, null));
      return true;
    }

    public final R remove(final int i) {
      final R row = super.remove(i);
      if (row != null) {
        if (isEmpty())
          matrix = BooleanMatrix2D.Factory.zeros(0, 0);
        else
          matrix = (BooleanMatrix) matrix.deleteRows(Ret.NEW, i);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_REMOVED, row, null));
      }
      return row;
    }

    public final boolean removeAll(final Collection<?> c) {
      final Collection<Integer> i = indicesOf(c, false);
      final Set<R> changes = new HashSet<R>();
      for (R row : this)
        if (c.contains(row))
          changes.add(row);
      if (super.removeAll(c)) {
        if (isEmpty())
          matrix = BooleanMatrix2D.Factory.zeros(0, 0);
        else
          matrix = (BooleanMatrix) matrix.deleteRows(Ret.NEW, i);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_REMOVED, changes, null, null));
        return true;
      }
      return false;
    }

    public final boolean retainAll(final Collection<?> c) {
      final Collection<Integer> i = indicesOf(c, false);
      final Set<R> changes = new HashSet<R>();
      for (R row : this)
        if (!c.contains(row))
          changes.add(row);
      if (super.retainAll(c)) {
        if (isEmpty())
          matrix = BooleanMatrix2D.Factory.zeros(0, 0);
        else
          matrix = (BooleanMatrix) matrix.selectRows(Ret.NEW, i);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_REMOVED, changes, null, null));
        return true;
      }
      return false;
    }

    public final ListIterator<R> listIterator(final int i) {
      return new ListIterator<R>() {

        private final ListIterator<R> it = RowHeads.super.listIterator(i);
        private R                     pointer;
        private int                   j;

        public final boolean hasNext() {
          return it.hasNext();
        }

        public final int nextIndex() {
          return it.nextIndex();
        }

        public final R next() {
          j = it.nextIndex();
          pointer = it.next();
          return pointer;
        }

        public final boolean hasPrevious() {
          return it.hasPrevious();
        }

        public final int previousIndex() {
          return it.previousIndex();
        }

        public final R previous() {
          j = it.previousIndex();
          pointer = it.previous();
          return pointer;
        }

        public final void add(final R row) {
          pointer = row;
          final int size0 = size();
          it.add(row);
          if (size0 != size()) {
            insert(++j, size0, 1);
            push(new RelationEvent<R, C>(RelationEvent.ROWS_ADDED, row, null));
          }
        }

        public final void set(final R row) {
          pointer = row;
          it.set(row);
        }

        public final void remove() {
          it.remove();
          if (isEmpty())
            matrix = BooleanMatrix2D.Factory.zeros(0, 0);
          else
            matrix = (BooleanMatrix) matrix.deleteRows(Ret.NEW, j);
          push(new RelationEvent<R, C>(RelationEvent.ROWS_REMOVED, pointer, null));
        }
      };
    }

    public final void clear() {
      super.clear();
      matrix = BooleanMatrix2D.Factory.zeros(0, 0);
      push(new RelationEvent<R, C>(RelationEvent.ROWS_CLEARED));
    }

    private final void append(final boolean wasEmpty, final int rows) {
      if (colHeads == null)
        return;
      final BooleanMatrix zeros = BooleanMatrix2D.Factory.zeros(rows, colHeads.size());
      if (wasEmpty)
        matrix = zeros;
      else
        matrix = (BooleanMatrix) matrix.appendVertically(Ret.NEW, zeros);
    }

    private final void insert(final int i, final int size0, final int rows) {
      if (colHeads == null)
        return;
      final int cols = colHeads.size();
      final BooleanMatrix zeros = BooleanMatrix2D.Factory.zeros(rows, cols);
      if (size0 == 0)
        matrix = zeros;
      else if (i == 0)
        matrix = (BooleanMatrix) zeros.appendVertically(Ret.NEW, matrix);
      else if (i == size0)
        matrix = (BooleanMatrix) matrix.appendVertically(Ret.NEW, zeros);
      else {
        BooleanMatrix upper = matrix.subMatrix(Ret.LINK, 0, 0, i - 1, cols - 1).toBooleanMatrix();
        BooleanMatrix lower = matrix.subMatrix(Ret.LINK, i, 0, size0 - 1, cols - 1).toBooleanMatrix();
        matrix = (BooleanMatrix) upper.appendVertically(Ret.NEW, zeros).appendVertically(Ret.NEW, lower);
      }
    }
  }

  private final class ColHeads extends HashSetArrayList<C> {

    private ColHeads(final Collection<? extends C> c) {
      super(c);
    }

    public final boolean add(final C col) {
      final boolean wasEmpty = isEmpty();
      if (super.add(col)) {
        append(wasEmpty, 1);
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_ADDED, null, col));
        return true;
      }
      return false;
    }

    public final void add(final int i, final C col) {
      final int size0 = size();
      super.add(i, col);
      if (size0 != size()) {
        insert(i, size0, 1);
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_ADDED, null, col));
      }
    }

    public final boolean addAll(final Collection<? extends C> c) {
      final int size0 = size();
      @SuppressWarnings("unchecked")
      final Set<C> changes = new HashSet<C>(Collections3.<C> difference((Collection<C>) c, this));
      if (super.addAll(c)) {
        append(size0 == 0, size() - size0);
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_ADDED, null, changes, null));
        return true;
      }
      return false;
    }

    public final boolean addAll(final int i, final Collection<? extends C> c) {
      final int size0 = size();
      @SuppressWarnings("unchecked")
      final Set<C> changes = new HashSet<C>(Collections3.<C> difference((Collection<C>) c, this));
      if (super.addAll(i, c)) {
        insert(i, size0, size() - size0);
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_ADDED, null, changes, null));
        return true;
      }
      return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean set(final Object o, final C col) {
      if (super.set(o, col)) {
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_SET, null, new Pair<C, C>((C) o, col)));
        return true;
      }
      return false;
    }

    @Override
    public final C set(final int i, final C col) {
      final C col0 = super.set(i, col);
      push(new RelationEvent<R, C>(RelationEvent.COLUMNS_SET, null, new Pair<C, C>(col0, col)));
      return col0;
    }

    @SuppressWarnings("unchecked")
    public final boolean remove(final Object o) {
      final int i = indexOf(o);
      if (i == -1)
        return false;
      super.remove(o);
      if (isEmpty())
        matrix = BooleanMatrix2D.Factory.zeros(0, 0);
      else
        matrix = (BooleanMatrix) matrix.deleteColumns(Ret.NEW, i);
      push(new RelationEvent<R, C>(RelationEvent.COLUMNS_REMOVED, null, (C) o));
      return true;
    }

    public final C remove(final int i) {
      final C col = super.remove(i);
      if (col != null) {
        if (isEmpty())
          matrix = BooleanMatrix2D.Factory.zeros(0, 0);
        else
          matrix = (BooleanMatrix) matrix.deleteColumns(Ret.NEW, i);
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_REMOVED, null, col));
      }
      return col;
    }

    public final boolean removeAll(final Collection<?> c) {
      final Collection<Integer> i = indicesOf(c, false);
      final Set<C> changes = new HashSet<C>();
      for (C col : this)
        if (c.contains(col))
          changes.add(col);
      if (super.removeAll(c)) {
        if (isEmpty())
          matrix = BooleanMatrix2D.Factory.zeros(0, 0);
        else
          matrix = (BooleanMatrix) matrix.deleteColumns(Ret.NEW, i);
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_REMOVED, null, changes, null));
        return true;
      }
      return false;
    }

    public final boolean retainAll(final Collection<?> c) {
      final Collection<Integer> i = indicesOf(c, false);
      final Set<C> changes = new HashSet<C>();
      for (C col : this)
        if (!c.contains(col))
          changes.add(col);
      if (super.retainAll(c)) {
        if (isEmpty())
          matrix = BooleanMatrix2D.Factory.zeros(0, 0);
        else
          matrix = (BooleanMatrix) matrix.selectColumns(Ret.NEW, i);
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_REMOVED, null, changes, null));
        return true;
      }
      return false;
    }

    public final ListIterator<C> listIterator(final int i) {
      return new ListIterator<C>() {

        private final ListIterator<C> it = ColHeads.super.listIterator(i);
        private C                     pointer;
        private int                   j;

        public final boolean hasNext() {
          return it.hasNext();
        }

        public final C next() {
          j = it.nextIndex();
          pointer = it.next();
          return pointer;
        }

        public final boolean hasPrevious() {
          return it.hasPrevious();
        }

        public final C previous() {
          j = it.previousIndex();
          pointer = it.previous();
          return pointer;
        }

        public final int nextIndex() {
          return it.nextIndex();
        }

        public final int previousIndex() {
          return it.previousIndex();
        }

        public final void remove() {
          it.remove();
          if (isEmpty())
            matrix = BooleanMatrix2D.Factory.zeros(0, 0);
          else
            matrix = (BooleanMatrix) matrix.deleteColumns(Ret.NEW, j);
          push(new RelationEvent<R, C>(RelationEvent.COLUMNS_REMOVED, null, pointer));
        }

        public final void set(final C col) {
          pointer = col;
          it.set(col);
        }

        public final void add(final C col) {
          pointer = col;
          final int size0 = size();
          it.add(col);
          if (size0 != size()) {
            insert(++j, size0, 1);
            push(new RelationEvent<R, C>(RelationEvent.COLUMNS_ADDED, null, col));
          }
        }
      };
    }

    public final void clear() {
      super.clear();
      matrix = BooleanMatrix2D.Factory.zeros(0, 0);
      push(new RelationEvent<R, C>(RelationEvent.COLUMNS_CLEARED));
    }

    private final void append(final boolean wasEmpty, final int cols) {
      final BooleanMatrix zeros = BooleanMatrix2D.Factory.zeros(rowHeads.size(), cols);
      if (wasEmpty)
        matrix = zeros;
      else
        matrix = (BooleanMatrix) matrix.appendHorizontally(Ret.NEW, zeros);
    }

    private final void insert(final int i, final int size0, final int cols) {
      final int rows = rowHeads.size();
      final BooleanMatrix zeros = BooleanMatrix2D.Factory.zeros(rows, cols);
      if (size0 == 0)
        matrix = zeros;
      else if (i == 0)
        matrix = (BooleanMatrix) zeros.appendHorizontally(Ret.NEW, matrix);
      else if (i == size0)
        matrix = (BooleanMatrix) matrix.appendHorizontally(Ret.NEW, zeros);
      else {
        final BooleanMatrix left = matrix.subMatrix(Ret.LINK, 0, 0, rows - 1, i - 1).toBooleanMatrix();
        final BooleanMatrix right = matrix.subMatrix(Ret.LINK, 0, i, rows - 1, size0 - 1).toBooleanMatrix();
        matrix = (BooleanMatrix) left.appendHorizontally(Ret.NEW, zeros).appendHorizontally(Ret.NEW, right);
      }
    }
  }

  private final class Heads extends HashSetArrayList<R> {

    private Heads(final Collection<? extends R> c) {
      super(c);
    }

    @SuppressWarnings("unchecked")
    public final boolean add(final R head) {
      if (super.add(head)) {
        append(size() - 1, 1);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_ADDED, head, null));
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_ADDED, null, (C) head));
        return true;
      }
      return false;
    }

    @SuppressWarnings("unchecked")
    public final void add(final int i, final R head) {
      final int size0 = size();
      super.add(i, head);
      if (size0 != size()) {
        insert(i, size0, 1);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_ADDED, head, null));
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_ADDED, null, (C) head));
      }
    }

    @SuppressWarnings("unchecked")
    public final boolean addAll(final Collection<? extends R> c) {
      final int size0 = size();
      final Set<R> changes = new HashSet<R>(Collections3.<R> difference((Collection<R>) c, this));
      if (super.addAll(c)) {
        append(size0, size() - size0);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_ADDED, changes, null, null));
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_ADDED, null, (Set<C>) changes, null));
        return true;
      }
      return false;
    }

    @SuppressWarnings("unchecked")
    public final boolean addAll(final int i, final Collection<? extends R> c) {
      final int size0 = size();
      final Set<R> changes = new HashSet<R>(Collections3.<R> difference((Collection<R>) c, this));
      if (super.addAll(i, c)) {
        insert(i, size0, size() - size0);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_ADDED, changes, null, null));
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_ADDED, null, (Set<C>) changes, null));
        return true;
      }
      return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean set(final Object o, final R head) {
      if (super.set(o, head)) {
        push(new RelationEvent<R, C>(RelationEvent.ROWS_SET, new Pair<R, R>((R) o, head), null));
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_SET, null, new Pair<C, C>((C) o, (C) head)));
        return true;
      }
      return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final R set(final int i, final R head) {
      final R head0 = super.set(i, head);
      push(new RelationEvent<R, C>(RelationEvent.ROWS_SET, new Pair<R, R>(head0, head), null));
      push(new RelationEvent<R, C>(RelationEvent.COLUMNS_SET, null, new Pair<C, C>((C) head0, (C) head)));
      return head0;
    }

    @SuppressWarnings("unchecked")
    public final boolean remove(final Object o) {
      final int i = indexOf(o);
      if (i == -1)
        return false;
      super.remove(o);
      if (isEmpty())
        matrix = BooleanMatrix2D.Factory.zeros(0, 0);
      else
        matrix = (BooleanMatrix) matrix.deleteRows(Ret.NEW, i).deleteColumns(Ret.NEW, i);
      push(new RelationEvent<R, C>(RelationEvent.ROWS_REMOVED, (R) o, null));
      push(new RelationEvent<R, C>(RelationEvent.COLUMNS_REMOVED, null, (C) o));
      return true;
    }

    @SuppressWarnings("unchecked")
    public final R remove(final int i) {
      final R head = super.remove(i);
      if (head != null) {
        if (isEmpty())
          matrix = BooleanMatrix2D.Factory.zeros(0, 0);
        else
          matrix = (BooleanMatrix) matrix.deleteRows(Ret.NEW, i).deleteColumns(Ret.NEW, i);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_REMOVED, head, null));
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_REMOVED, null, (C) head));
      }
      return head;
    }

    @SuppressWarnings("unchecked")
    public final boolean removeAll(final Collection<?> c) {
      final Collection<Integer> i = Sets.newHashSet(indicesOf(c, false));
      final Set<R> changes = new HashSet<R>();
      for (R head : this)
        if (c.contains(head))
          changes.add(head);
      if (super.removeAll(c)) {
        if (isEmpty())
          matrix = BooleanMatrix2D.Factory.zeros(0, 0);
        else
          matrix = (BooleanMatrix) matrix.deleteRows(Ret.NEW, i).deleteColumns(Ret.NEW, i);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_REMOVED, changes, null, null));
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_REMOVED, null, (Set<C>) changes, null));
        return true;
      }
      return false;
    }

    @SuppressWarnings("unchecked")
    public final boolean retainAll(final Collection<?> c) {
      final Collection<Integer> i = indicesOf(c, false);
      final Set<R> changes = new HashSet<R>();
      for (R head : this)
        if (c.contains(head))
          changes.add(head);
      if (super.removeAll(c)) {
        if (isEmpty())
          matrix = BooleanMatrix2D.Factory.zeros(0, 0);
        else
          matrix = (BooleanMatrix) matrix.selectRows(Ret.NEW, i).selectColumns(Ret.NEW, i);
        push(new RelationEvent<R, C>(RelationEvent.ROWS_REMOVED, changes, null, null));
        push(new RelationEvent<R, C>(RelationEvent.COLUMNS_REMOVED, null, (Set<C>) changes, null));
        return true;
      }
      return false;
    }

    public final ListIterator<R> listIterator(final int i) {
      return new ListIterator<R>() {

        private final ListIterator<R> it = Heads.super.listIterator(i);
        private R                     pointer;
        private int                   j;

        public final boolean hasNext() {
          return it.hasNext();
        }

        public final int nextIndex() {
          return it.nextIndex();
        }

        public final R next() {
          j = it.nextIndex();
          pointer = it.next();
          return pointer;
        }

        public final boolean hasPrevious() {
          return it.hasPrevious();
        }

        public final int previousIndex() {
          return it.previousIndex();
        }

        public final R previous() {
          j = it.previousIndex();
          pointer = it.previous();
          return pointer;
        }

        @SuppressWarnings("unchecked")
        public final void add(final R head) {
          pointer = head;
          final int size0 = size();
          it.add(head);
          if (size0 != size()) {
            j++;
            insert(j, size0, 1);
            push(new RelationEvent<R, C>(RelationEvent.ROWS_ADDED, head, null));
            push(new RelationEvent<R, C>(RelationEvent.COLUMNS_ADDED, null, (C) head));
          }
        }

        public final void set(final R head) {
          pointer = head;
          it.set(head);
        }

        @SuppressWarnings("unchecked")
        public final void remove() {
          it.remove();
          if (isEmpty())
            matrix = BooleanMatrix2D.Factory.zeros(0, 0);
          else
            matrix = (BooleanMatrix) matrix.deleteRows(Ret.NEW, j).deleteColumns(Ret.NEW, j);
          push(new RelationEvent<R, C>(RelationEvent.ROWS_REMOVED, pointer, null));
          push(new RelationEvent<R, C>(RelationEvent.COLUMNS_REMOVED, null, (C) pointer));
        }
      };
    }

    public final void clear() {
      super.clear();
      matrix = BooleanMatrix2D.Factory.zeros(0, 0);
      push(new RelationEvent<R, C>(RelationEvent.ROWS_CLEARED));
      push(new RelationEvent<R, C>(RelationEvent.COLUMNS_CLEARED));
    }

    private final void append(final int size0, final int heads) {
      if (size0 == 0) {
        matrix = BooleanMatrix2D.Factory.zeros(heads, heads);
        return;
      }
      final BooleanMatrix rows = BooleanMatrix2D.Factory.zeros(heads, size0);
      final BooleanMatrix cols = BooleanMatrix2D.Factory.zeros(size0 + heads, heads);
      matrix = (BooleanMatrix) matrix.appendVertically(Ret.NEW, rows).appendHorizontally(Ret.NEW, cols);
    }

    private final void insert(final int i, final int size0, final int heads) {
      if (size0 == 0) {
        matrix = BooleanMatrix2D.Factory.zeros(heads, heads);
        return;
      }
      final BooleanMatrix rows = BooleanMatrix2D.Factory.zeros(heads, size0);
      final BooleanMatrix cols = BooleanMatrix2D.Factory.zeros(size0 + heads, heads);
      if (i == 0)
        matrix = (BooleanMatrix) cols.appendHorizontally(Ret.NEW, rows.appendVertically(Ret.NEW, matrix));
      else if (i == size0)
        matrix = (BooleanMatrix) matrix.appendVertically(Ret.NEW, rows).appendHorizontally(Ret.NEW, cols);
      else {
        final BooleanMatrix upper = (BooleanMatrix) matrix.subMatrix(Ret.LINK, 0, 0, i - 1, size0 - 1);
        final BooleanMatrix lower = (BooleanMatrix) matrix.subMatrix(Ret.LINK, i, 0, size0 - 1, size0 - 1);
        matrix = (BooleanMatrix) upper.appendVertically(Ret.NEW, rows).appendVertically(Ret.NEW, lower);
        final BooleanMatrix left = (BooleanMatrix) matrix.subMatrix(Ret.LINK, 0, 0, size0 + heads - 1, i - 1);
        final BooleanMatrix right = (BooleanMatrix) matrix.subMatrix(Ret.LINK, 0, i, size0 + heads - 1, size0 - 1);
        matrix = (BooleanMatrix) left.appendHorizontally(Ret.NEW, cols).appendHorizontally(Ret.NEW, right);
      }
    }
  }

  protected BooleanMatrix                                                 matrix;
  private final Map<RelationEvent.Type, List<RelationEventHandler<R, C>>> eventHandlers = new ConcurrentHashMap<>();

  public MatrixRelation(final boolean homogen) {
    this(SetLists.<R> empty(), SetLists.<C> empty(), BooleanMatrix2D.Factory.zeros(0, 0), homogen);
  }

  public MatrixRelation(final SetList<R> rowHeads, final SetList<C> colHeads, final boolean homogen) {
    this(rowHeads, colHeads, BooleanMatrix2D.Factory.zeros(rowHeads.size(), colHeads.size()), homogen);
  }

  @SuppressWarnings("unchecked")
  public MatrixRelation(
      final SetList<R> rowHeads,
      final SetList<C> colHeads,
      final BooleanMatrix matrix,
      final boolean homogen) {
    super(homogen);
    if (homogen) {
      if (!rowHeads.equals(colHeads))
        throw new NoHomogenRelationException();
      this.rowHeads = new Heads(rowHeads);
      this.colHeads = (SetList<C>) this.rowHeads;
    } else {
      this.rowHeads = new RowHeads(rowHeads);
      this.colHeads = new ColHeads(colHeads);
    }
    this.matrix = matrix;
  }

  public final boolean add(final R row, final C col) {
    boolean changed;
    final int i;
    if (rowHeads.add(row)) {
      i = rowHeads.size() - 1;
      changed = true;
    } else {
      i = rowHeads.indexOf(row);
      changed = false;
    }
    final int j;
    if (colHeads.add(col)) {
      j = colHeads.size() - 1;
      changed = true;
    } else
      j = colHeads.indexOf(col);
    if (changed || !matrix.getBoolean(i, j)) {
      matrix.setBoolean(true, i, j);
      push(
          new RelationEvent<R, C>(
              RelationEvent.ENTRIES_ADDED,
              null,
              null,
              Collections.singleton(new Pair<R, C>(row, col))));
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public boolean addFast(final Object o1, final Object o2) {
    final int i = rowHeads.indexOf(o1);
//    if (i != -1) {
    final int j = colHeads.indexOf(o2);
//      if (j != -1)
    if (!matrix.getBoolean(i, j)) {
      matrix.setBoolean(true, i, j);
      push(
          new RelationEvent<R, C>(
              RelationEvent.ENTRIES_ADDED,
              null,
              null,
              Collections.singleton(new Pair<R, C>((R) o1, (C) o2))));
      return true;
    }
//    }
    return false;
  }

//  @SuppressWarnings("unchecked")
  public void addFastSilent(final Object o1, final Object o2) {
    final int i = rowHeads.indexOf(o1);
//    if (i != -1) {
    final int j = colHeads.indexOf(o2);
//      if (j != -1)
//    if (!matrix.getBoolean(i, j)) {
    matrix.setBoolean(true, i, j);
//      push(new RelationEvent<R, C>(RelationEvent.ENTRIES_ADDED, null, null, Collections.singleton(new Pair<R, C>(
//          (R) o1,
//          (C) o2))));
//      return true;
//    }
//    }
//    return false;
  }

  @SuppressWarnings("unchecked")
  public final boolean addAll(final Relation<? extends R, ? extends C> r) {
    boolean changed = false;
    if (r instanceof MatrixRelation) {
      final MatrixRelation<? extends R, ? extends C> _r = (MatrixRelation<? extends R, ? extends C>) r;
      rowHeads.addAll(_r.rowHeads);
      colHeads.addAll(_r.colHeads);
      matrix
          .selectRows(Ret.LINK, rowHeads.indicesOf(_r.rowHeads, true))
          .selectColumns(Ret.LINK, colHeads.indicesOf(_r.colHeads, true))
          .or(Ret.ORIG, _r.matrix);
      changed = true;
      push(new RelationEvent<R, C>(RelationEvent.ALL_CHANGED, null, null, null));
    } else {
      final Iterator<?> it = r.iterator();
      Pair<? extends R, ? extends C> next;
      while (it.hasNext()) {
        next = (Pair<? extends R, ? extends C>) it.next();
        changed |= add(next.x(), next.y());
      }
    }
    return changed;
  }

  @SuppressWarnings("unchecked")
  public final boolean addAllFast(final Relation<?, ?> r) {
    boolean changed = false;
    if (r instanceof MatrixRelation) {
      final MatrixRelation<?, ?> _r = (MatrixRelation<?, ?>) r;
      matrix
          .selectRows(Ret.LINK, rowHeads.indicesOf(_r.rowHeads, false))
          .selectColumns(Ret.LINK, colHeads.indicesOf(_r.colHeads, false))
          .or(
              Ret.ORIG,
              _r.matrix
                  .selectRows(Ret.LINK, _r.rowHeads.indicesOf(rowHeads, false))
                  .selectColumns(Ret.LINK, _r.colHeads.indicesOf(colHeads, false)));
      push(new RelationEvent<R, C>(RelationEvent.ALL_CHANGED, null, null, null));
      changed = true;
    } else {
      final Iterator<?> it = r.iterator();
      Pair<? extends R, ? extends C> next;
      while (it.hasNext()) {
        next = (Pair<? extends R, ? extends C>) it.next();
        changed |= addFast(next.x(), next.y());
      }
    }
    return changed;
  }

  @SuppressWarnings("unchecked")
  public boolean remove(final Object o1, final Object o2) {
    final int i = rowHeads.indexOf(o1);
    if (i != -1) {
      final int j = colHeads.indexOf(o2);
      if (j != -1 && matrix.getBoolean(i, j)) {
        matrix.setBoolean(false, i, j);
        push(
            new RelationEvent<R, C>(
                RelationEvent.ENTRIES_REMOVED,
                null,
                null,
                Collections.singleton(new Pair<R, C>((R) o1, (C) o2))));
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public final boolean removeAll(final Relation<?, ?> r) {
    boolean changed = false;
    if (r instanceof MatrixRelation) {
      final MatrixRelation<?, ?> _r = (MatrixRelation<?, ?>) r;
      matrix
          .selectRows(Ret.LINK, rowHeads.indicesOf(_r.rowHeads, false))
          .selectColumns(Ret.LINK, colHeads.indicesOf(_r.colHeads, false))
          .and(
              Ret.ORIG,
              _r.matrix
                  .selectRows(Ret.LINK, _r.rowHeads.indicesOf(rowHeads, false))
                  .selectColumns(Ret.LINK, _r.colHeads.indicesOf(colHeads, false))
                  .not(Ret.LINK));
      push(new RelationEvent<R, C>(RelationEvent.ALL_CHANGED, null, null, null));
      changed = true;
    } else {
      final Iterator<?> it = r.iterator();
      Pair<? extends R, ? extends C> next;
      while (it.hasNext()) {
        next = (Pair<? extends R, ? extends C>) it.next();
        changed |= remove(next.x(), next.y());
      }
    }
    return changed;
  }

  public final boolean retainAll(final Relation<?, ?> r) {
    boolean changed = false;
    if (r instanceof MatrixRelation) {
      final MatrixRelation<?, ?> _r = (MatrixRelation<?, ?>) r;
      final Collection<Integer> i = rowHeads.indicesOf(_r.rowHeads, false);
      final Collection<Integer> j = colHeads.indicesOf(_r.colHeads, false);
      final Collection<Integer> i0 =
          Collections2.filter(SetLists.integers(rowHeads.size()), Predicates.not(Predicates.in(i)));
      final Collection<Integer> j0 =
          Collections2.filter(SetLists.integers(colHeads.size()), Predicates.not(Predicates.in(j)));
      matrix.selectRows(Ret.LINK, i0).selectColumns(Ret.LINK, j0).and(Ret.ORIG, false);
      matrix.selectRows(Ret.LINK, i0).selectColumns(Ret.LINK, j).and(Ret.ORIG, false);
      matrix.selectRows(Ret.LINK, i).selectColumns(Ret.LINK, j0).and(Ret.ORIG, false);
      matrix
          .selectRows(Ret.LINK, i)
          .selectColumns(Ret.LINK, j)
          .and(
              Ret.ORIG,
              _r.matrix
                  .selectRows(Ret.LINK, _r.rowHeads.indicesOf(rowHeads, false))
                  .selectColumns(Ret.LINK, _r.colHeads.indicesOf(colHeads, false)));
      push(new RelationEvent<R, C>(RelationEvent.ALL_CHANGED, null, null, null));
      changed = true;
    } else {
      for (C col : colHeads) {
        final Set<R> column = col(col);
        if (r.colHeads().contains(col))
          changed |= column.retainAll(r.col(col));
        else {
          changed |= !column.isEmpty();
          column.clear();
        }
      }
    }
    return changed;
  }

  public final boolean contains(final Object o1, final Object o2) {
    final int i = rowHeads.indexOf(o1);
    if (i == -1)
      return false;
    final int j = colHeads.indexOf(o2);
    return j != -1 && matrix.getBoolean(i, j);
  }

  public final boolean containsAll(final Relation<?, ?> r) {
    if (rowHeads.containsAll(r.rowHeads()) && colHeads.containsAll(r.colHeads())) {
      if (r instanceof MatrixRelation) {
        final MatrixRelation<?, ?> _r = (MatrixRelation<?, ?>) r;
        return matrix
            .selectRows(Ret.LINK, rowHeads.indicesOf(_r.rowHeads, true))
            .selectColumns(Ret.LINK, colHeads.indicesOf(_r.colHeads, true))
            .equals(_r.matrix);
      } else {
        final Iterator<?> iterator = r.iterator();
        Pair<?, ?> next;
        while (iterator.hasNext()) {
          next = (Pair<?, ?>) iterator.next();
          if (!contains(next.x(), next.y()))
            return false;
        }
        return true;
      }
    }
    return false;
  }

  public final Set<C> row(final Object o) {
    return new AbstractSet<C>() {

      private final int i = rowHeads.indexOf(o);

      @SuppressWarnings("unchecked")
      public final boolean add(final C col) {
        final int j = colHeads.indexOf(col);
        if (matrix.getBoolean(i, j))
          return false;
        matrix.setBoolean(true, i, j);
        push(
            new RelationEvent<R, C>(
                RelationEvent.ENTRIES_ADDED,
                null,
                null,
                Collections.singleton(new Pair<R, C>((R) o, col))));
        return true;
      }

      public final boolean addAll(final Collection<? extends C> c) {
        boolean changed = false;
        @SuppressWarnings("unchecked")
        final Set<C> changes = new HashSet<C>(Collections3.<C> difference((Collection<C>) c, this));
        for (C col : c) {
          final int j = colHeads.indexOf(col);
          if (!matrix.getBoolean(i, j)) {
            matrix.setBoolean(true, i, j);
            changed = true;
            push(
                new RelationEvent<R, C>(
                    RelationEvent.ENTRIES_ADDED,
                    null,
                    null,
                    new HashSet<Pair<R, C>>(Collections2.transform(changes, new Function<C, Pair<R, C>>() {

                      @SuppressWarnings("unchecked")
                      public final Pair<R, C> apply(final C col) {
                        return new Pair<R, C>((R) o, col);
                      }
                    }))));
          }
        }
        return changed;
      }

      public final boolean contains(final Object o) {
        return matrix.getBoolean(i, colHeads.indexOf(o));
      }

      @SuppressWarnings("unchecked")
      public final boolean remove(final Object o2) {
        final int j = colHeads.indexOf(o2);
        if (!matrix.getBoolean(i, j))
          return false;
        matrix.setBoolean(false, i, j);
        push(
            new RelationEvent<R, C>(
                RelationEvent.ENTRIES_REMOVED,
                null,
                null,
                Collections.singleton(new Pair<R, C>((R) o, (C) o2))));
        return true;
      }

      public final boolean removeAll(final Collection<?> c) {
        boolean changed = false;
        final Set<C> changes = new HashSet<C>();
        for (C col : this)
          if (c.contains(col))
            changes.add(col);
        for (Object o2 : c) {
          final int j = colHeads.indexOf(o2);
          if (matrix.getBoolean(i, j)) {
            matrix.setBoolean(false, i, j);
            changed = true;
            push(
                new RelationEvent<R, C>(
                    RelationEvent.ENTRIES_REMOVED,
                    null,
                    null,
                    new HashSet<Pair<R, C>>(Collections2.transform(changes, new Function<C, Pair<R, C>>() {

                      @SuppressWarnings("unchecked")
                      public final Pair<R, C> apply(final C col) {
                        return new Pair<R, C>((R) o, col);
                      }
                    }))));
          }
        }
        return changed;
      }

      public final boolean retainAll(final Collection<?> c) {
        boolean changed = false;
        final Set<C> changes = new HashSet<C>();
        for (C col : this)
          if (!c.contains(col))
            changes.add(col);
        for (Object o2 : Collections2.filter(colHeads, Predicates.not(Predicates.in(c)))) {
          final int j = colHeads.indexOf(o2);
          if (matrix.getBoolean(i, j)) {
            matrix.setBoolean(false, i, j);
            changed = true;
            push(
                new RelationEvent<R, C>(
                    RelationEvent.ENTRIES_REMOVED,
                    null,
                    null,
                    new HashSet<Pair<R, C>>(Collections2.transform(changes, new Function<C, Pair<R, C>>() {

                      @SuppressWarnings("unchecked")
                      public final Pair<R, C> apply(final C col) {
                        return new Pair<R, C>((R) o, col);
                      }
                    }))));
          }
        }
        return changed;
      }

      public final void clear() {
        for (int j = 0; j < colHeads.size(); j++)
          matrix.setBoolean(false, i, j);
      }

      public final Iterator<C> iterator() {
        return Iterators
            .transform(Iterators.filter(ListIterators.integers(0, colHeads.size()), new Predicate<Integer>() {

              public final boolean apply(final Integer j) {
                return matrix.getBoolean(i, j);
              }
            }), new Function<Integer, C>() {

              public final C apply(final Integer j) {
                return colHeads.get(j);
              }
            });
      }

      public final int size() {
        int size = 0;
        if (i != -1)
          for (int j = 0; j < colHeads.size(); j++)
            if (matrix.getBoolean(i, j))
              size++;
        return size;
      }

      public final HashSet<C> clone() {
        return new HashSet<C>(this);
      }
    };
  }

  public final Set<R> col(final Object o) {
    return new AbstractSet<R>() {

      private final int j = colHeads.indexOf(o);

      @SuppressWarnings("unchecked")
      public final boolean add(final R row) {
        final int i = rowHeads.indexOf(row);
        if (matrix.getBoolean(i, j))
          return false;
        matrix.setBoolean(true, i, j);
        push(
            new RelationEvent<R, C>(
                RelationEvent.ENTRIES_ADDED,
                null,
                null,
                Collections.singleton(new Pair<R, C>(row, (C) o))));
        return true;
      };

      public final boolean addAll(Collection<? extends R> c) {
        boolean changed = false;
        @SuppressWarnings("unchecked")
        final Set<R> changes = new HashSet<R>(Collections3.<R> difference((Collection<R>) c, this));
        for (R row : c) {
          final int i = rowHeads.indexOf(row);
          if (!matrix.getBoolean(i, j)) {
            matrix.setBoolean(true, i, j);
            changed = true;
            push(
                new RelationEvent<R, C>(
                    RelationEvent.ENTRIES_ADDED,
                    null,
                    null,
                    new HashSet<Pair<R, C>>(Collections2.transform(changes, new Function<R, Pair<R, C>>() {

                      @SuppressWarnings("unchecked")
                      public final Pair<R, C> apply(final R row) {
                        return new Pair<R, C>(row, (C) o);
                      }
                    }))));
          }
        }
        return changed;
      }

      public final boolean contains(final Object o1) {
        return matrix.getBoolean(rowHeads.indexOf(o1), j);
      }

      @SuppressWarnings("unchecked")
      public final boolean remove(final Object o1) {
        final int i = rowHeads.indexOf(o1);
        if (!matrix.getBoolean(i, j))
          return false;
        matrix.setBoolean(false, i, j);
        push(
            new RelationEvent<R, C>(
                RelationEvent.ENTRIES_REMOVED,
                null,
                null,
                Collections.singleton(new Pair<R, C>((R) o1, (C) o))));
        return true;
      }

      public final boolean removeAll(final Collection<?> c) {
        boolean changed = false;
        final Set<R> changes = new HashSet<R>();
        for (R row : this)
          if (c.contains(row))
            changes.add(row);
        for (Object o1 : c) {
          final int i = rowHeads.indexOf(o1);
          if (matrix.getBoolean(i, j)) {
            matrix.setBoolean(false, i, j);
            changed = true;
            push(
                new RelationEvent<R, C>(
                    RelationEvent.ENTRIES_REMOVED,
                    null,
                    null,
                    new HashSet<Pair<R, C>>(Collections2.transform(changes, new Function<R, Pair<R, C>>() {

                      @SuppressWarnings("unchecked")
                      public final Pair<R, C> apply(final R row) {
                        return new Pair<R, C>(row, (C) o);
                      }
                    }))));
          }
        }
        return changed;
      }

      public final boolean retainAll(final Collection<?> c) {
        boolean changed = false;
        final Set<R> changes = new HashSet<R>();
        for (R row : this)
          if (!c.contains(row))
            changes.add(row);
        for (Object o1 : Collections2.filter(rowHeads, Predicates.not(Predicates.in(c)))) {
          final int i = rowHeads.indexOf(o1);
          if (matrix.getBoolean(i, j)) {
            matrix.setBoolean(false, i, j);
            changed = true;
            push(
                new RelationEvent<R, C>(
                    RelationEvent.ENTRIES_REMOVED,
                    null,
                    null,
                    new HashSet<Pair<R, C>>(Collections2.transform(changes, new Function<R, Pair<R, C>>() {

                      @SuppressWarnings("unchecked")
                      public final Pair<R, C> apply(final R row) {
                        return new Pair<R, C>(row, (C) o);
                      }
                    }))));
          }
        }
        return changed;
      }

      public final void clear() {
        for (int i = 0; i < rowHeads.size(); i++)
          matrix.setBoolean(false, i, j);
      }

      public final Iterator<R> iterator() {
        return Iterators
            .transform(Iterators.filter(ListIterators.integers(0, rowHeads.size()), new Predicate<Integer>() {

              public final boolean apply(final Integer i) {
                return matrix.getBoolean(i, j);
              }
            }), new Function<Integer, R>() {

              public final R apply(final Integer i) {
                return rowHeads.get(i);
              }
            });
      }

      public final int size() {
        int size = 0;
        if (j != -1)
          for (int i = 0; i < rowHeads.size(); i++)
            if (matrix.getBoolean(i, j))
              size++;
        return size;
      }

      public final HashSet<R> clone() {
        return new HashSet<R>(this);
      }
    };
  }

  public final Set<C> rowAnd(final Collection<?> c) {
    if (rowHeads().size() == 0 || colHeads().size() == 0)
      return new HashSet<C>(colHeads());
    return colHeads()
        .parallelStream()
        .filter(col -> c.parallelStream().allMatch(row -> contains(row, col)))
        .collect(Collectors.toSet());
//    return Sets.filter(colHeads(), new Predicate<C>() {
//
//      private final BooleanMatrix rowAnd = BooleanMatrices.andRow(matrix, rowHeads.indicesOf(c, true));
//
//      public final boolean apply(final C col) {
//        return rowAnd.getBoolean(0, colHeads.indexOf(col));
//      }
//    });
  }

  public final Set<R> colAnd(final Collection<?> c) {
    if (rowHeads().size() == 0 || colHeads().size() == 0)
      return new HashSet<R>(rowHeads());
    return rowHeads()
        .parallelStream()
        .filter(row -> c.parallelStream().allMatch(col -> contains(row, col)))
        .collect(Collectors.toSet());
//    return Sets.filter(rowHeads(), new Predicate<R>() {
//
//      private final BooleanMatrix colAnd = BooleanMatrices.andCol(matrix, colHeads.indicesOf(c, true));
//
//      public final boolean apply(final R row) {
//        return colAnd.getBoolean(rowHeads.indexOf(row), 0);
//      }
//    });
  }

  public final void _add(final int i, final int j) {
    matrix.setBoolean(true, i, j);
    push(
        new RelationEvent<R, C>(
            RelationEvent.ENTRIES_ADDED,
            null,
            null,
            Collections.singleton(new Pair<R, C>(rowHeads().get(i), colHeads().get(j)))));
  }

  public final void _remove(final int i, final int j) {
    matrix.setBoolean(false, i, j);
  }

  public final void _flip(final int i, final int j) {
    matrix.setBoolean(matrix.getBoolean(i, j), i, j);
  }

  public final boolean _contains(final int i, final int j) {
    return matrix.getBoolean(i, j);
  }

  public final Collection<Integer> _row(final int i) {
    return _row(i, SetLists.integers(colHeads.size()));
  }

  public final Collection<Integer> _col(final int j) {
    return _col(j, SetLists.integers(rowHeads.size()));
  }

  public final Collection<Integer> _row(final int i, final Collection<Integer> js) {
    final BitSetFX _row = new BitSetFX();
    for (int j : js)
      if (matrix.getBoolean(i, j))
        _row.set(j);
    return _row;
//    return Collections3.newBitSetFX(Collections2.filter(j, new Predicate<Integer>() {
//
//      public final boolean apply(final Integer j) {
//        return matrix.getBoolean(i, j);
//      }
//    }));
  }

  public final Collection<Integer> _col(final int j, final Collection<Integer> is) {
    final BitSetFX _col = new BitSetFX();
    for (int i : is)
      if (matrix.getBoolean(i, j))
        _col.set(i);
    return _col;
//    return Collections3.newBitSetFX(Collections2.filter(i, new Predicate<Integer>() {
//
//      public final boolean apply(final Integer i) {
//        return matrix.getBoolean(i, j);
//      }
//    }));
  }

  public final Collection<Integer> _rowAnd(final int... i) {
    if (i.length == 1)
      return _row(i[0]);
    return _rowAnd(Ints.asList(i));
  }

  public final Collection<Integer> _colAnd(final int... j) {
    if (j.length == 1)
      return _col(j[0]);
    return _colAnd(Ints.asList(j));
  }

  public synchronized final BitSetFX _rowAnd(final Iterable<Integer> i) {
//    return _rowAnd(i, SetLists.integers(colHeads.size()));
    if (rowHeads().size() == 0 || colHeads().size() == 0)
      return Collections3.integers(colHeads.size());
//      return SetLists.integers(colHeads.size());
    final BooleanMatrix rowAnd = BooleanMatrices.andRow(matrix, i);
    BitSetFX _rowAnd = new BitSetFX();
    for (int j = 0; j < colHeads.size(); j++)
      if (rowAnd.getBoolean(0, j))
        _rowAnd.add(j);
    return _rowAnd;
  }

  public synchronized final BitSetFX _colAnd(final Iterable<Integer> j) {
//    return _colAnd(j, SetLists.integers(rowHeads.size()));
    if (rowHeads().size() == 0 || colHeads().size() == 0)
      return Collections3.integers(rowHeads().size());
//      return SetLists.integers(rowHeads.size());
    final BooleanMatrix colAnd = BooleanMatrices.andCol(matrix, j);
    BitSetFX _colAnd = new BitSetFX();
    for (int i = 0; i < rowHeads.size(); i++)
      if (colAnd.getBoolean(i, 0))
        _colAnd.add(i);
    return _colAnd;
  }

  public final BitSetFX _rowAnd(final Iterable<Integer> i, final Collection<Integer> j) {
    if (rowHeads().size() == 0 || colHeads().size() == 0)
      return Collections3.integers(colHeads.size());
//      return SetLists.integers(colHeads.size());
    final BooleanMatrix rowAnd = BooleanMatrices.andRow(matrix, i);
    return j
        .parallelStream()
        .filter(_j -> rowAnd.getBoolean(0, _j))
        .collect(BitSetFX::new, BitSetFX::add, BitSetFX::addAll);
//    return Collections3.newBitSetFX(Collections2.filter(j, new Predicate<Integer>() {
//
//      private final BooleanMatrix rowAnd = BooleanMatrices.andRow(matrix, i);
//
//      public final boolean apply(final Integer _j) {
//        return rowAnd.getBoolean(0, _j);
//      }
//    }));
  }

  public final BitSetFX _colAnd(final Iterable<Integer> j, final Collection<Integer> i) {
    if (rowHeads().size() == 0 || colHeads().size() == 0)
      return Collections3.integers(rowHeads().size());
//      return SetLists.integers(rowHeads.size());
    final BooleanMatrix colAnd = BooleanMatrices.andCol(matrix, j);
    return i
        .parallelStream()
        .filter(_i -> colAnd.getBoolean(_i, 0))
        .collect(BitSetFX::new, BitSetFX::add, BitSetFX::addAll);
//    return Collections3.newBitSetFX(Collections2.filter(i, new Predicate<Integer>() {
//
//      private final BooleanMatrix colAnd = BooleanMatrices.andCol(matrix, j);
//
//      public final boolean apply(final Integer _i) {
//        return colAnd.getBoolean(_i, 0);
//      }
//    }));
  }

  public final void empty() {
    matrix = BooleanMatrices.empty(Math.max(rowHeads.size(), 1), Math.max(colHeads.size(), 1));
    push(new RelationEvent<R, C>(RelationEvent.ALL_CHANGED));
  }

  public final void fill() {
    matrix = BooleanMatrices.full(Math.max(rowHeads.size(), 1), Math.max(colHeads.size(), 1));
    push(new RelationEvent<R, C>(RelationEvent.ALL_CHANGED));
  }

  public final boolean isEmpty() {
    return !matrix.containsBoolean(true);
  }

  public final boolean isFull() {
    return !matrix.containsBoolean(false);
  }

  public final int size() {
    int size = 0;
    for (int i = 0; i < rowHeads.size(); i++)
      for (int j = 0; j < colHeads.size(); j++)
        if (matrix.getBoolean(i, j))
          size++;
    return size;
  }

  public final Iterator<Pair<R, C>> iterator() {
    return Iterators.transform(Iterators.filter(matrix.allCoordinates().iterator(), new Predicate<long[]>() {

      public final boolean apply(final long[] ij) {
        return matrix.getBoolean(ij) && ij[0] < rowHeads.size() && ij[1] < colHeads.size();
      }
    }), new Function<long[], Pair<R, C>>() {

      public final Pair<R, C> apply(final long[] ij) {
        return new Pair<R, C>(rowHeads.get((int) ij[0]), colHeads.get((int) ij[1]));
      }
    });
  }

  public Relation<R, C> subRelation(final Collection<?> rows, final Collection<?> cols) {
    return new AbstractRelation<R, C>(
        SetLists.intersection(rowHeads, rows),
        SetLists.intersection(colHeads, cols),
        false) {

      public final boolean contains(final Object o1, final Object o2) {
        return rowHeads().contains(o1) && colHeads().contains(o2) && MatrixRelation.this.contains(o1, o2);
      }

      public final MatrixRelation<R, C> clone() {
        return new MatrixRelation<R, C>(
            rowHeads(),
            colHeads(),
            (BooleanMatrix) MatrixRelation.this.matrix
                .selectRows(Ret.NEW, MatrixRelation.this.rowHeads().indicesOf(rows, false))
                .selectColumns(Ret.NEW, MatrixRelation.this.colHeads().indicesOf(cols, false)),
            false);
      }
    };
  }

  public final void pushAllChangedEvent() {
    synchronized (eventHandlers) {
      push(RelationEvent.ALL_CHANGED, new RelationEvent<R, C>(RelationEvent.ALL_CHANGED));
    }
  }

  protected final void push(final RelationEvent<R, C> event) {
    synchronized (eventHandlers) {
      RelationEvent.Type type = event.getType();
      push(type, event);
      while ((type = type.getSuperType()) != null)
        push(type, event);
    }
  }

  private final void push(final RelationEvent.Type type, final RelationEvent<R, C> event) {
    synchronized (eventHandlers) {
      if (eventHandlers.containsKey(type))
        for (RelationEventHandler<R, C> eventHandler : eventHandlers.get(type))
          eventHandler.handle(event);
    }
  }

  public final void addEventHandler(final RelationEventHandler<R, C> eventHandler, final RelationEvent.Type... types) {
    synchronized (eventHandlers) {
      for (RelationEvent.Type type : types) {
        if (!eventHandlers.containsKey(type))
          eventHandlers.put(type, new CopyOnWriteArrayList<>());
        eventHandlers.get(type).add(eventHandler);
      }
    }
  }

  public final void removeEventHandler(final RelationEvent.Type type, final RelationEventHandler<R, C> eventHandler) {
    synchronized (eventHandlers) {
      eventHandlers.remove(type, eventHandler);
    }
  }

  protected final boolean hasEventHandlers(final RelationEvent.Type type) {
    synchronized (eventHandlers) {
      RelationEvent.Type type_ = type;
      if (!eventHandlers.get(type_).isEmpty())
        return true;
      while ((type_ = type_.getSuperType()) != null)
        if (!eventHandlers.get(type_).isEmpty())
          return true;
      return false;
    }
  }

  public final BooleanMatrix matrix() {
    return matrix;
  }

  public final void setMatrix(final BooleanMatrix matrix) {
    if (matrix.getSize(0) != rowHeads.size() || matrix.getSize(1) != colHeads.size())
      throw new IllegalArgumentException();
    this.matrix = matrix;
    pushAllChangedEvent();
  }

  @Deprecated
  public final void rewriteMatrix() {
    final BooleanMatrix copy = BooleanMatrices.clone(matrix);
    setMatrix(BooleanMatrices.empty(matrix.getRowCount(), matrix.getColumnCount()));
    matrix.or(Ret.ORIG, copy);
  }

  public final void setContent(final SetList<R> rows, final SetList<C> cols, final BooleanMatrix matrix) {
    rowHeads.addAll(rows);
    if (!homogen)
      colHeads.addAll(cols);
    setMatrix(matrix);
  }

  public void dispose() {
    rowHeads.clear();
    colHeads.clear();
  }

  public MatrixRelation<R, C> clone() {
    return new MatrixRelation<R, C>(rowHeads, colHeads, BooleanMatrices.clone(matrix), homogen);
  }

  public int hashCode() {
    return 7 * rowHeads.hashCode() + 11 * colHeads.hashCode() + 13 * matrix.hashCode();
  }

  public final boolean[][] toArray() {
    return matrix.toBooleanArray();
  }

  public Relation<R, R> subRelation(final Collection<?> c) {
    return new AbstractRelation<R, R>(
        SetLists.<R> intersection(rowHeads, c),
        SetLists.<R> intersection(rowHeads, c),
        true) {

      public boolean contains(Object o1, Object o2) {
        return rowHeads().contains(o1) && rowHeads().contains(o2) && MatrixRelation.this.contains(o1, o2);
      }

      public MatrixRelation<R, R> clone() {
        return new MatrixRelation<R, R>(
            rowHeads(),
            rowHeads(),
            (BooleanMatrix) MatrixRelation.this.matrix
                .selectRows(Ret.NEW, MatrixRelation.this.rowHeads().indicesOf(c, false))
                .selectColumns(Ret.NEW, MatrixRelation.this.colHeads().indicesOf(c, false)),
            true);
      }
    };
  }

  public MatrixRelation<R, R> neighborhood() {
    checkHomogen();
    return new MatrixRelation<R, R>(
        rowHeads(),
        rowHeads(),
        BooleanMatrices.transitiveReduction(BooleanMatrices.reflexiveReduction(matrix)),
        true);
  }

  public MatrixRelation<R, R> order() {
    checkHomogen();
    return new MatrixRelation<R, R>(
        rowHeads(),
        rowHeads(),
        BooleanMatrices.reflexiveClosure(BooleanMatrices.transitiveClosure(matrix)),
        true);
  }

  public final boolean isReflexive() {
    return matrix.ge(Ret.NEW, BooleanMatrices.identity(size())).equals(BooleanMatrices.full(size()));
  }

  public final boolean isIrreflexive() {
    return matrix.and(Ret.NEW, BooleanMatrices.identity(size())).equals(BooleanMatrices.empty(size()));
  }

  public final boolean isSymmetric() {
    return matrix.equals(matrix.transpose(Ret.NEW));
  }

  public final boolean isAsymmetric() {
    return matrix.and(Ret.NEW, matrix.transpose(Ret.NEW)).equals(BooleanMatrices.empty(size()));
  }

  public final boolean isConnex() {
    return matrix.or(Ret.NEW, matrix.transpose(Ret.NEW)).equals(BooleanMatrices.full(size()));
  }

  public final boolean isAntisymmetric() {
    return matrix
        .and(Ret.NEW, matrix.transpose(Ret.NEW))
        .le(Ret.NEW, BooleanMatrices.identity(size()))
        .equals(BooleanMatrices.full(size()));
  }

  public final boolean isQuasiconnex() {
    return matrix
        .or(Ret.NEW, matrix.transpose(Ret.NEW))
        .ge(Ret.NEW, BooleanMatrices.negativeIdentity(size()))
        .equals(BooleanMatrices.full(size()));
  }

  public final boolean isAlternative() {
    return isAntisymmetric() && isQuasiconnex();
  }

  public final boolean isTransitive() {
    return matrix
        .mtimes(Ret.NEW, false, matrix)
        .toBooleanMatrix()
        .le(Ret.NEW, matrix)
        .equals(BooleanMatrices.full(size()));
  }

  public final boolean isNegativeTransitive() {
    final BooleanMatrix2D not = (BooleanMatrix2D) matrix.not(Ret.NEW);
    return not.mtimes(Ret.NEW, false, not).toBooleanMatrix().le(Ret.NEW, not).equals(BooleanMatrices.full(size()));
  }

  public final boolean isAtransitive() {
    return matrix
        .mtimes(Ret.NEW, false, matrix)
        .toBooleanMatrix()
        .and(Ret.NEW, matrix)
        .equals(BooleanMatrices.empty(size()));
  }

  public final boolean isNegativAtransitive() {
    final BooleanMatrix2D not = (BooleanMatrix2D) matrix.not(Ret.NEW);
    return not.mtimes(Ret.NEW, false, not).toBooleanMatrix().and(Ret.NEW, not).equals(BooleanMatrices.empty(size()));
  }

  public final boolean isNCyclic(final int n) {
    return BooleanMatrices
        .power(matrix(), n)
        .le(Ret.NEW, matrix.transpose(Ret.NEW))
        .equals(BooleanMatrices.full(size()));
  }

  public final boolean isCyclic() {
    return BooleanMatrices
        .transitiveClosure(matrix())
        .le(Ret.NEW, matrix.transpose(Ret.NEW))
        .equals(BooleanMatrices.full(size()));
  }

  public final boolean isNAcyclic(final int n) {
    return BooleanMatrices
        .power(matrix(), n)
        .le(Ret.NEW, matrix.transpose(Ret.NEW).not(Ret.NEW))
        .equals(BooleanMatrices.full(size()));
  }

  public final boolean isAcyclic() {
    return BooleanMatrices
        .transitiveClosure(matrix())
        .le(Ret.NEW, matrix.transpose(Ret.NEW).not(Ret.NEW))
        .equals(BooleanMatrices.full(size()));
  }

  public final boolean isNTransitive(final int n) {
    return BooleanMatrices.power(matrix(), n).le(Ret.NEW, matrix).equals(BooleanMatrices.full(size()));
  }

  public final boolean isNAtransitive(final int n) {
    return BooleanMatrices.power(matrix(), n).le(Ret.NEW, matrix.not(Ret.NEW)).equals(BooleanMatrices.full(size()));
  }

  public final boolean isLeftComparative() {
    return matrix
        .transpose(Ret.NEW)
        .mtimes(Ret.NEW, false, matrix)
        .toBooleanMatrix()
        .le(Ret.NEW, matrix)
        .equals(BooleanMatrices.full(size()));
  }

  public final boolean isRightComparative() {
    return matrix
        .mtimes(Ret.NEW, false, matrix.transpose(Ret.NEW))
        .toBooleanMatrix()
        .le(Ret.NEW, matrix)
        .equals(BooleanMatrices.full(size()));
  }
}
