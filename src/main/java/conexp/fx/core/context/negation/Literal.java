package conexp.fx.core.context.negation;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public class Literal<M> {

  public enum Type {
    POSITIVE,
    NEGATIVE;
  }

  private final Type type;
  private final M    m;

  public Literal(final Type type, final M m) {
    super();
    this.type = type;
    this.m = m;
  }

  public Literal(final M m) {
    this(Type.POSITIVE, m);
  }

  public final M getM() {
    return m;
  }

  public final Type getType() {
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
