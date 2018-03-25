package conexp.fx.core.context.temporal;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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

public final class LTL<M> {

  /**
   * This enumeration defines the available types for linear temporal logic, that are NOW (emulates the current
   * timepoint just for code convienience), NEXTW (weak next), NEXTS (strong next), SOMETIMES (sometimes in the future),
   * ALWAYS (always in the future), UNTILW (weak until), and UNTILS (strong until).
   *
   */
  public enum Type {
    NOW(""),
    NEXTW("o"),
    NEXTS("o"),
    SOMETIMES("<>"),
    ALWAYS("[]"),
    UNTILW("U"),
    UNTILS("U");

    private final String symbol;

    private Type(final String symbol) {
      this.symbol = symbol;
    }

    public final String getSymbol() {
      return symbol;
    }
  }

  private final LTL.Type type;
  private final M        m;
  private final M        n;

  public LTL(final LTL.Type type, final M m) {
    this(type, m, null);
  }

  public LTL(final LTL.Type type, final M m, final M n) {
    super();
    if (m == null)
      throw new NullPointerException("temporalized attribute m cannot be null.");
    this.type = type;
    this.m = m;
    this.n = n;
  }

  public final LTL.Type getType() {
    return type;
  }

  public final M getM() {
    return m;
  }

  public final M getN() {
    return n;
  }

  @Override
  public String toString() {
    switch (type) {
    case UNTILS:
    case UNTILW:
      return m + " " + type.symbol + " " + n;
    case NOW:
      return m.toString();
    default:
      return type.symbol + " " + m + "";
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LTL))
      return false;
    final LTL<?> other = (LTL<?>) obj;
    return other.type.equals(this.type) && other.m.equals(this.m)
        && ((other.n == null && this.n == null) || other.n.equals(this.n));
  }

  @Override
  public final int hashCode() {
    return type.hashCode() + 7 * m.hashCode() + 13 * (n == null ? 0 : n.hashCode());
  }

}
