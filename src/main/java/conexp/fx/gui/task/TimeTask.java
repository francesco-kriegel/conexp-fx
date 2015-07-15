package conexp.fx.gui.task;

import java.util.concurrent.Callable;

import conexp.fx.gui.dataset.Dataset;

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
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.concurrent.Task;

public abstract class TimeTask<T> extends Task<T> {

  @FunctionalInterface
  public static interface RunnableWithException {

    public void run() throws Exception;

    public default Callable<Void> toCallable() {
      return () -> {
        run();
        return null;
      };
    }

  }

  public static final TimeTask<Void>
      create(final Dataset dataset, final String title, final RunnableWithException runnable) {
    return create(dataset, title, runnable.toCallable());
  }

  public static final <T> TimeTask<T> create(final Dataset dataset, final String title, final Callable<T> callable) {
    return new TimeTask<T>(dataset, title) {

      @Override
      protected T call() throws Exception {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        final T result = callable.call();
        updateProgress(1d, 1d);
        return result;
      }
    };
  }

  private final ReadOnlyLongWrapper runTimeMillis = new ReadOnlyLongWrapper(0l);
  private final Dataset             dataset;

  public TimeTask(final String title) {
    this(null, title);
  }

  public TimeTask(final Dataset dataset, final String title) {
    super();
    this.dataset = dataset;
    if (dataset != null)
      updateTitle(dataset.id.get() + " - " + title);
    else
      updateTitle(title);
  }

  public final Dataset getDataset() {
    return dataset;
  }

  public ReadOnlyLongProperty runTimeMillisProperty() {
    return runTimeMillis.getReadOnlyProperty();
  }

  private long   startTimeMillis;
  private Thread timer;

  private final void startTimer() {
    startTimeMillis = System.currentTimeMillis();
    timer = new Thread(() -> {
      try {
        while (true) {
          final long currentTimeMillis = System.currentTimeMillis();
          Platform.runLater(() -> runTimeMillis.set(currentTimeMillis - startTimeMillis));
          Thread.sleep(250);
        }
      } catch (InterruptedException e) {}
    });
    timer.start();
  }

  private final void stopTimer() {
    if (timer == null)
      return;
    timer.interrupt();
    final long currentTimeMillis = System.currentTimeMillis();
    Platform.runLater(() -> runTimeMillis.set(currentTimeMillis - startTimeMillis));
  }

  @Override
  protected void running() {
    super.running();
    startTimer();
  }

  @Override
  protected void succeeded() {
    super.succeeded();
    stopTimer();
  }

  @Override
  protected void cancelled() {
    super.cancelled();
    if (getProgress() < 0)
      updateProgress(0d, 1d);
    stopTimer();
  };

  @Override
  protected void failed() {
    super.failed();
    if (getProgress() < 0)
      updateProgress(0d, 1d);
    stopTimer();
  }

}
