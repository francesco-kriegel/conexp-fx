package conexp.fx.gui.task;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.Dataset;
import conexp.fx.gui.dialog.ErrorDialog;
import conexp.fx.gui.util.Platform2;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class BlockingExecutor {

  public final ExecutorService              tpe                     = Executors.newWorkStealingPool();
  // new ForkJoinPool(Runtime.getRuntime().availableProcessors() - 1);
//      new ThreadPoolExecutor(
//          Runtime.getRuntime().availableProcessors() - 1,
//          Runtime.getRuntime().availableProcessors() - 1,
//          1,
//          TimeUnit.SECONDS,
//          new LinkedBlockingQueue<Runnable>());
  public final BooleanBinding               isIdleBinding;
  public final DoubleBinding                overallProgressBinding;
  public final ObservableList<TimeTask<?>>  scheduledTasks          =
      FXCollections.observableList(Collections.synchronizedList(new LinkedList<TimeTask<?>>()));
  public final Property<TimeTask<?>>        currentTaskProperty     = new SimpleObjectProperty<TimeTask<?>>();
  private final Map<Dataset, DoubleBinding> datasetProgressBindings = new ConcurrentHashMap<>();

  public BlockingExecutor() {
    overallProgressBinding = Bindings.createDoubleBinding(() -> {
      synchronized (scheduledTasks) {
        if (scheduledTasks.isEmpty())
          return 1d;
        double progress = 0d;
        for (TimeTask<?> task : scheduledTasks) {
          if (task.isDone())
            progress += 1d;
          else if (task.getProgress() > 0)
            progress += Math.max(0, Math.min(1, task.getProgress()));
        }
        return progress / (double) scheduledTasks.size();
      }
    } , scheduledTasks, currentTaskProperty);
    isIdleBinding = Bindings.createBooleanBinding(() -> overallProgressBinding.get() == 1d, overallProgressBinding);
    currentTaskProperty.addListener((__, ___, task) -> {
      synchronized (scheduledTasks) {
        if (task.isDone())
          next();
        else {
          task.exceptionProperty().addListener(
              (____, _____, exception) -> new ErrorDialog(ConExpFX.instance.primaryStage, exception).showAndWait());
          final EventHandler<WorkerStateEvent> x = ____ -> {
//            System.out.println("finished task " + task.n + " " + task.getTitle());
//            System.out.println("");
            next();
          };
          task.setOnCancelled(x);
          task.setOnFailed(x);
          task.setOnSucceeded(x);
//          System.out.println("starting task " + task.n + " " + task.getTitle());
          if (task.onFXThread())
            tpe.submit(TimeTask.encapsulateTaskOnFXThread(task));
          else
            tpe.submit(task);
        }
      }
    });

  }

  public final void execute(final TimeTask<?> task) {
    Platform2.runOnFXThread(() -> {
      synchronized (scheduledTasks) {
        final boolean wasIdle = isIdleBinding.get();
        scheduledTasks.add(task);
        Arrays.asList(task.progressProperty(), task.stateProperty()).forEach(p -> p.addListener((__, ___, ____) -> {
          overallProgressBinding.invalidate();
          datasetProgressBindings.values().forEach(DoubleBinding::invalidate);
        }));
        if (wasIdle)
          currentTaskProperty.setValue(task);
      }
    });
  }

  private final void next() {
    synchronized (scheduledTasks) {
      if (!isIdleBinding.get()) {
        try {
          final TimeTask<?> nextTask = scheduledTasks.get(scheduledTasks.indexOf(currentTaskProperty.getValue()) + 1);
          if (nextTask != null)
            currentTaskProperty.setValue(nextTask);
        } catch (IndexOutOfBoundsException e) {
          System.err.println("index out of bounds, task not found.");
          System.err.println(scheduledTasks.size() + " tasks");
          System.err.println(scheduledTasks.indexOf(currentTaskProperty.getValue()));
        }
      }
    }
  }

  public final DoubleBinding datasetProgressBinding(final Dataset dataset) {
    if (datasetProgressBindings.containsKey(dataset))
      return datasetProgressBindings.get(dataset);
    final DoubleBinding datasetProgressBinding = Bindings.createDoubleBinding(() -> {
      synchronized (scheduledTasks) {
        if (scheduledTasks.isEmpty())
          return 1d;
        double progress = 0d;
        double tasks = 0d;
        for (TimeTask<?> task : scheduledTasks)
          if (task.getDataset() != null && task.getDataset().equals(dataset)) {
            if (task.isDone())
              progress += 1d;
            else if (task.getProgress() > 0)
              progress += task.getProgress();
            tasks++;
          }
        if (tasks > 0)
          return progress / tasks;
        return 1d;
      }
    } , scheduledTasks, currentTaskProperty);
    datasetProgressBindings.put(dataset, datasetProgressBinding);
    return datasetProgressBinding;
  }

  public final void cancel(final Dataset dataset) {
    synchronized (scheduledTasks) {
      scheduledTasks.filtered(task -> task.getDataset() != null && task.getDataset().equals(dataset)).forEach(
          task -> task.cancel());
      scheduledTasks.removeIf(task -> task.getDataset() != null && task.getDataset().equals(dataset));
      datasetProgressBindings.remove(dataset);
    }
  }

}
