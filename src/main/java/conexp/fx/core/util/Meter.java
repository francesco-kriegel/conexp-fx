package conexp.fx.core.util;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;

public class Meter<T> {

  public static final Meter<Long> newNanoStopWatch() {
    return new Meter<Long>(System::nanoTime, (x, y) -> x - y);
  }

  public static final Meter<Long> newMilliStopWatch() {
    return new Meter<Long>(System::currentTimeMillis, (x, y) -> x - y);
  }

  private final Supplier<T>       supplier;
  private final BinaryOperator<T> operator;
  private final T                 origin;

  private Meter(final Supplier<T> supplier, final BinaryOperator<T> operator) {
    super();
    this.supplier = supplier;
    this.operator = operator;
    this.origin = supplier.get();
  }

  public final T measure() {
    return operator.apply(supplier.get(), origin);
  }

//  public static final class ObservableMeter<T> extends Meter<T> {
//
//    private ObservableMeter(final Supplier<T> supplier, final BinaryOperator<T> operator) {
//      super(supplier, operator);
//    }
//  }

}
