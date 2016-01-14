package conexp.fx.gui.task;

import conexp.fx.core.math.Math3;
import conexp.fx.gui.util.Platform2;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressBarBuilder;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressIndicatorBuilder;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBox;

public final class ExecutorStatusBar {

  private final class TaskItem {

    private final ProgressBar currentProgressBar = ProgressBarBuilder
        .create()
        .minHeight(height - 2 * padding)
        .maxHeight(height - 2 * padding)
        .minWidth(100)
        .maxWidth(100)
        .build();
    private final Label       currentStatusLabel = LabelBuilder
        .create()
        .minWidth(478)
        .maxWidth(478)
        .minHeight(height - 2 * padding)
        .maxHeight(height - 2 * padding)
        .build();
    private final Label       timeLabel          = LabelBuilder
        .create()
        .minWidth(80)
        .maxWidth(80)
        .minHeight(height - 2 * padding)
        .maxHeight(height - 2 * padding)
        .build();
    private final Button      stopButton         =
        ButtonBuilder.create().style("-fx-base: red").maxHeight(12).maxWidth(12).minHeight(12).minWidth(12).build();
    private final BorderPane  currentPane        = new BorderPane();

    public TaskItem() {
      currentPane.setLeft(currentStatusLabel);
      currentPane.setCenter(new HBox(currentProgressBar, stopButton));
      currentPane.setRight(timeLabel);
      currentProgressBar.setPadding(new Insets(0, 5, 0, 5));
    }

    private final void bindTo(final TimeTask<?> task) {
      currentStatusLabel.textProperty().bind(task.titleProperty());
      timeLabel.textProperty().bind(
          Bindings.createStringBinding(
              () -> task.isCancelled() ? "cancelled" : Math3.formatTime(task.runTimeMillisProperty().get()),
              task.runTimeMillisProperty(),
              task.stateProperty()));
      timeLabel.alignmentProperty().bind(
          Bindings.createObjectBinding(
              () -> task.isDone() || task.isRunning() ? Pos.BASELINE_RIGHT : Pos.BASELINE_LEFT,
              task.stateProperty()));
      currentProgressBar.progressProperty().bind(task.progressProperty());
      stopButton.setOnAction(e -> task.cancel(true));
      stopButton.visibleProperty().bind(Bindings.createObjectBinding(() -> !task.isDone(), task.stateProperty()));
    }
  }

  private BlockingExecutor        executor;
  public final BorderPane         statusBar;
  private final int               height                   = 20;
  private final int               padding                  = 2;
  private final ProgressIndicator overallProgressIndicator = ProgressIndicatorBuilder
      .create()
      .minHeight(height - 2 * padding)
      .maxHeight(height - 2 * padding)
      .minWidth(height - 2 * padding)
      .maxWidth(height - 2 * padding)
      .build();
  private final ProgressBar       overallProgressBar       = ProgressBarBuilder
      .create()
      .minHeight(height - 2 * padding)
      .maxHeight(height - 2 * padding)
      .minWidth(200)
      .maxWidth(200)
      .build();
  private final Label             overallStatusLabel       = LabelBuilder
      .create()
      .graphic(overallProgressIndicator)
      .minHeight(height - 2 * padding)
      .maxHeight(height - 2 * padding)
      .build();

  private final ListView<TimeTask<?>> scheduledTaskListView;

  public ExecutorStatusBar(AnchorPane overlayPane) {
    final BorderPane overallPane =
        BorderPaneBuilder.create().left(overallStatusLabel).right(overallProgressBar).build();
    statusBar = BorderPaneBuilder
        .create()
        .padding(new Insets(padding, padding, padding, padding))
        .minHeight(height)
        .maxHeight(height)
        .right(overallPane)
        .build();
    scheduledTaskListView = new ListView<TimeTask<?>>();
//    scheduledTaskListView.setOnMouseClicked(e -> executor.clearFinished());
    scheduledTaskListView.setPrefSize(700, 200);
    scheduledTaskListView.setCellFactory(l -> {
      final ListCell<TimeTask<?>> cell = new ListCell<TimeTask<?>>() {

        private final TaskItem taskItem = new TaskItem();

        @Override
        protected void updateItem(TimeTask<?> p, boolean empty) {
          if (empty)
            return;
          taskItem.bindTo(p);
          setGraphic(taskItem.currentPane);
        }
      };
      return cell;
    });
    overallPane.setOnMouseEntered(e -> {
      overlayPane.setMouseTransparent(false);
      if (overlayPane.getChildren().contains(scheduledTaskListView))
        return;
      overlayPane.getChildren().add(scheduledTaskListView);
      AnchorPane.setRightAnchor(scheduledTaskListView, 4d);
      AnchorPane.setBottomAnchor(scheduledTaskListView, 4d);
    });
    scheduledTaskListView.setOnMouseExited(e -> {
      overlayPane.setMouseTransparent(true);
      overlayPane.getChildren().clear();
    });
  }

  public final void setOnMouseExitedHandler(final Scene scene) {
    scene.setOnMouseExited(scheduledTaskListView.getOnMouseExited());
  }

  public void bindTo(final BlockingExecutor executor) {
    this.executor = executor;
    scheduledTaskListView.setItems(executor.scheduledTasks);
    scheduledTaskListView.scrollTo(executor.currentTaskProperty.getValue());
    overallProgressIndicator.progressProperty().bind(
        Bindings.createDoubleBinding(
            () -> executor.overallProgressBinding.get() != 1 ? -1d : 1d,
            executor.overallProgressBinding));
    overallProgressIndicator.visibleProperty().bind(
        Bindings.createBooleanBinding(() -> !executor.isIdleBinding.get(), executor.isIdleBinding));
    executor.currentTaskProperty.addListener(new ChangeListener<TimeTask<?>>() {

      public final void changed(
          final ObservableValue<? extends TimeTask<?>> observable,
          final TimeTask<?> oldTask,
          final TimeTask<?> newTask) {
        Platform2.runOnFXThread(() -> {
          scheduledTaskListView.scrollTo(newTask);
          overallStatusLabel
              .textProperty()
              .bind(
                  Bindings.createStringBinding(
                      () -> executor.isIdleBinding.get() ? "" : newTask.titleProperty().get()
                          + (newTask.messageProperty().get().equals("") ? "" : ": " + newTask.messageProperty().get()),
                      executor.isIdleBinding,
                      newTask.messageProperty(),
                      newTask.titleProperty()));
        });
      }
    });
    overallProgressBar.progressProperty().bind(executor.overallProgressBinding);
  }
}
