package conexp.fx.core.util;

/*-
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
