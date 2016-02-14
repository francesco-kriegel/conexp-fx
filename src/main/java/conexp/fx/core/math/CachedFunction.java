package conexp.fx.core.math;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class CachedFunction<T, R> implements Function<T, R> {

  private final Map<T, R> cache = new ConcurrentHashMap<>();

  protected CachedFunction() {
    super();
  }

  @Override
  public final R apply(final T t) {
    return cache.computeIfAbsent(t, this::compute);
  }

  protected abstract R compute(T t);

}
