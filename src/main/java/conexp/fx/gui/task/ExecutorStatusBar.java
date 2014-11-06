package conexp.fx.gui.task;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressBarBuilder;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressIndicatorBuilder;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import conexp.fx.core.collections.pair.Pair;

public final class ExecutorStatusBar<G, M> {

  private final BlockingExecutor  executor;
  public final BorderPane         statusBar;
  private final int               height                   = 20;
  private final int               buttonSize               = 16;
  private final int               padding                  = 2;
  private final int               progressWidth            = 128;
  private final ProgressIndicator currentProgressIndicator = ProgressIndicatorBuilder
                                                               .create()
                                                               .minHeight(height - 2 * padding)
                                                               .maxHeight(height - 2 * padding)
                                                               .minWidth(height - 2 * padding)
                                                               .maxWidth(height - 2 * padding)
                                                               .build();
  private final ProgressIndicator overallProgressIndicator = ProgressIndicatorBuilder
                                                               .create()
                                                               .minHeight(height - 2 * padding)
                                                               .maxHeight(height - 2 * padding)
                                                               .minWidth(height - 2 * padding)
                                                               .maxWidth(height - 2 * padding)
                                                               .build();
  private final ProgressBar       currentProgressBar       = ProgressBarBuilder
                                                               .create()
                                                               .minHeight(height - 2 * padding)
                                                               .maxHeight(height - 2 * padding)
                                                               .minWidth(progressWidth)
                                                               .maxWidth(progressWidth)
                                                               .build();
  private final ProgressBar       overallProgressBar       = ProgressBarBuilder
                                                               .create()
                                                               .minHeight(height - 2 * padding)
                                                               .maxHeight(height - 2 * padding)
                                                               .minWidth(progressWidth)
                                                               .maxWidth(progressWidth)
                                                               .build();
  private final Label             currentStatusLabel       = LabelBuilder
                                                               .create()
                                                               .graphic(currentProgressIndicator)
                                                               .minHeight(height - 2 * padding)
                                                               .maxHeight(height - 2 * padding)
                                                               .build();
  private final Label             overallStatusLabel       = LabelBuilder
                                                               .create()
                                                               .graphic(overallProgressIndicator)
                                                               .minHeight(height - 2 * padding)
                                                               .maxHeight(height - 2 * padding)
                                                               .build();

  private final Button            stopButton               = ButtonBuilder.create()
                                                           // .text("stop")
                                                               .style("-fx-base: red; -fx-label-padding: 0 4 0 0")
                                                               .maxHeight(buttonSize)
                                                               .maxWidth(buttonSize)
                                                               .minHeight(buttonSize)
                                                               .minWidth(buttonSize)
                                                               .onAction(new EventHandler<ActionEvent>() {

                                                                 public void handle(ActionEvent event) {
                                                                   stopCurrentTask();
                                                                 };
                                                               })
                                                               .build();

  private final void stopCurrentTask() {
    this.executor.currentTaskProperty.getValue().cancel();
  }

  public ExecutorStatusBar(final BlockingExecutor executor) {
    this.executor = executor;
    final DoubleBinding progressIndicationBinding = new DoubleBinding() {

      {
        bind(executor.overallProgressBinding);
      }

      protected final double computeValue() {
        if (executor.overallProgressBinding.get() != 1)
          return -1;
        return 1;
      }
    };
    currentProgressIndicator.progressProperty().bind(progressIndicationBinding);
    overallProgressIndicator.progressProperty().bind(progressIndicationBinding);
    final BooleanBinding executorIsProcessing = new BooleanBinding() {

      {
        bind(executor.isIdleBinding);
      }

      protected final boolean computeValue() {
        return !executor.isIdleBinding.get();
      }
    };
    currentProgressIndicator.visibleProperty().bind(executorIsProcessing);
    overallProgressIndicator.visibleProperty().bind(executorIsProcessing);
    executor.currentTaskProperty.addListener(new ChangeListener<BlockingTask>() {

      public final void changed(
          final ObservableValue<? extends BlockingTask> observable,
          final BlockingTask oldTask,
          final BlockingTask newTask) {
        currentStatusLabel.textProperty().bind(new StringBinding() {

          {
            bind(newTask.messageProperty(), newTask.titleProperty());
          }

          protected final String computeValue() {
            return newTask.titleProperty().get() + ": " + newTask.messageProperty().get();
          }
        });
        currentProgressBar.progressProperty().bind(newTask.progressProperty());
      }
    });
    overallProgressBar.progressProperty().bind(executor.overallProgressBinding);
    overallStatusLabel.textProperty().bind(new StringBinding() {

      {
        bind(executor.isIdleBinding, executor.executionTimeMillis);
      }

      protected final String computeValue() {
        return (executor.isIdleBinding.get() ? "idle" : "processing") + " (" + executor.executionTimeMillis.get()
            + "ms)";
      }
    });
    final Tooltip t = new Tooltip();
    t.textProperty().bind(new StringBinding() {

      {
        bind(executor.executionsTimeMillisMap);
      }

      protected String computeValue() {
        final StringBuilder s = new StringBuilder();
        int i = 0;
        final int size = executor.executionsTimeMillisMap.size();
        for (Pair<String, Long> e : executor.executionsTimeMillisMap.subList(Math.max(0, size - 32), size)) {
          s.append("[");
          s.append(++i);
          s.append("] ");
          s.append(e.first());
          s.append(": ");
          s.append(e.second());
          s.append("ms\r\n");
        }
        return s.toString();
      }
    });
    overallProgressIndicator.setTooltip(t);
    overallStatusLabel.setTooltip(t);
    overallProgressBar.setTooltip(t);
    final BorderPane currentPane =
        BorderPaneBuilder.create().left(currentStatusLabel).right(currentProgressBar).build();
    final BorderPane overallPane =
        BorderPaneBuilder.create().left(overallStatusLabel).right(overallProgressBar).build();
    final BorderPane overallPaneWithStopButton =
        BorderPaneBuilder.create().center(overallPane).right(stopButton).build();
    statusBar =
        BorderPaneBuilder
            .create()
            .padding(new Insets(padding, padding, padding, padding))
            .minHeight(height)
            .maxHeight(height)
            .left(currentPane)
            .right(overallPaneWithStopButton)
            .build();
  }
}
