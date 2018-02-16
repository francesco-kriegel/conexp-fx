package conexp.fx.core.collections;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
