package conexp.fx.core.xml;

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


import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class IntegerListData extends ListData<Integer> {

  public IntegerListData(final String key, final String subkey, final List<Integer> value) throws NullPointerException,
      IndexOutOfBoundsException {
    super(Datatype.INTEGER_LIST, key, subkey, value);
  }

  public IntegerListData(final String key, final String subkey, final Integer... values) throws NullPointerException,
      IndexOutOfBoundsException {
    this(key, subkey, Arrays.asList(values));
  }

  public IntegerListData(final String key, final String subkey, final Void v, final List<String> value)
      throws NullPointerException, IndexOutOfBoundsException {
    this(key, subkey, Lists.transform(value, STRING_TO_INTEGER_FUNCTION));
  }

  public IntegerListData(final String key, final String subkey, final Void v, final String... values)
      throws NullPointerException, IndexOutOfBoundsException {
    this(key, subkey, Lists.transform(Arrays.asList(values), STRING_TO_INTEGER_FUNCTION));
  }

  private static final Function<String, Integer> STRING_TO_INTEGER_FUNCTION = new Function<String, Integer>() {

                                                                              @Override
                                                                              public final Integer apply(
                                                                                  final String value) {
                                                                                return Integer.valueOf(value);
                                                                              }
                                                                            };

  @Override
  public final boolean add(Integer value) {
    return this.value.add(value);
  }

  @Override
  public final void add(int index, Integer value) {
    this.value.add(index, value);
  }

  @Override
  public final Integer set(int index, Integer value) {
    return this.value.set(index, value);
  }

  @Override
  public final Integer get(int index) {
    return value.get(index);
  }

  @Override
  public final Integer remove(int index) {
    return value.remove(index);
  }

  @Override
  public final boolean remove(Object object) {
    return value.remove(object);
  }

  @Override
  public final boolean contains(Object object) {
    return this.value.contains(object);
  }

  @Override
  public final int indexOf(Object object) {
    return this.value.indexOf(object);
  }

  @Override
  public final int lastIndexOf(Object object) {
    return value.lastIndexOf(object);
  }

  @Override
  public final boolean addAll(Collection<? extends Integer> collection) {
    return value.addAll(collection);
  }

  @Override
  public final boolean addAll(int index, Collection<? extends Integer> collection) {
    return value.addAll(index, collection);
  }

  @Override
  public final boolean removeAll(Collection<?> collection) {
    return value.removeAll(collection);
  }

  @Override
  public final boolean retainAll(Collection<?> collection) {
    return value.retainAll(collection);
  }

  @Override
  public final boolean containsAll(Collection<?> collection) {
    return value.containsAll(collection);
  }

  @Override
  public final void clear() {
    value.clear();
  }

  @Override
  public final boolean isEmpty() {
    return value.isEmpty();
  }

  @Override
  public final int size() {
    return value.size();
  }

  @Override
  public final Iterator<Integer> iterator() {
    return value.iterator();
  }

  @Override
  public final ListIterator<Integer> listIterator() {
    return value.listIterator();
  }

  @Override
  public final ListIterator<Integer> listIterator(int index) {
    return value.listIterator(index);
  }

  @Override
  public final List<Integer> subList(int fromIndex, int toIndex) {
    return value.subList(fromIndex, toIndex);
  }

  @Override
  public final Object[] toArray() {
    return value.toArray();
  }

  @Override
  public final <T> T[] toArray(T[] array) {
    return value.<T> toArray(array);
  }

}