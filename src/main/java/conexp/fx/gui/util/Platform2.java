package conexp.fx.gui.util;

import java.util.concurrent.CountDownLatch;

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
