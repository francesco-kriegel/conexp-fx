/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.math;

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
import java.util.Iterator;

import org.ujmp.core.booleanmatrix.BooleanMatrix;
import org.ujmp.core.booleanmatrix.BooleanMatrix2D;
import org.ujmp.core.calculation.Calculation.Ret;

public final class BooleanMatrices {

  public static final BooleanMatrix clone(final BooleanMatrix m) {
    final BooleanMatrix copy = BooleanMatrices.empty(m.getRowCount(), m.getColumnCount());
    copy.or(Ret.ORIG, m);
    return copy;
//    The below return statement sometimes yields read-only matrices.
//    return m.clone().toBooleanMatrix();
  }

  public static final BooleanMatrix empty(final long size) {
    return empty(size, size);
  }

  public static final BooleanMatrix empty(final long rows, final long columns) {
    return BooleanMatrix2D.Factory.zeros(rows, columns);
  }

  public static final BooleanMatrix full(final long size) {
    return full(size, size);
  }

  public static final BooleanMatrix full(final long rows, final long columns) {
    return complement(empty(rows, columns));
  }

  public static final BooleanMatrix identity(final long size) {
    final BooleanMatrix m = empty(size);
    for (int i = 0; i < size; i++)
      m.setBoolean(true, i, i);
    return m;
  }

  public static final BooleanMatrix negativeIdentity(final long size) {
    return complement(identity(size));
  }

  public static final BooleanMatrix lowerDiagonal(final long size) {
    return dual(upperDiagonal(size));
  }

  public static final BooleanMatrix upperDiagonal(final long size) {
    final BooleanMatrix m = empty(size);
    for (int i = 0; i < size; i++)
      for (int j = i; j < size; j++)
        m.setBoolean(true, i, j);
    return m;
  }

  public static final BooleanMatrix strictLowerDiagonal(final long size) {
    return complement(upperDiagonal(size));
  }

  public static final BooleanMatrix strictUpperDiagonal(final long size) {
    return complement(lowerDiagonal(size));
  }

  public static final BooleanMatrix apposition(final BooleanMatrix... ms) {
    if (ms.length == 0)
      return null;
    BooleanMatrix m = ms[0];
    for (int i = 1; i < ms.length; i++)
      m = apposition(m, ms[i]);
    return m;
  }

  public static final BooleanMatrix apposition(final BooleanMatrix left, final BooleanMatrix right) {
    if (left == null)
      return (BooleanMatrix) right.clone();
    if (right == null)
      return (BooleanMatrix) left.clone();
    return (BooleanMatrix) left.appendHorizontally(Ret.NEW, right);
  }

  public static final BooleanMatrix subposition(final BooleanMatrix... ms) {
    if (ms.length == 0)
      return null;
    BooleanMatrix m = ms[0];
    for (int i = 1; i < ms.length; i++)
      m = subposition(m, ms[i]);
    return m;
  }

  public static final BooleanMatrix subposition(final BooleanMatrix upper, final BooleanMatrix lower) {
    if (upper == null)
      return (BooleanMatrix) lower.clone();
    if (lower == null)
      return (BooleanMatrix) upper.clone();
    return (BooleanMatrix) upper.appendVertically(Ret.NEW, lower);
  }

  public static final BooleanMatrix quadPosition(
      final BooleanMatrix leftUpper,
      final BooleanMatrix rightUpper,
      final BooleanMatrix leftLower,
      final BooleanMatrix rightLower) {
    return subposition(apposition(leftUpper, rightUpper), apposition(leftLower, rightLower));
  }

  public static final BooleanMatrix complement(final BooleanMatrix m) {
    return (BooleanMatrix) m.not(Ret.NEW);
  }

  public static final BooleanMatrix dual(final BooleanMatrix m) {
    return (BooleanMatrix) m.transpose(Ret.NEW);
  }

  public static final BooleanMatrix booleann(final long size) {
    if (size < 0)
      return null;
    else if (size == 0)
      return identity(1);
    final BooleanMatrix m = booleann(size - 1);
    return subposition(apposition(m, m), apposition(empty((long) Math.pow(2, size - 1)), m));
  }

