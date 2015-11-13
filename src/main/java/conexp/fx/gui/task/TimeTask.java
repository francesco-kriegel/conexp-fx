package conexp.fx.gui.task;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import conexp.fx.gui.dataset.Dataset;
import conexp.fx.gui.util.Platform2;

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

  public static final TimeTask<Void> create(
      final Dataset dataset,
      final String title,
      final RunnableWithException runnable,
      final boolean onFXThread) {
    return create(dataset, title, runnable.toCallable(), onFXThread);
  }

  public static final <T> TimeTask<T> create(final Dataset dataset, final String title, final Callable<T> callable) {
    return create(dataset, title, callable, false);
  }

  public static final <T> TimeTask<T>
      create(final Dataset dataset, final String title, final Callable<T> callable, final boolean onFXThread) {
    return new TimeTask<T>(dataset, title, onFXThread) {

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

  @SafeVarargs
  public static final TimeTask<Void> compose(final Dataset dataset, final String title, final TimeTask<Void>... tasks) {
    return new TimeTask<Void>(dataset, title) {

      @Override
      protected Void call() throws Exception {
        for (TimeTask<Void> task : Arrays.asList(tasks)) {
          task.progressProperty().addListener((__, ___, p) -> updateProgress(p.doubleValue(), 1d));
          task.messageProperty().addListener((__, ___, m) -> updateMessage(m));
//          task.exceptionProperty().addListener((__, ___, e) -> setException(e));
          task.call();
        }
        return null;
      }
    };
  }

  public static final <T> TimeTask<T> encapsulateTaskOnFXThread(final TimeTask<T> task) {
    return new TimeTask<T>(task.getDataset(), task.getTitle()) {

      @Override
      protected T call() throws Exception {
        final CountDownLatch cdl = new CountDownLatch(1);
        task.progressProperty().addListener((____, _____, p) -> updateProgress(p.doubleValue(), 1d));
        task.messageProperty().addListener((____, _____, m) -> updateMessage(m));
        Platform2.runOnFXThread(() -> {
          task.run();
          cdl.countDown();
        });
        cdl.await();
        return task.get();
      }
    };
  }

  private final ReadOnlyLongWrapper runTimeMillis = new ReadOnlyLongWrapper(0l);
  private final Dataset             dataset;
  private final boolean             onFXThread;

  public TimeTask(final String title) {
    this(null, title);
  }

  public TimeTask(final Dataset dataset, final String title) {
    this(dataset, title, false);
  }

  private static int _n = 0;
  public int         n;

  public TimeTask(final Dataset dataset, final String title, final boolean onFXThread) {
    super();
    n = _n++;
    this.dataset = dataset;
    if (dataset != null)
      updateTitle(dataset.id.get() + " - " + title);
    else
      updateTitle(title);
    this.onFXThread = onFXThread;
  }

  public final Dataset getDataset() {
    return dataset;
  }

  public final boolean onFXThread() {
    return onFXThread;
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
