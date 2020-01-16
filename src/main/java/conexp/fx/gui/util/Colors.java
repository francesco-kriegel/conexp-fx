package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2020 Francesco Kriegel
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


import javafx.scene.paint.Color;

public final class Colors {

  public static final Color TUD_DARKBLUE       = Color.rgb(11, 42, 81);
  public static final Color TUD_BLUE           = Color.rgb(78, 100, 126);
  public static final Color TUD_LIGHTBLUE      = Color.rgb(164, 174, 184);
  public static final Color TUD_EXTRALIGHTBLUE = Color.rgb(204, 214, 223);

  public static final String toCSSColorString(final Color color) {
    return "#" + color.toString().substring(2, 8);
  }

  public static Color fromCSSColorString(String string) {
    return Color.rgb(
        Integer.parseInt(string.substring(1, 3), 16),
        Integer.parseInt(string.substring(3, 5), 16),
        Integer.parseInt(string.substring(5, 7), 16));
  }

}