  public static final BooleanMatrix directSum(final BooleanMatrix leftUpper, final BooleanMatrix rightLower) {
    return subposition(
        apposition(leftUpper, full(leftUpper.getRowCount(), rightLower.getColumnCount())),
        apposition(full(rightLower.getRowCount(), leftUpper.getColumnCount()), rightLower));
  }

  public static final BooleanMatrix horizontalSum(final BooleanMatrix leftUpper, final BooleanMatrix rightLower) {
    return subposition(
        apposition(leftUpper, empty(leftUpper.getRowCount(), rightLower.getColumnCount())),
        apposition(empty(rightLower.getRowCount(), leftUpper.getColumnCount()), rightLower));
  }

  public static final BooleanMatrix verticalSum(final BooleanMatrix leftUpper, final BooleanMatrix rightLower) {
    return subposition(
        apposition(leftUpper, full(leftUpper.getRowCount(), rightLower.getColumnCount())),
        apposition(empty(rightLower.getRowCount(), leftUpper.getColumnCount()), rightLower));
  }

  public static final BooleanMatrix substitutionSum(
      final BooleanMatrix outer,
      final BooleanMatrix inner,
      final long row,
      final long column,
      final Collection<Integer> gI,
      final Collection<Integer> mI) {
    final BooleanMatrix lu, ru, ll, rl;
    // ( G\{g}, M\{m}, I )
    lu = outer;
    System.out.println(lu);
    // ( H, N, J )
    rl = inner;
    System.out.println(rl);
    // ( G\{g}, N, (m'\{g}) x N )
    ru = empty(lu.getRowCount(), rl.getColumnCount());
    ru.selectRows(Ret.LINK, mI).not(Ret.ORIG);
    System.out.println(ru);
    // ( H, M\{m}, H x (g'\{m}) )
    ll = empty(rl.getRowCount(), lu.getColumnCount());
    ll.selectColumns(Ret.LINK, gI).not(Ret.ORIG);
    System.out.println(ll);
    return quadPosition(lu, ru, ll, rl).deleteRows(Ret.NEW, row).deleteColumns(Ret.NEW, column).toBooleanMatrix();
  }

  public static final BooleanMatrix directProduct(final BooleanMatrix m1, final BooleanMatrix m2) {
    return (BooleanMatrix) scale(m1, m2.getRowCount(), m2.getColumnCount())
        .or(Ret.NEW, duplicate(m2, m1.getRowCount(), m1.getColumnCount()));
  }

  public static final BooleanMatrix biProduct(final BooleanMatrix m1, final BooleanMatrix m2) {
    return (BooleanMatrix) scale(m1, m2.getRowCount(), m2.getColumnCount())
        .and(Ret.NEW, duplicate(m2, m1.getRowCount(), m1.getColumnCount()));
  }

  public static final BooleanMatrix semiProduct(final BooleanMatrix m1, final BooleanMatrix m2) {
    return apposition(scaleV(m1, m2.getRowCount()), duplicateV(m2, m1.getRowCount()));
  }

  private static final BooleanMatrix duplicate(final BooleanMatrix m, final long rowFactor, final long columnFactor) {
    return duplicateV(duplicateH(m, columnFactor), rowFactor);
  }

  private static final BooleanMatrix duplicateH(final BooleanMatrix m, final long columnFactor) {
    BooleanMatrix h = null;
    for (int i = 0; i < columnFactor; i++)
      h = apposition(h, m);
    return h;
  }

  private static final BooleanMatrix duplicateV(final BooleanMatrix m, final long rowFactor) {
    BooleanMatrix v = null;
    for (int i = 0; i < rowFactor; i++)
      v = subposition(v, m);
    return v;
  }

  private static final BooleanMatrix scale(final BooleanMatrix m, final long rowFactor, final long columnFactor) {
    return scaleV(scaleH(m, columnFactor), rowFactor);
  }

  private static final BooleanMatrix scaleH(final BooleanMatrix m, final long columnFactor) {
    BooleanMatrix h = null;
    for (long j = 0; j < m.getColumnCount(); j++)
      for (int f = 0; f < columnFactor; f++)
        h = apposition(h, (BooleanMatrix) m.selectColumns(Ret.NEW, j));
    return h;
  }

