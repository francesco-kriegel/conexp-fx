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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.nodes.Element;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import conexp.fx.core.collections.Pair;

public abstract class AbstractCompoundData extends Data<Map<String, Data<?>>> implements Map<String, Data<?>> {

  private static final Function<Element, String> ELEMENT_TO_TEXT_FUNCTION = new Function<Element, String>() {

    public final String apply(final Element element) {
      return element.text();
    }
  };

  public AbstractCompoundData(final Datatype type, final String key) {
    super(type, key, new ConcurrentHashMap<String, Data<?>>());
  }

  public AbstractCompoundData(final Datatype type, final String key, final Map<String, Data<?>> value) {
    super(type, key, new ConcurrentHashMap<String, Data<?>>(value));
  }

  public AbstractCompoundData(final Datatype type, final String key, final Element element, final Metadata metadata)
      throws NullPointerException, IndexOutOfBoundsException {
    super(type, key, readDataFromElement(element, metadata));
  }

  protected synchronized static final Map<String, Data<?>>
      readDataFromElement(final Element element, final Metadata metadata) {
    final Map<String, Data<?>> map = new ConcurrentHashMap<String, Data<?>>();
    for (Map.Entry<String, Pair<Datatype, Metadata>> entry : metadata.getKeyDatatypeMap().entrySet()) {
      final String key = entry.getKey();
      final Datatype type = entry.getValue().first();
      final Metadata _metadata = entry.getValue().second();
      final Element data = JsoupUtil.firstElement(element, Key.toArray(key));
      switch (type) {
      case BOOLEAN:
        map.put(key, new BooleanData(key, data.text()));
        break;
      case INTEGER:
        map.put(key, new IntegerData(key, data.text()));
        break;
      case FLOAT:
        map.put(key, new FloatData(key, data.text()));
        break;
      case STRING:
        try {
          map.put(key, new StringData(key, data.text()));
        } catch (Exception e) {
          System.err.println(data);
          e.printStackTrace();
        }
        break;
      case COMPOUND:
        map.put(key, new CompoundData(key, data, entry.getValue().second()));
        break;
      case BOOLEAN_LIST:
        map.put(key, new BooleanListData(key, _metadata.getSubkey(), null, readStringDataFromElement(data, _metadata)));
        break;
      case INTEGER_LIST:
        map.put(key, new IntegerListData(key, _metadata.getSubkey(), null, readStringDataFromElement(data, _metadata)));
        break;
      case FLOAT_LIST:
        map.put(key, new FloatListData(key, _metadata.getSubkey(), null, readStringDataFromElement(data, _metadata)));
        break;
      case STRING_LIST:
        map.put(key, new StringListData(key, _metadata.getSubkey(), readStringDataFromElement(data, _metadata)));
        break;
      case COMPOUND_LIST:
        map.put(key, new CompoundListData(key, _metadata.getSubkey(), data, _metadata));
      case METADATA:
      default:
      }
    }
    return map;
  }

  protected static List<String> readStringDataFromElement(final Element element, final Metadata metadata) {
    List<String> list = new LinkedList<String>();
    for (String value : Iterables
        .transform(JsoupUtil.childrenByTag(element, metadata.getSubkey()), ELEMENT_TO_TEXT_FUNCTION))
      list.add(value);
    return Collections.synchronizedList(list);
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
  public final void clear() {
    value.clear();
  }

  @Override
  public final boolean containsKey(Object key) {
    return value.containsKey(key);
  }

  @Override
  public final boolean containsValue(Object value) {
    return this.value.containsValue(value);
  }

  @Override
  public final Data<?> get(Object key) {
    return value.get(key);
  }

  @Override
  public final Data<?> put(String key, Data<?> value) {
    return this.value.put(key, value);
  }

  @Override
  public final void putAll(Map<? extends String, ? extends Data<?>> m) {
    value.putAll(m);
  }

  @Override
  public final Data<?> remove(Object key) {
    return value.remove(key);
  }

  @Override
  public final Set<String> keySet() {
    return value.keySet();
  }

  @Override
  public final Collection<Data<?>> values() {
    return value.values();
  }

  @Override
  public final Set<Map.Entry<String, Data<?>>> entrySet() {
    return value.entrySet();
  }

}
