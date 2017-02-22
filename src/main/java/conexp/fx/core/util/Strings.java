package conexp.fx.core.util;

public final class Strings {

  public static final String firstCharacterInUpperCase(final String string) {
    if (string.isEmpty())
      return string;
    return string.substring(0, 1).toUpperCase() + string.substring(1);
  }

  public static final int countOccurences(final String string, final String substring) {
    int n = 0;
    int pos = 0;
    final int len = string.length();
    while (pos != -1 && pos < len) {
      pos = string.indexOf(substring, pos + 1);
      if (pos != -1)
        n++;
    }
    return n;
  }

}
