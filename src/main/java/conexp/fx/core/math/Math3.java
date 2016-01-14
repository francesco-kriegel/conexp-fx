package conexp.fx.core.math;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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

  public static final String formatTime(final long time) {
    if (time == 0)
      return "";
    final long ms = time % 1000;
    final long s = (time / 1000) % 60;
    final long m = (time / 60000) % 60;
    final long h = (time / 3600000);
    if (h > 0)
      return String.format("%02dh %02dmin", h, m);
    if (m > 0)
      return String.format("%02dmin %02ds", m, s);
    if (s > 0)
      return String.format("%02ds %03dms", s, ms);
    return String.format("%03dms", ms);
  }
}
