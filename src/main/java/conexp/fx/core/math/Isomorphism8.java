package conexp.fx.core.math;

import java.util.function.Function;

public final class Isomorphism8<T, R> implements Function<T, R> {

  private final Function<T, R> function;
  private final Function<R, T> inverse;

  public Isomorphism8(final Function<T, R> function, final Function<R, T> inverse) {
    super();
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
