/**
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

public interface PartialComparable<E> extends Comparable<E> {

  public default boolean equivalent(E e) {
    return compareTo(e) == 0;
  }

  public default boolean smaller(E e) {
    return compareTo(e) == -1;
  }

  public default boolean greater(E e) {
    return compareTo(e) == 1;
  }

  public default boolean smallerEq(E e) {
    final int c = compareTo(e);
    return c == -1 || c == 0;
  }

  public default boolean greaterEq(E e) {
    final int c = compareTo(e);
    return c == 0 || c == 1;
  }

  public default boolean uncomparable(E e) {
    final int c = compareTo(e);
    return c < 1 || c > 1;
  }
}
