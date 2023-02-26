package conexp.fx.core.collections;

/*-
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

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public final class ConcurrentHashSetMultimap<K, V> implements SetMultimap<K, V> {

  private final Map<K, Set<V>> map;

  public ConcurrentHashSetMultimap() {
    super();
    this.map = new ConcurrentHashMap<>();
  }

  @Override
  public int size() {
    return this.map.keySet().parallelStream().map(key -> map.get(key).size()).reduce(0, Integer::sum);
  }

  @Override
  public boolean isEmpty() {
    return this.map.values().parallelStream().allMatch(Collection::isEmpty);
  }

  @Override
  public boolean containsKey(Object key) {
    return this.map.containsKey(key) && !this.map.get(key).isEmpty();
  }

  @Override
  public boolean containsValue(Object value) {
    return this.map.values().parallelStream().anyMatch(set -> set.contains(value));
  }

  @Override
  public boolean containsEntry(Object key, Object value) {
    return this.map.containsKey(key) && this.map.get(key).contains(value);
  }

  @Override
  public boolean put(K key, V value) {
    return this.map.computeIfAbsent(key, __ -> Sets.newConcurrentHashSet()).add(value);
  }

  @Override
  public boolean remove(Object key, Object value) {
    return this.map.containsKey(key) && this.map.get(key).remove(value);
  }

  @Override
  public boolean putAll(K key, Iterable<? extends V> values) {
    final Set<V> set = this.map.computeIfAbsent(key, __ -> Sets.newConcurrentHashSet());
    final AtomicBoolean result = new AtomicBoolean(false);
    StreamSupport.stream(values.spliterator(), true).forEach(value -> {
      if (set.add(value))
        result.lazySet(true);
    });
    return result.get();
  }

  @Override
  public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
    final AtomicBoolean result = new AtomicBoolean(false);
    multimap.entries().parallelStream().forEach(entry -> {
      if (this.put((K) entry.getKey(), (V) entry.getValue()))
        result.lazySet(true);
    });
//    multimap.keySet().parallelStream().forEach(key -> {
//      if (this.putAll((K) key, Collections2.transform(multimap.get((K) key), element -> (V) element)))
//        result.lazySet(true);
//    });
    return result.get();
  }

  @Override
  public void clear() {
    this.map.clear();
  }

  @Override
  public Set<K> keySet() {
    return this.map.keySet();
  }

  @Override
  public Multiset<K> keys() {
    throw new UnsupportedOperationException("Operation is not implemented.");
  }

  @Override
  public Collection<V> values() {
    return new AbstractCollection<V>() {

      @Override
      public Iterator<V> iterator() {
        return Iterators
            .concat(
                Collections2
                    .transform(
                        ConcurrentHashSetMultimap.this.map.keySet(),
                        key -> ConcurrentHashSetMultimap.this.map.get(key).iterator())
                    .iterator());
      }

      @Override
      public int size() {
        return ConcurrentHashSetMultimap.this.size();
      }

    };
  }

  @Override
  public Set<V> get(K key) {
    return this.map.get(key);
  }

  @Override
  public Set<V> removeAll(Object key) {
    return this.map.remove(key);
  }

  @Override
  public Set<V> replaceValues(K key, Iterable<? extends V> values) {
    final Set<V> set = this.map.computeIfAbsent(key, __ -> Sets.newConcurrentHashSet());
    StreamSupport.stream(values.spliterator(), true).forEach(value -> {
      set.add(value);
    });
    return set;
  }

  @Override
  public Set<Entry<K, V>> entries() {
    return new AbstractSet<Entry<K, V>>() {

      @Override
      public Iterator<Entry<K, V>> iterator() {
        return Iterators
            .concat(
                Collections2
                    .transform(
                        ConcurrentHashSetMultimap.this.map.keySet(),
                        key -> Iterators
                            .transform(
                                ConcurrentHashSetMultimap.this.map.get(key).iterator(),
                                value -> new AbstractMap.SimpleEntry<>(key, value)))
                    .iterator());
      }

      @Override
      public int size() {
        return ConcurrentHashSetMultimap.this.size();
      }
    };
  }

  @Override
  public Map<K, Collection<V>> asMap() {
    return Maps.transformValues(this.map, set -> (Collection<V>) set);
  }

}
