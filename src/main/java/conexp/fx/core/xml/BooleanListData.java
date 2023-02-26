package conexp.fx.core.xml;

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


import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class BooleanListData extends ListData<Boolean> {

  public BooleanListData(final String key, final String subkey, final List<Boolean> value) throws NullPointerException,
      IndexOutOfBoundsException {
    super(Datatype.BOOLEAN_LIST, key, subkey, value);
  }

  public BooleanListData(final String key, final String subkey, final Boolean... values) throws NullPointerException,
      IndexOutOfBoundsException {
    this(key, subkey, Arrays.asList(values));
  }

  public BooleanListData(final String key, final String subkey, final Void v, final List<String> value)
      throws NullPointerException, IndexOutOfBoundsException {
    this(key, subkey, Lists.transform(value, STRING_TO_BOOLEAN_FUNCTION));
  }

  public BooleanListData(final String key, final String subkey, final Void v, final String... values)
      throws NullPointerException, IndexOutOfBoundsException {
    this(key, subkey, Lists.transform(Arrays.asList(values), STRING_TO_BOOLEAN_FUNCTION));
  }

  private static final Function<String, Boolean> STRING_TO_BOOLEAN_FUNCTION = new Function<String, Boolean>() {

                                                                              @Override
                                                                              public final Boolean apply(
                                                                                  final String value) {
                                                                                return Boolean.valueOf(value);
                                                                              }
                                                                            };

  @Override
  public final boolean add(Boolean value) {
    return this.value.add(value);
  }

  @Override
  public final void add(int index, Boolean value) {
    this.value.add(index, value);
  }

  @Override
  public final Boolean set(int index, Boolean value) {
    return this.value.set(index, value);
  }

  @Override
  public final Boolean get(int index) {
    return value.get(index);
  }

  @Override
  public final Boolean remove(int index) {
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
  public final boolean addAll(Collection<? extends Boolean> collection) {
    return value.addAll(collection);
  }

  @Override
  public final boolean addAll(int index, Collection<? extends Boolean> collection) {
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
  public final Iterator<Boolean> iterator() {
    return value.iterator();
  }

  @Override
  public final ListIterator<Boolean> listIterator() {
    return value.listIterator();
  }

  @Override
  public final ListIterator<Boolean> listIterator(int index) {
    return value.listIterator(index);
  }

  @Override
  public final List<Boolean> subList(int fromIndex, int toIndex) {
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
