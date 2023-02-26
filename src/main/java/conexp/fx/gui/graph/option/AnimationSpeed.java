package conexp.fx.gui.graph.option;

/*
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

import conexp.fx.core.util.Constants;
import javafx.util.Duration;

public enum AnimationSpeed {
  OFF(0, "Off"),
  FASTESTEST(1, "Fastestest"),
  FASTESTER(2, "Fastester"),
  FASTEST(3, "Fastest"),
  FASTER(5, "Faster"),
  FAST(7, "Fast"),
  DEFAULT(10, "Default"),
  SLOW(14, "Slow"),
  SLOWER(19, "Slower"),
  SLOWEST(25, "Slowest"),
  SLOWESTER(32, "Slowester"),
  SLOWESTEST(40, "Slowestest");

  public final int      n;
  public final String   title;
  public final Duration frameSize;

  private AnimationSpeed(final int n, final String title) throws IllegalArgumentException {
    this.n = n;
    this.title = title;
    if (n < 0)
      throw new IllegalArgumentException("Unable to set negative frame size.");
    else if (n == 0)
      this.frameSize = Duration.ONE;
    else
      this.frameSize = Duration.millis(Constants.FRAME_SIZE_MILLIS * n);
  }

  public String toString() {
    return title;
  }
}
