package conexp.fx.core.collections.map;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ComputationException;

public abstract class FastCache<K, V> {

  private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<K, V>();

  public FastCache() {}

  protected abstract V computeValue(K key) throws ComputationException;

  public V get(K key) throws ComputationException {
    if (!map.containsKey(key))
      map.put(key, computeValue(key));
    return map.get(key);
  }

}
