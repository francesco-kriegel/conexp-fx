package conexp.fx.core.concurrent;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import conexp.fx.core.collections.pair.Pair;

public final class BlockingExecutor {

  public final LongProperty                        executionTimeMillis     = new SimpleLongProperty(0l);
  public final ObservableList<Pair<String, Long>>  executionsTimeMillisMap =
                                                                               FXCollections
                                                                                   .observableList(new LinkedList<Pair<String, Long>>());
  public final DoubleBinding                       overallProgressBinding;
  public final BooleanBinding                      isIdleBinding;
  public final ConcurrentLinkedQueue<BlockingTask> taskQueue               = new ConcurrentLinkedQueue<BlockingTask>();
  public final Property<BlockingTask>              currentTaskProperty     = new SimpleObjectProperty<BlockingTask>(
                                                                               BlockingTask.NULL);
  public final ThreadPoolExecutor                  exec                    = new ThreadPoolExecutor(
                                                                               0,
                                                                               1,
                                                                               1,
                                                                               TimeUnit.SECONDS,
                                                                               new LinkedBlockingQueue<Runnable>());
  public final IntegerProperty                     doneTasksProperty       = new SimpleIntegerProperty(0);
  public final DoubleProperty                      currentProgressProperty = new SimpleDoubleProperty(1d);
  public final IntegerProperty                     scheduledTasksProperty  = new SimpleIntegerProperty(0);

  public BlockingExecutor() {
    currentTaskProperty.getValue().run();
    exec.prestartAllCoreThreads();
    overallProgressBinding = new DoubleBinding() {

      {
        super.bind(doneTasksProperty, currentProgressProperty, scheduledTasksProperty);
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
        super.bind(currentProgressProperty, scheduledTasksProperty);
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
        newTask.stateProperty().addListener(new ChangeListener<State>() {

          @SuppressWarnings("incomplete-switch")
          public final void changed(
              final ObservableValue<? extends State> observable,
              final State oldState,
              final State newState) {
            switch (newState) {
            case SUCCEEDED:
            case CANCELLED:
            case FAILED:
              executionTimeMillis.set(executionTimeMillis.get() + newTask.runTimeMillis());
              executionsTimeMillisMap.add(Pair.of(newTask.titleProperty().get(), newTask.runTimeMillis()));
              doneTasksProperty.set(doneTasksProperty.get() + 1);
              BlockingExecutor.this.next();
              break;
            }
          };
        });
        exec.submit(newTask);
      }
    });
  }

  public final void submit(final BlockingTask task) {
    synchronized (taskQueue) {
      if (currentTaskProperty.getValue().isDone() && taskQueue.isEmpty())
        currentTaskProperty.setValue(task);
      else {
        scheduledTasksProperty.set(scheduledTasksProperty.get() + 1);
        taskQueue.offer(task);
      }
    }
  }

  private final void next() {
    synchronized (taskQueue) {
      if (taskQueue.isEmpty())
        return;
      currentTaskProperty.setValue(taskQueue.poll());
      scheduledTasksProperty.set(scheduledTasksProperty.get() - 1);
    }
  }
}
