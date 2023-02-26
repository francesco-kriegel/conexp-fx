package conexp.fx.core.context.negation;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2023 Francesco Kriegel
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

public class Literal<M> {

  public enum Type {
    POSITIVE,
    NEGATIVE;
  }

  private final Literal.Type type;
  private final M            m;

  public Literal(final Literal.Type type, final M m) {
    super();
    this.type = type;
    this.m = m;
  }

  public Literal(final M m) {
    this(Literal.Type.POSITIVE, m);
  }

  public final M getM() {
    return m;
  }

  public final Literal.Type getType() {
    return type;
  }

  @Override
  public final String toString() {
    switch (type) {
    case POSITIVE:
      return m.toString();
    case NEGATIVE:
      return "-" + m;
    }
    return null;
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof Literal))
      return false;
    final Literal<?> other = (Literal<?>) obj;
    return other.type.equals(this.type) && other.m.equals(this.m);
  }

  @Override
  public final int hashCode() {
    return type.hashCode() + 3 * m.hashCode();
  }
}
