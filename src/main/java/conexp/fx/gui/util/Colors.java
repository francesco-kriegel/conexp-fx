package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX - Graphical User Interface
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
