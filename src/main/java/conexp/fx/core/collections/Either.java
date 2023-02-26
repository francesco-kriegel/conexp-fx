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

import java.util.Optional;

public final class Either<L, R> extends Pair<Optional<L>, Optional<R>> {

  public static final <L, R> Either<L, R> ofLeft(final L left) {
    if (left == null)
      throw new NullPointerException();
    return new Either<L, R>(left, null);
  }

  public static final <L, R> Either<L, R> ofRight(final R right) {
    if (right == null)
      throw new NullPointerException();
    return new Either<L, R>(null, right);
  }

  private Either(final L left, final R right) {
    super(Optional.ofNullable(left), Optional.ofNullable(right));
  }

}
