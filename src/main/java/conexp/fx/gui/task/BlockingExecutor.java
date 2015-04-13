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

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import conexp.fx.gui.util.Platform2;

public final class BlockingExecutor {

//  public void clearFinished() {
//    scheduledTasks.removeIf(t -> t.isDone());
//  }

//  public final LongProperty                        executionTimeMillis     = new SimpleLongProperty(0l);
  public final DoubleBinding                       overallProgressBinding;
  public final BooleanBinding                      isIdleBinding;
  public final ObservableList<BlockingTask>        scheduledTasks          =
                                                                               FXCollections
                                                                                   .observableList(Collections
                                                                                       .synchronizedList(new LinkedList<BlockingTask>()));
  public final ConcurrentLinkedQueue<BlockingTask> taskQueue               = new ConcurrentLinkedQueue<BlockingTask>();
  public final Property<BlockingTask>              currentTaskProperty     = new SimpleObjectProperty<BlockingTask>(
                                                                               BlockingTask.NULL);
  public final ThreadPoolExecutor                  tpe                     = new ThreadPoolExecutor(
                                                                               Runtime
                                                                                   .getRuntime()
                                                                                   .availableProcessors(),
                                                                               Runtime
                                                                                   .getRuntime()
                                                                                   .availableProcessors(),
                                                                               60,
                                                                               TimeUnit.SECONDS,
                                                                               new LinkedBlockingQueue<Runnable>());
  public final IntegerProperty                     doneTasksProperty       = new SimpleIntegerProperty(0);
  public final DoubleProperty                      currentProgressProperty = new SimpleDoubleProperty(1d);
  public final IntegerProperty                     scheduledTasksProperty  = new SimpleIntegerProperty(0);

  public BlockingExecutor() {
    currentTaskProperty.getValue().run();
    tpe.prestartAllCoreThreads();
    overallProgressBinding = new DoubleBinding() {

      {
        super.bind(
            doneTasksProperty,
            currentProgressProperty,
            scheduledTasksProperty);
      }

      protected final double computeValue() {
        final double doneTasks = doneTasksProperty.get();
        final double p = doneTasks + currentProgressProperty.get();
        final double n = doneTasks + 1 + scheduledTasksProperty.get();
        return p / n;
      }
    };
    overallProgressBinding.addListener(new ChangeListener<Number>() {

      public final void changed(
          final ObservableValue<? extends Number> observable,
          final Number oldProgress,
          final Number newProgress) {
        if (newProgress.doubleValue() == 1d && scheduledTasksProperty.get() == 0
            && currentTaskProperty.getValue().isDone()) {
          doneTasksProperty.set(0);
          currentProgressProperty.unbind();
          currentProgressProperty.set(1d);
        }
      }
    });
    isIdleBinding = new BooleanBinding() {

      {
        super.bind(
            currentProgressProperty,
            scheduledTasksProperty);
      }

      protected boolean computeValue() {
        return scheduledTasksProperty.get() == 0 && currentProgressProperty.get() == 1d;
      }
    };
    currentTaskProperty.addListener(new ChangeListener<BlockingTask>() {

      public void changed(
          final ObservableValue<? extends BlockingTask> observable,
          final BlockingTask oldTask,
          final BlockingTask newTask) {
        currentProgressProperty.bind(newTask.progressProperty());
        newTask.stateProperty().addListener(
            new ChangeListener<State>() {

              @SuppressWarnings("incomplete-switch")
              public final void changed(
                  final ObservableValue<? extends State> observable,
                  final State oldState,
                  final State newState) {
                switch (newState) {
                case SUCCEEDED:
                case CANCELLED:
                case FAILED:
//              executionTimeMillis.set(executionTimeMillis.get() + newTask.runTimeMillis.get());
//              executionsTimeMillisMap.add(Pair.of(newTask.titleProperty().get(), newTask.runTimeMillis()));
                  doneTasksProperty.set(doneTasksProperty.get() + 1);
                  BlockingExecutor.this.next();
                  break;
                }
              };
            });
        tpe.submit(newTask);
      }
    });
  }

  public final void submit(final BlockingTask task) {
    Platform2.runOnFXThread(() -> {
      synchronized (taskQueue) {
        scheduledTasks.add(task);
        if (currentTaskProperty.getValue().isDone() && taskQueue.isEmpty())
          currentTaskProperty.setValue(task);
        else {
          scheduledTasksProperty.set(scheduledTasksProperty.get() + 1);
          taskQueue.offer(task);
//          scheduledTasks.add(task);
        }
      }
    });
  }

  private final void next() {
    synchronized (taskQueue) {
      if (taskQueue.isEmpty()) {
//        currentTaskProperty.setValue(null);
        return;
      }
      currentTaskProperty.setValue(taskQueue.poll());
      scheduledTasksProperty.set(scheduledTasksProperty.get() - 1);
    }
  }
}
