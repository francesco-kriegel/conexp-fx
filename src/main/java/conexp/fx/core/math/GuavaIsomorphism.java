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

import com.google.common.base.Function;

public abstract class GuavaIsomorphism<A, B> implements Function<A, B> {

  public static final <A> GuavaIsomorphism<A, A> identity() {
    return new GuavaIsomorphism<A, A>() {

      @Override
      public A apply(A a) {
        return a;
      }

      @Override
      public A invert(A a) {
        return a;
      }
    };
  }

  public static final <A, B> GuavaIsomorphism<A, B> create(final Function<A, B> function, final Function<B, A> inverse) {
    return new GuavaIsomorphism<A, B>() {

      @Override
      public final B apply(final A a) {
        return function.apply(a);
      }

      @Override
      public final A invert(final B b) {
        return inverse.apply(b);
      }

      @Override
      public Function<A, B> function() {
        return function;
      }

      @Override
      public Function<B, A> inverse() {
        return inverse;
      }
    };
  }

  public static final <A, B> GuavaIsomorphism<B, A> invert(final GuavaIsomorphism<A, B> isomorphism) {
    return new GuavaIsomorphism<B, A>() {

      @Override
      public final A apply(final B b) {
        return isomorphism.invert(b);
      }

      @Override
      public final B invert(final A a) {
        return isomorphism.apply(a);
      }

      @Override
      public Function<B, A> function() {
        return isomorphism.inverse();
      }

      @Override
      public Function<A, B> inverse() {
        return isomorphism.function();
      }
    };
  }

  public static final <A, B, C> GuavaIsomorphism<A, C>
      compose(final GuavaIsomorphism<A, B> isomorphism1, final GuavaIsomorphism<B, C> isomorphism2) {
    return new GuavaIsomorphism<A, C>() {

      @Override
      public final C apply(final A a) {
        return isomorphism2.apply(isomorphism1.apply(a));
      }

      @Override
      public final A invert(final C c) {
        return isomorphism1.invert(isomorphism2.invert(c));
      }
    };
  }

  public A invert(B b) {
    throw new RuntimeException();
  }

  public Function<A, B> function() {
    return this;
  }

  public Function<B, A> inverse() {
    return new Function<B, A>() {

      @Override
      public final A apply(final B b) {
        return GuavaIsomorphism.this.invert(b);
      }
    };
  }

  public final java.util.function.Function<A, B> toJavaFunction() {
    return a -> apply(a);
  }

  public final java.util.function.Function<B, A> inverseToJavaFunction() {
    return b -> invert(b);
  }
}
