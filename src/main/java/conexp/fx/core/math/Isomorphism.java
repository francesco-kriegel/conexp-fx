package conexp.fx.core.math;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
