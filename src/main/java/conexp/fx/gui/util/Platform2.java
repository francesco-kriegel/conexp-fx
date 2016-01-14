package conexp.fx.gui.util;

import java.util.concurrent.CountDownLatch;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import javafx.application.Platform;

public final class Platform2 {

  public static final void runOnFXThread(final Runnable runnable) {
    if (Platform.isFxApplicationThread())
      runnable.run();
    else
      Platform.runLater(runnable);
  }

  public static final void runOnFXThreadAndWait(final Runnable runnable) throws InterruptedException {
    if (Platform.isFxApplicationThread())
      runnable.run();
    else {
      final CountDownLatch cdl = new CountDownLatch(1);
      Platform.runLater(() -> {
        runnable.run();
        cdl.countDown();
      });
      cdl.await();
    }
  }

  public static final void runOnFXThreadAndWaitTryCatch(final Runnable runnable) {
    try {
      runOnFXThreadAndWait(runnable);
    } catch (InterruptedException __) {}
  }

}
