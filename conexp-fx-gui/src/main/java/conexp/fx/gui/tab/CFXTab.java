package conexp.fx.gui.tab;

/*
 * #%L
 * Concept Explorer FX - Graphical User Interface
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
import java.io.File;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressBarBuilder;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressIndicatorBuilder;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import conexp.fx.core.builder.FileRequest;
import conexp.fx.core.builder.Request;
import conexp.fx.core.builder.StringRequest;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.concurrent.BlockingTask;
import conexp.fx.core.lock.ALock;
import conexp.fx.core.service.FCAInstance;
import conexp.fx.core.service.FCAInstance.InitLevel;
import conexp.fx.core.service.FCAInstance.TabConfiguration;
import conexp.fx.gui.GUI;
import conexp.fx.gui.context.MatrixContextWidget;
import conexp.fx.gui.context.StringMatrixContextWidget;
import conexp.fx.gui.dialog.FXDialog;
import conexp.fx.gui.dialog.FXDialog.Result;
import conexp.fx.gui.dialog.FXDialog.Style;
import conexp.fx.gui.exploration.ImplicationWidget;
import conexp.fx.gui.graph.ConceptGraph;

public class CFXTab<G, M> extends Tab {

  public final GUI                       conExp;
  public final FCAInstance<G, M>         fca;
  private final BorderPane               pane = new BorderPane();
  public final MatrixContextWidget<G, M> contextWidget;
  public final ConceptGraph<G, M>        conceptGraph;

  @SuppressWarnings("unchecked")
  public CFXTab(final GUI conExp, final Request<G, M> request) {
    super();
    this.conExp = conExp;
    this.fca = conExp.fcaService.add(request);
    this.fca.setTabConfiguration(new TabConfiguration() {

      @Override
      public Stage primaryStage() {
        return conExp.primaryStage;
      }

      @Override
      public ALock graphLock() {
        return conceptGraph.controller.graphLock;
      }

      @Override
      public ALock highlightLock() {
        return conceptGraph.highlightLock;
      }

      @Override
      public File lastDirectory() {
        return conExp.lastDirectory;
      }

      @Override
      public void setLastDirectory(final File dir) {
        conExp.lastDirectory = dir;
      }

      @Override
      public List<File> fileHistory() {
        return conExp.fileHistory;
      }

      @Override
      public boolean threeDimensions() {
        return conceptGraph.threeDimensions();
      }

      @Override
      public Map<Integer, Integer> rowMap() {
        return contextWidget.rowHeaderPane.rowMap;
      }

      @Override
      public Map<Integer, Integer> colMap() {
        return contextWidget.colHeaderPane.columnMap;
      }
    });
    this.setContent(pane);
    this.setGraphic(ImageViewBuilder
        .create()
        .image(new Image(GUI.class.getResourceAsStream("image/context.gif")))
        .build());
    this.textProperty().bind(new StringBinding() {

      {
        bind(fca.id, fca.unsavedChanges);
      }

      protected final String computeValue() {
        return fca.id.get() + (fca.unsavedChanges.get() ? " *" : "");
      }
    });
    this.setOnClosed(new EventHandler<Event>() {

      public final void handle(final Event event) {
        if (fca.unsavedChanges.get())
          if (new FXDialog(conExp.primaryStage, Style.QUESTION, "Unsaved Changes", fca.id.get()
              + " has unsaved changes. Do you want to save?", null).showAndWait().equals(Result.YES))
            save();
      }
    });
    new CFXStatusBar();
    if (request instanceof FileRequest) {
      this.isStringTab = true;
      this.contextWidget = (MatrixContextWidget<G, M>) new StringMatrixContextWidget((CFXTab<String, String>) this);
      conExp.fileHistory.remove(((FileRequest) request).file);
      conExp.fileHistory.add(0, ((FileRequest) request).file);
    } else if (request instanceof StringRequest) {
      this.isStringTab = true;
      this.contextWidget = (MatrixContextWidget<G, M>) new StringMatrixContextWidget((CFXTab<String, String>) this);
    } else
      this.contextWidget = new MatrixContextWidget<G, M>(this);
    this.conceptGraph = new ConceptGraph<G, M>(this);
    this.fca.initialize(InitLevel.LAYOUT);
    final ImplicationWidget<G, M> implicationWidget = new ImplicationWidget<G, M>(this);
    this.pane.setCenter(SplitPaneBuilder
        .create()
        .dividerPositions(new double[] { 0.8d })
        .orientation(Orientation.VERTICAL)
        .items(
            SplitPaneBuilder
                .create()
                .dividerPositions(new double[] { 0.3d })
                .orientation(Orientation.HORIZONTAL)
                .items(contextWidget, conceptGraph)
                .build(),
            implicationWidget)
        .build());
  }

  private final class CFXStatusBar {

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
//                                                                 .text("stop")
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
      fca.executor.currentTaskProperty.getValue().cancel();
    }

    private CFXStatusBar() {
      final DoubleBinding progressIndicationBinding = new DoubleBinding() {

        {
          bind(fca.executor.overallProgressBinding);
        }

        protected final double computeValue() {
          if (fca.executor.overallProgressBinding.get() != 1)
            return -1;
          return 1;
        }
      };
      currentProgressIndicator.progressProperty().bind(progressIndicationBinding);
      overallProgressIndicator.progressProperty().bind(progressIndicationBinding);
      final BooleanBinding executorIsProcessing = new BooleanBinding() {

        {
          bind(fca.executor.isIdleBinding);
        }

        protected final boolean computeValue() {
          return !fca.executor.isIdleBinding.get();
        }
      };
      currentProgressIndicator.visibleProperty().bind(executorIsProcessing);
      overallProgressIndicator.visibleProperty().bind(executorIsProcessing);
      fca.executor.currentTaskProperty.addListener(new ChangeListener<BlockingTask>() {

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
      overallProgressBar.progressProperty().bind(fca.executor.overallProgressBinding);
      overallStatusLabel.textProperty().bind(new StringBinding() {

        {
          bind(fca.executor.isIdleBinding, fca.executor.executionTimeMillis);
        }

        protected final String computeValue() {
          return (fca.executor.isIdleBinding.get() ? "idle" : "processing") + " ("
              + fca.executor.executionTimeMillis.get() + "ms)";
        }
      });
      final Tooltip t = new Tooltip();
      t.textProperty().bind(new StringBinding() {

        {
          bind(fca.executor.executionsTimeMillisMap);
        }

        protected String computeValue() {
          final StringBuilder s = new StringBuilder();
          int i = 0;
          final int size = fca.executor.executionsTimeMillisMap.size();
          for (Pair<String, Long> e : fca.executor.executionsTimeMillisMap.subList(Math.max(0, size - 32), size)) {
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
      final BorderPane statusBar =
          BorderPaneBuilder
              .create()
              .padding(new Insets(padding, padding, padding, padding))
              .minHeight(height)
              .maxHeight(height)
              .left(currentPane)
              .right(overallPaneWithStopButton)
              .build();
      pane.setBottom(statusBar);
    }
  }

  private boolean isStringTab = false;

  public final boolean isStringTab() {
    return isStringTab;
  }

  public final void save() {
    if (fca.file == null)
      saveAs();
    else
      fca.storeToFile();
  }

  public final void saveAs() {
    if (fca.tab == null)
      return;
    final FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Formal Context File");
    if (fca.tab.lastDirectory() != null)
      fileChooser.setInitialDirectory(fca.tab.lastDirectory());
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Context & Lattice (ConExpFX Format, *.cfx)", "*.cfx"));
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Only Context (Burmeister Format, *.cxt)", "*.cxt"));
    final File file = fileChooser.showSaveDialog(fca.tab.primaryStage());
    if (file != null) {
      fca.file = file;
      fca.tab.setLastDirectory(file.getParentFile());
      fca.storeToFile();
    }
  }

  public final void export() {
    if (fca.tab == null)
      return;
    final FileChooser fileChooser = new FileChooser();
    if (fca.tab.lastDirectory() != null)
      fileChooser.setInitialDirectory(fca.tab.lastDirectory());
    fileChooser.getExtensionFilters().add(
        new ExtensionFilter("Context & Lattice (TeX - Ganter's fca.sty, *.tex)", "*.tex"));
    fileChooser.getExtensionFilters().add(
        new ExtensionFilter("Only Lattice (Portable Network Graphics, *.png)", "*.png"));
    fileChooser.getExtensionFilters().add(
        new ExtensionFilter("Only Lattice (Scalable Vector Graphics, *.svg)", "*.svg"));
    fileChooser.getExtensionFilters().add(
        new ExtensionFilter("Only Lattice (Portable Document Format, *.pdf)", "*.pdf"));
    fileChooser.getExtensionFilters().add(
        new ExtensionFilter("Only Context (Hypertext Markup Language, *.html)", "*.html"));
    final File file = fileChooser.showSaveDialog(fca.tab.primaryStage());
    if (file != null) {
      fca.tab.setLastDirectory(file.getParentFile());
      fca.exportToFile(file);
    }
  }
}
