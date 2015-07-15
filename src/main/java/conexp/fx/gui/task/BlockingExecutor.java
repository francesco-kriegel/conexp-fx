package conexp.fx.gui.task;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

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

public class BlockingExecutor {

  public final AbstractExecutorService     tpe                 =
      new ForkJoinPool(Runtime.getRuntime().availableProcessors() - 1);
  public final BooleanBinding              isIdleBinding;
  public final DoubleBinding               overallProgressBinding;
  public final ObservableList<TimeTask<?>> scheduledTasks      =
      FXCollections.observableList(Collections.synchronizedList(new LinkedList<TimeTask<?>>()));
  public final Property<TimeTask<?>>       currentTaskProperty = new SimpleObjectProperty<TimeTask<?>>();

  private final Map<Dataset, DoubleBinding> datasetProgressBindings = new ConcurrentHashMap<>();

  public BlockingExecutor() {
    overallProgressBinding = Bindings.createDoubleBinding(() -> {
      if (scheduledTasks.isEmpty())
        return 1d;
      double progress = 0d;
      for (TimeTask<?> task : scheduledTasks) {
        if (task.isDone())
          progress += 1d;
        else if (task.getProgress() > 0)
          progress += task.getProgress();
      }
      return progress / (double) scheduledTasks.size();
    } , scheduledTasks, currentTaskProperty);
    isIdleBinding = Bindings.createBooleanBinding(() -> overallProgressBinding.get() == 1d, overallProgressBinding);
    currentTaskProperty.addListener((__, ___, task) -> {
      if (task.isDone())
        next();
      else {
        task.exceptionProperty().addListener(
            (____, _____, exception) -> new ErrorDialog(ConExpFX.instance.primaryStage, exception).showAndWait());
        task.setOnCancelled(____ -> next());
        task.setOnFailed(____ -> next());
        task.setOnSucceeded(____ -> next());
        tpe.submit(task);
      }
    });
  }

  public final void execute(final TimeTask<?> task) {
    Platform2.runOnFXThread(() -> {
      synchronized (scheduledTasks) {
        if (isIdleBinding.get())
          currentTaskProperty.setValue(task);
        scheduledTasks.add(task);
        task.progressProperty().addListener((__, ___, ____) -> {
          overallProgressBinding.invalidate();
          datasetProgressBindings.values().forEach(DoubleBinding::invalidate);
        });
        task.stateProperty().addListener((__, ___, ____) -> {
          overallProgressBinding.invalidate();
          datasetProgressBindings.values().forEach(DoubleBinding::invalidate);
        });
      }
    });
  }

  private final void next() {
    synchronized (scheduledTasks) {
      if (!isIdleBinding.get())
        currentTaskProperty.setValue(scheduledTasks.get(scheduledTasks.indexOf(currentTaskProperty.getValue()) + 1));
    }
  }

  public final DoubleBinding datasetProgressBinding(final Dataset dataset) {
    if (datasetProgressBindings.containsKey(dataset))
      return datasetProgressBindings.get(dataset);
    final DoubleBinding datasetProgressBinding = Bindings.createDoubleBinding(() -> {
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
    } , scheduledTasks, currentTaskProperty);
    datasetProgressBindings.put(dataset, datasetProgressBinding);
    return datasetProgressBinding;
  }

  public final void cancel(final Dataset dataset) {
    scheduledTasks.filtered(task -> task.getDataset() != null && task.getDataset().equals(dataset)).forEach(
        task -> task.cancel());
    scheduledTasks.removeIf(task -> task.getDataset() != null && task.getDataset().equals(dataset));
    datasetProgressBindings.remove(dataset);
  }

}
