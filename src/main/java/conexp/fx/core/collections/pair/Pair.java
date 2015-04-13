package conexp.fx.core.collections.pair;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