  private static final BooleanMatrix scaleV(final BooleanMatrix m, final long rowFactor) {
    BooleanMatrix v = null;
    for (long i = 0; i < m.getRowCount(); i++)
      for (int f = 0; f < rowFactor; f++)
        v = subposition(v, (BooleanMatrix) m.selectRows(Ret.NEW, i));
    return v;
  }

  public static final BooleanMatrix andCol(final BooleanMatrix m, final Iterable<Integer> columns) {
//    return StreamSupport.stream(columns.spliterator(), false).map(column -> m.selectColumns(Ret.NEW, column)).collect(
//        () -> full(m.getRowCount(), 1),
//        (x, y) -> x.and(Ret.ORIG, y),
//        (x, y) -> x.and(Ret.ORIG, y));
    final Iterator<Integer> it = columns.iterator();
    if (it.hasNext()) {
      final BooleanMatrix andCol = (BooleanMatrix) m.selectColumns(Ret.NEW, it.next());
      while (it.hasNext())
        andCol.and(Ret.ORIG, m.selectColumns(Ret.LINK, it.next()));
      return andCol;
    }
    return full(m.getRowCount(), 1);
  }

  public static final BooleanMatrix andRow(final BooleanMatrix m, final Iterable<Integer> rows) {
//    return StreamSupport.stream(rows.spliterator(), false).map(row -> m.selectRows(Ret.NEW, row)).collect(
//        () -> full(1, m.getColumnCount()),
//        (x, y) -> x.and(Ret.ORIG, y),
//        (x, y) -> x.and(Ret.ORIG, y));
    final Iterator<Integer> it = rows.iterator();
    if (it.hasNext()) {
      final BooleanMatrix andRow = (BooleanMatrix) m.selectRows(Ret.NEW, it.next());
      while (it.hasNext())
        andRow.and(Ret.ORIG, m.selectRows(Ret.LINK, it.next()));
      return andRow;
    }
    return full(1, m.getColumnCount());
  }

  public static final BooleanMatrix andCol(final BooleanMatrix m, final Integer... columns) {
    return andCol(m, Arrays.<Integer> asList(columns));
  }

  public static final BooleanMatrix andRow(final BooleanMatrix m, final Integer... rows) {
    return andRow(m, Arrays.<Integer> asList(rows));
  }

  private static final boolean isSquare(final BooleanMatrix m) {
    return m.getRowCount() == m.getColumnCount();
  }

  public static final BooleanMatrix product(final BooleanMatrix m1, final BooleanMatrix m2) {
    return m1.mtimes(Ret.NEW, false, m2).toBooleanMatrix();
  }

  public static final BooleanMatrix power(final BooleanMatrix m, final int n) {
    if (!isSquare(m))
      throw new IllegalArgumentException();
    if (n < 0)
      throw new IllegalArgumentException();
    if (n == 0)
      return identity(m.getRowCount());
    if (n == 1)
      return clone(m);
    if (n == 2)
      return product(m, m);
    return product(power(m, n - 1), m);
  }

  public static final BooleanMatrix reflexiveClosure(final BooleanMatrix m) {
    if (!isSquare(m))
      return null;
    return m.or(Ret.NEW, identity(m.getRowCount())).toBooleanMatrix();
  }

  public static final BooleanMatrix reflexiveReduction(final BooleanMatrix m) {
    if (!isSquare(m))
      return null;
    return m.and(Ret.NEW, negativeIdentity(m.getRowCount())).toBooleanMatrix();
  }

  public static final BooleanMatrix transitiveClosure(final BooleanMatrix m) {
    if (!isSquare(m))
      return null;
    BooleanMatrix t = clone(m);
    BooleanMatrix power = clone(m);
    BooleanMatrix last = null;
    do {
      power = product(power, m);
      last = clone(t);
      t = t.or(Ret.NEW, power).toBooleanMatrix();
    } while (!t.equals(last));
    return t;
  }

  public static final BooleanMatrix transitiveReduction(final BooleanMatrix m) {
    if (!isSquare(m))
      return null;
    return m.and(Ret.NEW, m.mtimes(Ret.NEW, false, transitiveClosure(m)).not(Ret.NEW)).toBooleanMatrix();
  }
}
