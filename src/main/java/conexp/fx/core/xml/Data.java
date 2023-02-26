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


import java.util.Iterator;
import java.util.List;
import java.util.Map;

import conexp.fx.core.collections.Collections3;

public class Data<T> {

  protected final Datatype type;
  protected final String   key;
  protected T              value;

  public Data(final Datatype type, final String key, final T value) throws NullPointerException,
      IndexOutOfBoundsException {
    super();
    if (type == null)
      throw new NullPointerException("Unable to create data without type");
    if (key == null)
      throw new IndexOutOfBoundsException("Unable to create data without keys");
//    if (value == null)
//      throw new NullPointerException("Unable to create data without value");
    this.type = type;
    this.key = key;
    this.value = value;
  }

  @Override
  public boolean equals(Object object) {
    if (object == null)
      return false;
    if (!(object instanceof Data))
      return false;
    final Data<?> other = (Data<?>) object;
    if (!this.type.equals(other.type))
      return false;
    if (!this.key.equals(other.key))
      return false;
    if (!this.value.equals(other.value))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    return 1 + 2 * type.hashCode() + 3 * key.hashCode() + 5 * value.hashCode();
  }

  @Override
  public String toString() {
    return toString("");
  }

  private static final String PREFIX = "| ";

  private final String toString(final String prefix) {
    switch (type) {
    case STRING:
      return prefix + key + " (" + type + ") = \"" + value.toString() + "\"";
    case BOOLEAN:
    case INTEGER:
    case BOOLEAN_LIST:
    case INTEGER_LIST:
    case STRING_LIST:
      return prefix + key + " (" + type + ") = " + value.toString();
    case METADATA:
    case COMPOUND:
      return prefix + key + " (" + type + ")\r\n";
    case COMPOUND_LIST:
      return prefix + key + " (" + type + ")"
          + toString(prefix + PREFIX, this.toCompoundListData().getSubkey(), this.toCompoundListData().getValue());
    default:
      return prefix + key + " (" + type + ")\r\n";
    }
  }

  private final String toString(final String prefix, final String subkey, final List<Map<String, Data<?>>> list) {
    String string = "";
    for (Map<String, Data<?>> map : list)
      string += "\r\n" + prefix + subkey + "\r\n" + toString(prefix + PREFIX, map);
    return string;
  }

  private final String toString(final String prefix, final Map<String, Data<?>> map) {
    final Iterator<String> keys = Collections3.sort(map.keySet()).iterator();
    String string = "";
    if (keys.hasNext())
      string += map.get(keys.next()).toString(prefix);
    while (keys.hasNext())
      string += "\r\n" + map.get(keys.next()).toString(prefix);
    return string;
  }

  public final Datatype getType() {
    return type;
  }

  public final String getKey() {
    return new String(key);
  }

  public final T getValue() {
    return value;
  }

  public final T setValue(T value) throws NullPointerException {
    if (value == null)
      throw new NullPointerException("Unable to set null value on data");
    final T previous = this.value;
    this.value = value;
    return previous;
  }

  public final boolean isBooleanData() {
    return getType().equals(Datatype.BOOLEAN);
  }

  public final BooleanData toBooleanData() throws ClassCastException {
    if (!isBooleanData())
      throw new ClassCastException("Cannot cast data of type " + type + " to " + Datatype.BOOLEAN);
    return (BooleanData) this;
  }

  public final Boolean getBooleanValue() {
    return toBooleanData().getValue();
  }

  public final void setBooleanValue(final Boolean value) {
    toBooleanData().setValue(value);
  }

  public final boolean isIntegerData() {
    return getType().equals(Datatype.INTEGER);
  }

  public final IntegerData toIntegerData() throws ClassCastException {
    if (!isIntegerData())
      throw new ClassCastException("Cannot cast data of type " + type + " to " + Datatype.INTEGER);
    return (IntegerData) this;
  }

  public final Integer getIntegerValue() {
    return toIntegerData().getValue();
  }

  public final void setIntegerValue(final Integer value) {
    toIntegerData().setValue(value);
  }

  public final boolean isFloatData() {
    return getType().equals(Datatype.FLOAT);
  }

