package conexp.fx.core.math;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

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
