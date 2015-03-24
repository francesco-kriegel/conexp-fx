package conexp.fx.core.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
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


import javafx.util.Duration;

public class Constants
{
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
  public static final int      GENERATIONS                      = 0;
  public static final int      POPULATION                       = 64;
}
