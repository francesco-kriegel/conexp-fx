package conexp.fx.gui.task;

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
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;

public abstract class BlockingTask extends Task<Void> {

  public static final BlockingTask NULL          = new BlockingTask("") {

                                                   protected void _call() {}
                                                 };
  public final LongProperty        runTimeMillis = new SimpleLongProperty(0l);

  public BlockingTask(final String title) {
    super();
    updateTitle(title);
  }

  @Override
  public final Void call() {
    final long startTimeMillis = System.currentTimeMillis();
    scheduled();
    running();
    updateProgress(0d, 1d);
    final Thread t = new Thread(() -> {
      try {
        while (true) {
          final long currentTimeMillis = System.currentTimeMillis();
          Platform.runLater(() -> runTimeMillis.set(currentTimeMillis - startTimeMillis));
          Thread.sleep(250);
        }
      } catch (InterruptedException e) {}
    });
    t.start();
    try {
      _call();
    } catch (Exception e) {
      e.printStackTrace();
    }
    t.interrupt();
    final long currentTimeMillis = System.currentTimeMillis();
    Platform.runLater(() -> runTimeMillis.set(currentTimeMillis - startTimeMillis));
    updateProgress(1d, 1d);
    updateMessage("succeeded (" + runTimeMillis + "ms)");
    succeeded();
    done();
    return null;
  }

  protected abstract void _call();
}
