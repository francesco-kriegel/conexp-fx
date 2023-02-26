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
import java.util.List;

public final class Key {

  public static final String[] toArray(String string) {
    return string.split("\\.");
  }

  public static final String[] toArray(List<String> list) {
    return list.toArray(new String[] {});
  }

  public static final List<String> toList(String string) {
    return toList(toArray(string));
  }

  public static final List<String> toList(String[] array) {
    return Arrays.asList(array);
  }

  public static final String toString(String... array) {
    return toString(toList(array));
  }

  public static final String toString(List<String> list) {
    if (list.isEmpty())
      return "";
    String string = list.get(0);
    for (int i = 1; i < list.size(); i++)
      string += "." + list.get(i);
    return string;
  }

  public static final String toString(String firstKey, String... keys) {
    String string = firstKey;
    for (String key : keys)
      string += "." + key;
    return string;
  }
}
