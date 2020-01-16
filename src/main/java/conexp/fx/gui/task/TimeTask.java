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
 * Copyright (C) 2010 - 2020 Francesco Kriegel
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
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.concurrent.Task;

public abstract class TimeTask<T> extends Task<T> {

  @FunctionalInterface
  public static interface RunnableWithException<E extends Exception> {

    public void run() throws E;

    public default Callable<Void> toCallable() {
      return () -> {
        run();
        return null;
      };
    }

  }

  public static final TimeTask<Void>
      create(final Dataset dataset, final String title, final RunnableWithException<?> runnable) {
    return create(dataset, title, runnable.toCallable());
  }

  public static final TimeTask<Void> create(
      final Dataset dataset,
      final String title,
      final RunnableWithException<?> runnable,
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
          updateTitle(title + " - " + task.getTitle());
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

  private final Dataset dataset;
  private final boolean onFXThread;

  public TimeTask(final String title) {
    this(null, title);
  }

  public TimeTask(final Dataset dataset, final String title) {
    this(dataset, title, false);
  }

  public TimeTask(final Dataset dataset, final String title, final boolean onFXThread) {
    super();
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

  private final ObservableTimer timer = new ObservableTimer();

  public final ReadOnlyLongProperty runTimeNanosProperty() {
    return timer.runTimeNanosProperty();
  }

  private static final class ObservableTimer {

    private final ReadOnlyLongWrapper runTimeNanos = new ReadOnlyLongWrapper(0l);
    private long                      startTimeNanos;
    private Thread                    thread;
    private final long                updateDelayMillis;

    public ObservableTimer(final long updateDelayMillis) {
      super();
      this.updateDelayMillis = updateDelayMillis;
    }

    public ObservableTimer() {
      this(250l);
    }

    public final ReadOnlyLongProperty runTimeNanosProperty() {
      return runTimeNanos.getReadOnlyProperty();
    }

    public final void start() {
      synchronized (this) {
        if (thread != null)
          return;
        startTimeNanos = System.nanoTime();
        thread = new Thread(() -> {
          try {
            while (true) {
              update();
              Thread.sleep(updateDelayMillis);
            }
          } catch (InterruptedException e) {}
        });
        thread.start();
      }
    }

    public final void stop() {
      synchronized (this) {
        if (thread == null)
          return;
        thread.interrupt();
        update();
        thread = null;
      }
    }

    public final void update() {
      if (thread == null)
        return;
      final long currentTimeNanos = System.nanoTime();
      Platform.runLater(() -> runTimeNanos.set(currentTimeNanos - startTimeNanos));
    }
  }

  @Override
  protected void running() {
    super.running();
    timer.start();
  }

  @Override
  protected void succeeded() {
    super.succeeded();
    timer.stop();
  }

  @Override
  protected void cancelled() {
    super.cancelled();
    if (getProgress() < 0)
      updateProgress(0d, 1d);
    timer.stop();
  };

  @Override
  protected void failed() {
    super.failed();
    if (getProgress() < 0)
      updateProgress(0d, 1d);
    timer.stop();
  }

}
