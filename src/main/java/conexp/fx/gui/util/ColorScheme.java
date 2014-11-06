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


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javafx.scene.paint.Color;

public final class ColorScheme {

  public static final ColorScheme        BASIC                 = new ColorScheme(
                                                                   "Basic",
                                                                   Color.BLUE,
                                                                   Color.GREEN,
                                                                   Color.ALICEBLUE,
                                                                   Color.GREENYELLOW,
                                                                   Color.RED);
  public static final ColorScheme        TUD                   = new ColorScheme("TU Dresden", Colors.TUD_DARKBLUE,
                                                               // Colors.TUD_LIGHTBLUE,
                                                                   Colors.TUD_BLUE,
                                                                   Colors.TUD_EXTRALIGHTBLUE,
                                                                   Color.rgb(250, 120, 48),
                                                                   Color.rgb(165, 74, 28));
  public static final ColorScheme        JAVA_FX               = new ColorScheme(
                                                                   "JavaFX",
                                                                   Color.rgb(0, 100, 172),
                                                                   Color.rgb(44, 44, 44),
                                                                   Color.rgb(236, 236, 236),
                                                                   Color.rgb(56, 166, 246),
                                                                   Color.rgb(100, 100, 100));
  public static final ColorScheme        WINDOWS_8             = new ColorScheme(
                                                                   "Windows 8",
                                                                   Color.rgb(25, 121, 202),
                                                                   Color.rgb(41, 140, 225),
                                                                   Color.rgb(235, 235, 235),
                                                                   Color.rgb(0, 204, 255),
                                                                   Color.rgb(224, 67, 67));
  public static final ColorScheme        WATERLIME             = new ColorScheme(
                                                                   "Waterlime",
                                                                   Color.rgb(92, 138, 45),
                                                                   Color.rgb(0, 195, 169),
                                                                   Color.rgb(255, 255, 255),
                                                                   Color.rgb(175, 214, 135),
                                                                   Color.rgb(0, 135, 152));
  public static final ColorScheme        SALMON_ON_ICE         = new ColorScheme(
                                                                   "Salmon on Ice",
                                                                   Color.rgb(62, 69, 76),
                                                                   Color.rgb(33, 133, 197),
                                                                   Color.rgb(255, 246, 229),
                                                                   Color.rgb(126, 206, 253),
                                                                   Color.rgb(255, 127, 102));
  public static final ColorScheme        DEFAULT               = WINDOWS_8;
  private static final List<ColorScheme> availableColorSchemes = Collections
                                                                   .synchronizedList(new LinkedList<ColorScheme>());
  static {
    availableColorSchemes.add(BASIC);
    availableColorSchemes.add(TUD);
    availableColorSchemes.add(JAVA_FX);
    availableColorSchemes.add(WINDOWS_8);
    availableColorSchemes.add(WATERLIME);
    availableColorSchemes.add(SALMON_ON_ICE);
  }

  public static final List<ColorScheme> getAvailableColorSchemes() {
    return availableColorSchemes;
  }

  private final String      name;
  private final List<Color> colors = new ArrayList<Color>(5);

  public ColorScheme(
      final String name,
      final Color color1,
      final Color color2,
      final Color color3,
      final Color color4,
      final Color color5) {
    this.name = name;
    colors.add(color1);
    colors.add(color2);
    colors.add(color3);
    colors.add(color4);
    colors.add(color5);
  }

  public final String getName() {
    return name;
  }

  public final Color getColor(final int index) {
    if (index < 1 || index > 5)
      return null;
    return colors.get(index - 1);
  }
}
