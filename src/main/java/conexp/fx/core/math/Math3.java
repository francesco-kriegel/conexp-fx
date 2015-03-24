package conexp.fx.core.math;

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


import java.util.Collection;

public final class Math3 {

  public static final double wsum(final double x, final double y, final double p) {
    return p * x + (1d - p) * y;
  }

  public static final double round(final double x, final int decimalsAfterDot) {
    final double f = Math.pow(10d, (double) decimalsAfterDot);
    return Math.rint(f * x) / f;
  }

  public static final long factorial(final int n) {
    return n == 0 ? 1 : n * factorial(n - 1);
  }

  public static final double modulo(final double number, final double module) {
    double value = number;
    while (value < 0)
      value += module;
    return value % module;
  }

  public static final long binomial(final int N, final int K) {
    final long[][] binomial = new long[N + 1][K + 1];
    for (int k = 1; k <= K; k++)
      binomial[0][k] = 0;
    for (int n = 0; n <= N; n++)
      binomial[n][0] = 1;
    for (int n = 1; n <= N; n++)
      for (int k = 1; k <= K; k++)
        binomial[n][k] = binomial[n - 1][k - 1] + binomial[n - 1][k];
    return binomial[N][K];
  }

  public static double sum(Collection<? extends Number> c) {
    double s = 0d;
    for (Number n : c)
      s += n.doubleValue();
    return s;
  }

  public static double product(Collection<? extends Number> c) {
    double s = 1d;
    for (Number n : c)
      s *= n.doubleValue();
    return s;
  }
}
