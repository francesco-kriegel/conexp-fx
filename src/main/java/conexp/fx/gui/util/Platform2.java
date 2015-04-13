package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import javafx.application.Platform;

public final class Platform2
{
  public static final void runOnFXThread(final Runnable runnable)
  {
    if (Platform.isFxApplicationThread())
      runnable.run();
    else
      Platform.runLater(runnable);
  }
}