  public final FloatData toFloatData() throws ClassCastException {
    if (!isFloatData())
      throw new ClassCastException("Cannot cast data of type " + type + " to " + Datatype.FLOAT);
    return (FloatData) this;
  }

  public final Float getFloatValue() {
    return toFloatData().getValue();
  }

  public final void setFloatValue(final Float value) {
    toFloatData().setValue(value);
  }

  public final boolean isStringData() {
    return getType().equals(Datatype.STRING);
  }

  public final StringData toStringData() throws ClassCastException {
    if (!isStringData())
      throw new ClassCastException("Cannot cast data of type " + type + " to " + Datatype.STRING);
    return (StringData) this;
  }

  public final String getStringValue() {
    return toStringData().getValue();
  }

  public final void setStringValue(final String value) {
    toStringData().setValue(value);
  }

  public final boolean isCompoundData() {
    return getType().equals(Datatype.COMPOUND);
  }

  public final CompoundData toCompoundData() throws ClassCastException {
    if (!isCompoundData())
      throw new ClassCastException("Cannot cast data of type " + type + " to " + Datatype.COMPOUND);
    return (CompoundData) this;
  }

  public final Map<String, Data<?>> getCompoundValue() {
    return toCompoundData().getValue();
  }

  public final boolean isListData() {
    return isBooleanListData() || isIntegerListData() || isStringListData() || isCompoundListData();
  }

  public final ListData<?> toListData() throws ClassCastException {
    if (!isListData())
      throw new ClassCastException("Cannot cast data of type " + type + " to LIST");
    return (ListData<?>) this;
  }

  public final List<?> getListValue() {
    return toListData().getValue();
  }

  public final boolean isBooleanListData() {
    return getType().equals(Datatype.BOOLEAN_LIST);
  }

  public final BooleanListData toBooleanListData() throws ClassCastException {
    if (!isBooleanListData())
      throw new ClassCastException("Cannot cast data of type " + type + " to " + Datatype.BOOLEAN_LIST);
    return (BooleanListData) this;
  }

  public final List<Boolean> getBooleanListValue() {
    return toBooleanListData().getValue();
  }

  public final boolean isIntegerListData() {
    return getType().equals(Datatype.INTEGER_LIST);
  }

  public final IntegerListData toIntegerListData() throws ClassCastException {
    if (!isIntegerListData())
      throw new ClassCastException("Cannot cast data of type " + type + " to " + Datatype.INTEGER_LIST);
    return (IntegerListData) this;
  }

  public final List<Integer> getIntegerListValue() {
    return toIntegerListData().getValue();
  }

  public final boolean isFloatListData() {
    return getType().equals(Datatype.FLOAT_LIST);
  }

  public final FloatListData toFloatListData() throws ClassCastException {
    if (!isFloatListData())
      throw new ClassCastException("Cannot cast data of type " + type + " to " + Datatype.FLOAT_LIST);
    return (FloatListData) this;
  }

  public final List<Float> getFloatListValue() {
    return toFloatListData().getValue();
  }

  public final boolean isStringListData() {
    return getType().equals(Datatype.STRING_LIST);
  }

  public final StringListData toStringListData() throws ClassCastException {
    if (!isStringListData())
      throw new ClassCastException("Cannot cast data of type " + type + " to " + Datatype.STRING_LIST);
    return (StringListData) this;
  }

  public final List<String> getStringListValue() {
    return toStringListData().getValue();
  }

  public final boolean isCompoundListData() {
    return getType().equals(Datatype.COMPOUND_LIST);
  }

  public final CompoundListData toCompoundListData() throws ClassCastException {
    if (!isCompoundListData())
      throw new ClassCastException("Cannot cast data of type " + type + " to " + Datatype.COMPOUND_LIST);
    return (CompoundListData) this;
  }

  public final List<Map<String, Data<?>>> getCompoundListValue() {
    return toCompoundListData().getValue();
  }

  public final boolean isMetadata() {
    return getType().equals(Datatype.METADATA);
  }

  public final boolean isDocument() {
    return getType().equals(Datatype.DOCUMENT);
  }

}
