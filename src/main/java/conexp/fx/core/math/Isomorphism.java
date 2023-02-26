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

import java.util.function.Function;

public final class Isomorphism<T, R> implements Function<T, R> {

  private final Function<T, R> function;
  private final Function<R, T> inverse;

  public Isomorphism(final Function<T, R> function, final Function<R, T> inverse) {
    super();
    if (function == null || inverse == null)
      throw new IllegalArgumentException(new NullPointerException());
    this.function = function;
    this.inverse = inverse;
  }

  @Override
  public final R apply(T t) {
    return function.apply(t);
  }

  public final T invert(R r) {
    return inverse.apply(r);
  }

  public final Function<R, T> inverse() {
    return inverse;
  }

}
