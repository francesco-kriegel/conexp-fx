package conexp.fx.core.xml;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ListData<T> extends Data<List<T>> implements List<T> {

  protected final String subkey;

  public ListData(final Datatype type, final String key, final String subkey, final List<T> value)
      throws NullPointerException, IndexOutOfBoundsException {
    super(type, key, Collections.synchronizedList(new ArrayList<T>(value)));
    this.subkey = subkey;
  }

  public final String getSubkey() {
    return subkey;
  }

  @Override
  public boolean add(T value) {
    return this.value.add(value);
  }

  @Override
  public void add(int index, T value) {
    this.value.add(index, value);
  }

  @Override
  public T set(int index, T value) {
    return this.value.set(index, value);
  }

  @Override
  public T get(int index) {
    return value.get(index);
  }

  @Override
  public T remove(int index) {
    return value.remove(index);
  }

  @Override
  public boolean remove(Object object) {
    return value.remove(object);
  }

  @Override
  public boolean contains(Object object) {
    return this.value.contains(object);
  }

  @Override
  public int indexOf(Object object) {
    return this.value.indexOf(object);
  }

  @Override
  public int lastIndexOf(Object object) {
    return value.lastIndexOf(object);
  }

  @Override
  public boolean addAll(Collection<? extends T> collection) {
    return value.addAll(collection);
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> collection) {
    return value.addAll(index, collection);
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    return value.removeAll(collection);
  }

  @Override
  public boolean retainAll(Collection<?> collection) {
    return value.retainAll(collection);
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    return value.containsAll(collection);
  }

  @Override
  public void clear() {
    value.clear();
  }

  @Override
  public boolean isEmpty() {
    return value.isEmpty();
  }

  @Override
  public int size() {
    return value.size();
  }

  @Override
  public Iterator<T> iterator() {
    return value.iterator();
  }

  @Override
  public ListIterator<T> listIterator() {
    return value.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return value.listIterator(index);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return value.subList(fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    return value.toArray();
  }

  @Override
  public <t> t[] toArray(t[] array) {
    return value.<t> toArray(array);
  }

}
