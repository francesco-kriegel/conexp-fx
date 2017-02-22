package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
