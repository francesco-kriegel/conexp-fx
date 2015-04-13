package conexp.fx.core.collections;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.util.Iterator;

import javafx.beans.value.ObservableValue;

import com.google.common.base.Function;

public final class Functions
{
  public static final <T> Function<ObservableValue<T>, T> observableValueToCurrentValueFunction()
  {
    return new Function<ObservableValue<T>, T>()
      {
        public final T apply(final ObservableValue<T> observable)
        {
          return observable.getValue();
        }
      };
  }

  public static final Function<Iterable<Double>, Double> sum     = new Function<Iterable<Double>, Double>()
                                                                   {
                                                                     @Override
                                                                     public final Double apply(
                                                                         final Iterable<Double> doubles)
                                                                     {
                                                                       double s = 0d;
                                                                       for (Double d : doubles)
                                                                         s += d;
                                                                       return s;
                                                                     }
                                                                   };
  public static final Function<Iterable<Double>, Double> product = new Function<Iterable<Double>, Double>()
                                                                   {
                                                                     @Override
                                                                     public final Double apply(
                                                                         final Iterable<Double> doubles)
                                                                     {
                                                                       double s = 1d;
                                                                       for (Double d : doubles)
                                                                         s *= d;
                                                                       return s;
                                                                     }
                                                                   };

  public static final Function<Iterable<Double>, Double> wsum(final Iterable<Double> weighs)
  {
    return new Function<Iterable<Double>, Double>()
      {
        @Override
        public final Double apply(final Iterable<Double> doubles)
        {
          final Iterator<Double> weigh = weighs.iterator();
          double s = 0d;
          for (Double d : doubles)
            s += weigh.next() * d;
          return s;
        }
      };
  }

  public static final Function<Iterable<Double>, Double> wproduct(final Iterable<Double> weighs)
  {
    return new Function<Iterable<Double>, Double>()
      {
        @Override
        public final Double apply(final Iterable<Double> doubles)
        {
          final Iterator<Double> weigh = weighs.iterator();
          double s = 1d;
          for (Double d : doubles)
            s *= Math.pow(d, weigh.next());
          return s;
        }
      };
  }

  public static final Function<Double, Double> power(final double p)
  {
    return new Function<Double, Double>()
      {
        public final Double apply(final Double x)
        {
          return Math.pow(x, p);
        }
      };
  }

  public static final Function<Double, Double> root(final double p)
  {
    return new Function<Double, Double>()
      {
        public final Double apply(final Double x)
        {
          return Math.pow(x, 1d / p);
        }
      };
  }
}
