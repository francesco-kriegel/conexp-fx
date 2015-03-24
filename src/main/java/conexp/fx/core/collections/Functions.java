package conexp.fx.core.collections;

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
