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
