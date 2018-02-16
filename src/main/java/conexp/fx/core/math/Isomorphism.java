package conexp.fx.core.math;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
