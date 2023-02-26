package conexp.fx.core.util;

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

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import conexp.fx.core.math.Math3;

public class Meter<T> {

  public static final Meter<Long> newNanoStopWatch() {
    return new Meter<Long>(System::nanoTime, (x, y) -> x - y, Math3::formatNanos);
  }

  public static final Meter<Long> newMilliStopWatch() {
    return new Meter<Long>(System::currentTimeMillis, (x, y) -> x - y, Math3::formatMillis);
  }

  private final Supplier<T>         supplier;
  private final BinaryOperator<T>   operator;
  private final Function<T, String> formatter;
  private T                         origin;

  private Meter(final Supplier<T> supplier, final BinaryOperator<T> operator, final Function<T, String> formatter) {
    super();
    this.supplier = supplier;
    this.operator = operator;
    this.formatter = formatter;
    this.origin = supplier.get();
  }

  public final T measure() {
    return operator.apply(supplier.get(), origin);
  }

  public final String measureAndFormat() {
    return formatter.apply(measure());
  }

  public final void reset() {
    this.origin = supplier.get();
  }

//  public static final class ObservableMeter<T> extends Meter<T> {
//
//    private ObservableMeter(final Supplier<T> supplier, final BinaryOperator<T> operator) {
//      super(supplier, operator);
//    }
//  }

}
