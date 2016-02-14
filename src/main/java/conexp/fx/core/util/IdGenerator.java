package conexp.fx.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

public final class IdGenerator {

  private static final IdGenerator             defaultKey = new IdGenerator();
  private static final Map<Object, AtomicLong> nextIds    = new ConcurrentHashMap<Object, AtomicLong>();

  private IdGenerator() {}

  public static final long getNextId() {
    return getNextId(defaultKey);
  }

  public static final long getNextId(final Object key) {
    synchronized (key) {
      return nextIds.computeIfAbsent(key, __ -> new AtomicLong(0l)).getAndIncrement();
    }
  }
}
