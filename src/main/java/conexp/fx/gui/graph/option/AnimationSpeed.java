package conexp.fx.gui.graph.option;

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
