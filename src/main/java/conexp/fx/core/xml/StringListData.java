package conexp.fx.core.xml;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class StringListData extends ListData<String> {

  public StringListData(final String key, final String subkey, final List<String> value) throws NullPointerException,
      IndexOutOfBoundsException {
    super(Datatype.STRING_LIST, key, subkey, value);
  }

  public StringListData(final String key, final String subkey, final String... values) throws NullPointerException,
      IndexOutOfBoundsException {
    this(key, subkey, Arrays.asList(values));
  }

  @Override
  public final boolean add(String value) {
    return this.value.add(value);
  }

  @Override
  public final void add(int index, String value) {
    this.value.add(index, value);
  }

  @Override
  public final String set(int index, String value) {
    return this.value.set(index, value);
  }

  @Override
  public final String get(int index) {
    return value.get(index);
  }

  @Override
  public final String remove(int index) {
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
  public final boolean addAll(Collection<? extends String> collection) {
    return value.addAll(collection);
  }

  @Override
  public final boolean addAll(int index, Collection<? extends String> collection) {
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
  public final Iterator<String> iterator() {
    return value.iterator();
  }

  @Override
  public final ListIterator<String> listIterator() {
    return value.listIterator();
  }

  @Override
  public final ListIterator<String> listIterator(int index) {
    return value.listIterator(index);
  }

  @Override
  public final List<String> subList(int fromIndex, int toIndex) {
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
