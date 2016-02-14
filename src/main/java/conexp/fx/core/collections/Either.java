package conexp.fx.core.collections;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Optional;

public final class Either<L, R> {

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

  private final Optional<L> left;
  private final Optional<R> right;

  private Either(final L left, final R right) {
    this.left = Optional.ofNullable(
        left);
    this.right = Optional.ofNullable(
        right);
  }

  public final Optional<L> getLeft() {
    return left;
  }

  public final Optional<R> getRight() {
    return right;
  }

}
