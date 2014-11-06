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