package conexp.fx.core.collections;

import java.util.Collection;

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
