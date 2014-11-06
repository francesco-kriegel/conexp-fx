package conexp.fx.core.math;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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


import com.google.common.base.Function;

public abstract class Isomorphism<A, B> implements Function<A, B> {

  public static final <A> Isomorphism<A, A> identity() {
    return new Isomorphism<A, A>() {

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

  public static final <A, B> Isomorphism<A, B> create(final Function<A, B> function, final Function<B, A> inverse) {
    return new Isomorphism<A, B>() {

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

  public static final <A, B> Isomorphism<B, A> invert(final Isomorphism<A, B> isomorphism) {
    return new Isomorphism<B, A>() {

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

  public static final <A, B, C> Isomorphism<A, C> compose(
      final Isomorphism<A, B> isomorphism1,
      final Isomorphism<B, C> isomorphism2) {
    return new Isomorphism<A, C>() {

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
        return Isomorphism.this.invert(b);
      }
    };
  }
}
