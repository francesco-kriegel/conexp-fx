package conexp.fx.core.xml;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jsoup.nodes.Element;


public class CompoundListData extends ListData<Map<String, Data<?>>> {

  public CompoundListData(final String key, final String subkey) {
    super(Datatype.COMPOUND_LIST, key, subkey, Collections.synchronizedList(new ArrayList<Map<String, Data<?>>>()));
  }

  public CompoundListData(final String key, final String subkey, final List<Map<String, Data<?>>> value) {
    super(Datatype.COMPOUND_LIST, key, subkey, Collections.synchronizedList(new ArrayList<Map<String, Data<?>>>(value)));
  }

  public CompoundListData(final String key, final String subkey, final Element element, final Metadata metadata)
      throws NullPointerException, IndexOutOfBoundsException {
    super(Datatype.COMPOUND_LIST, key, subkey, readListDataFromElement(element, subkey, metadata));
  }

  private synchronized static final List<Map<String, Data<?>>> readListDataFromElement(
      final Element element,
      final String subkey,
      final Metadata metadata) {
    final List<Map<String, Data<?>>> list = new LinkedList<Map<String, Data<?>>>();
    for (final Element subElement : JsoupUtil.childrenByTag(element, subkey))
      list.add(AbstractCompoundData.readDataFromElement(subElement, metadata));
    return Collections.synchronizedList(list);
  }

  @Override
  public final boolean add(Map<String,Data<?>> value) {
    return this.value.add(value);
  }

  @Override
  public final void add(int index, Map<String,Data<?>> value) {
    this.value.add(index, value);
  }

  @Override
  public final Map<String,Data<?>> set(int index, Map<String,Data<?>> value) {
    return this.value.set(index, value);
  }

  @Override
  public final Map<String,Data<?>> get(int index) {
    return value.get(index);
  }

  @Override
  public final Map<String,Data<?>> remove(int index) {
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
  public final boolean addAll(Collection<? extends Map<String,Data<?>>> collection) {
    return value.addAll(collection);
  }

  @Override
  public final boolean addAll(int index, Collection<? extends Map<String,Data<?>>> collection) {
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
  public final Iterator<Map<String,Data<?>>> iterator() {
    return value.iterator();
  }

  @Override
  public final ListIterator<Map<String,Data<?>>> listIterator() {
    return value.listIterator();
  }

  @Override
  public final ListIterator<Map<String,Data<?>>> listIterator(int index) {
    return value.listIterator(index);
  }

  @Override
  public final List<Map<String, Data<?>>> subList(int fromIndex, int toIndex) {
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
