package conexp.fx.core.collections.map;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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
