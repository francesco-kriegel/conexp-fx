package conexp.fx.core.collections;

import java.util.Collection;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import javafx.beans.value.ObservableValue;

public final class GuavaFunctions {

  public static final <X> Predicate<X> toGuavaPredicate(final java.util.function.Predicate<X> p) {
    return new Predicate<X>() {

      @Override
      public final boolean apply(final X x) {
        return p.test(x);
      }
    };
  }

  public static final <X> java.util.function.Predicate<X> toJavaPredicate(final Predicate<X> p) {
    return p::apply;
  }

  public static final <X, Y> Function<X, Y> toGuavaFunction(final java.util.function.Function<X, Y> f) {
    return new Function<X, Y>() {

      @Override
      public final Y apply(final X x) {
        return f.apply(x);
      }
    };
  }

  public static final <X, Y> java.util.function.Function<X, Y> toJavaFunction(final Function<X, Y> f) {
    return f::apply;
  }

  public static final <T> Function<ObservableValue<T>, T> observableValueToCurrentValueFunction() {
    return toGuavaFunction(ObservableValue::getValue);
  }

  public static final Function<Iterable<Double>, Double> sum     = toGuavaFunction(doubles -> {
                                                                   double s = 0d;
                                                                   for (Double d : doubles)
                                                                     s += d;
                                                                   return s;
                                                                 });

  public static final Function<Iterable<Double>, Double> product = toGuavaFunction(doubles -> {
                                                                   double s = 1d;
                                                                   for (Double d : doubles)
                                                                     s *= d;
                                                                   return s;
                                                                 });

  public static final Function<Iterable<Double>, Double> wsum(final Iterable<Double> weighs) {
    return toGuavaFunction(doubles -> {
      final Iterator<Double> weigh = weighs.iterator();
      double s = 0d;
      for (Double d : doubles)
        s += weigh.next() * d;
      return s;
    });
  }

  public static final Function<Iterable<Double>, Double> wproduct(final Iterable<Double> weighs) {
    return toGuavaFunction(doubles -> {
      final Iterator<Double> weigh = weighs.iterator();
      double s = 1d;
      for (Double d : doubles)
        s *= Math.pow(d, weigh.next());
      return s;
    });
  }

  public static final Function<Double, Double> power(final double p) {
    return toGuavaFunction(x -> Math.pow(x, p));
  }

  public static final Function<Double, Double> root(final double p) {
    return toGuavaFunction(x -> Math.pow(x, 1d / p));
  }

  public static final <E> Predicate<Collection<E>> isEmpty() {
    return toGuavaPredicate(Collection::isEmpty);
  }
}
