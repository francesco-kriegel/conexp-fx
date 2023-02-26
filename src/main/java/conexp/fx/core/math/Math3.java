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

  public static final String formatMillis(final long millis) {
    if (millis == 0)
      return "";
    final long ms = millis % 1000l;
    final long s = (millis / 1000l) % 60l;
    final long m = (millis / 60000l) % 60l;
    final long h = (millis / 3600000l);
    if (h > 0)
      return String.format("%02dh %02dmin", h, m);
    if (m > 0)
      return String.format("%02dmin %02ds", m, s);
    if (s > 0)
      return String.format("%02ds %03dms", s, ms);
    return String.format("%03dms", ms);
  }

  public static final String formatNanos(final long nanos) {
    if (nanos == 0)
      return "";
    final long ns = nanos % 1000l;
    final long µs = (nanos / 1000l) % 1000l;
    final long ms = (nanos / 1000000l) % 1000l;
    final long s = (nanos / 1000000000l) % 60l;
    final long m = (nanos / 60000000000l) % 60l;
    final long h = (nanos / 3600000000000l);
    if (h > 0)
      return String.format("%02dh %02dmin", h, m);
    if (m > 0)
      return String.format("%02dmin %02ds", m, s);
    if (s > 0)
      return String.format("%02ds %03dms", s, ms);
    if (ms > 0)
      return String.format("%03dms %03dµs", ms, µs);
    if (µs > 0)
      return String.format("%03dµs %03dns", µs, ns);
    return String.format("%03dns", ns);
  }
}
