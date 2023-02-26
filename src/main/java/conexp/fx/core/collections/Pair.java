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

public class Pair<X, Y> {

  public static final <X, Y> Pair<X, Y> of(final X x, final Y y) {
    return new Pair<X, Y>(x, y);
  }

  protected X x;
  protected Y y;

  public Pair(final X x, final Y y) {
    super();
    this.x = x;
    this.y = y;
  }

  public final X x() {
    return x;
  }

  public final Y y() {
    return y;
  }

  public final X first() {
    return x;
  }

  public final Y second() {
    return y;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null)
      return false;
    if (!(object instanceof Pair))
      return false;
    final Pair<?, ?> other = (Pair<?, ?>) object;
    final boolean equalsX =
        (other.x == null && this.x == null) || (other.x != null && this.x != null && other.x.equals(this.x));
    if (!equalsX)
      return false;
    final boolean equalsY =
        (other.y == null && this.y == null) || (other.y != null && this.y != null && other.y.equals(this.y));
    return equalsY;
  }

  @Override
  public int hashCode() {
    if (x == null && y == null)
      return 0;
    if (x == null)
      return 1 + 3 * y.hashCode();
    if (y == null)
      return 1 + 2 * x.hashCode();
    return 1 + 2 * x.hashCode() + 3 * y.hashCode();
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
