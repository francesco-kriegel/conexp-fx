package conexp.fx.core.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
