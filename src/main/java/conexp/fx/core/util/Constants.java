package conexp.fx.core.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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

import javafx.util.Duration;

public class Constants {

  public static final double   FRAMES_PER_SECOND                = 100d;
  public static final double   FRAME_SIZE_MILLIS                = 1000d / FRAMES_PER_SECOND;
  public static final int      MAX_CONCEPTS                     = 2048;
  public static final int      MULTI_CLICK_DURATION             = 300;
  public static final int      UI_DELAY                         = 100;
  public static final int      ANIMATION_TIME                   = 100;
  public static final Duration ANIMATION_DURATION               = Duration.millis(ANIMATION_TIME);
  public static final double   HIDE_OPACITY                     = 0.191d;
  public static final double   SHOW_OPACITY                     = 1d;
  public static final String   CROSS_CHARACTER                  = "\u2715";
  public static final String   NO_CROSS_CHARACTER               = "\u00b7";
  public static final String   NO_CROSS_CHARACTER_BOLD          = "\u25cf";
  public static final String   UP_ARROW_CHARACTER               = "\u2197";
  public static final String   DOWN_ARROW_CHARACTER             = "\u2199";
  public static final String   BOTH_ARROW_CHARACTER             = "\u21c6";
  public static final String   BOTH_ARROW_CHARACTER_ALTERNATIVE = "\u2194";
  public static final String   DOUBLE_UP_ARROW_CHARACTER        = "\u21c9";
  public static final String   DOUBLE_DOWN_ARROW_CHARACTER      = "\u21c7";
  public static final String   DOUBLE_BOTH_ARROW_CHARACTER      = "\u21ba";
  public static final int      GENERATIONS                      = 1;
  public static final int      POPULATION                       = 64;
}
