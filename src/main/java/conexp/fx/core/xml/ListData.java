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
