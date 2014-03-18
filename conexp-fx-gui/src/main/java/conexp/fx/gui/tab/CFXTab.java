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
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressBarBuilder;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressIndicatorBuilder;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioButtonBuilder;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleGroupBuilder;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import jfxtras.labs.scene.control.ListSpinner;
import jfxtras.labs.scene.control.ListSpinner.ArrowDirection;
import jfxtras.labs.scene.control.ListSpinner.ArrowPosition;
import conexp.fx.core.builder.FileRequest;
import conexp.fx.core.builder.Request;
import conexp.fx.core.builder.StringRequest;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.concurrent.BlockingTask;
import conexp.fx.core.exporter.TeXExporter.ContextTeXPackage;
import conexp.fx.core.exporter.TeXExporter.DiagramTeXPackage;
import conexp.fx.core.exporter.TeXExporter.FitScale;
import conexp.fx.core.exporter.TeXExporter.ScaleEnum;
import conexp.fx.core.exporter.TeXExporter.TeXOptions;
import conexp.fx.core.lock.ALock;
import conexp.fx.core.service.FCAInstance;
import conexp.fx.core.service.FCAInstance.TabConfiguration;
import conexp.fx.gui.GUI;
import conexp.fx.gui.context.MatrixContextWidget;
import conexp.fx.gui.context.StringMatrixContextWidget;
import conexp.fx.gui.dialog.FXDialog;
import conexp.fx.gui.dialog.FXDialog.Result;
import conexp.fx.gui.dialog.FXDialog.Return;
import conexp.fx.gui.dialog.FXDialog.Style;
import conexp.fx.gui.exploration.ImplicationWidget;
import conexp.fx.gui.graph.ConceptGraph;

public class CFXTab<G, M> extends Tab {

  public final GUI                       conExp;
  public final FCAInstance<G, M>         fca;
  private final BorderPane               pane = new BorderPane();
  public final MatrixContextWidget<G, M> contextWidget;
  public final ConceptGraph<G, M>        conceptGraph;
  public final ImplicationWidget<G, M>   implicationWidget;

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
    this.implicationWidget = new ImplicationWidget<G, M>(this);
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
    this.fca.initialize();
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

  public void exportTeX() {
    final Return<TeXOptions> ret = new TeXDialog().showAndWait();
    if (ret.result().equals(Result.OK)) {
      final FileChooser chooser = new FileChooser();
      chooser.getExtensionFilters().add(new ExtensionFilter("LaTeX File (*.tex)", "*.tex"));
      chooser.setInitialDirectory(conExp.lastDirectory);
      final File file = chooser.showSaveDialog(fca.tab.primaryStage());
      if (file == null)
        return;
      final TeXOptions value = ret.value();
      value.file = file;
      fca.exportTeX(value);
    }
  }

  private final class TeXDialog extends FXDialog<TeXOptions> {

    public TeXDialog() {
      super(conExp.primaryStage, FXDialog.Style.WARN, "TeX Export Wizard", "TeX Export Wizard Options", new VBox(), 270);
      VBox box = (VBox) optionalCenterNode;
      box.setPadding(new Insets(0, 10, 0, 10));
      box.setSpacing(4);
      value =
          new TeXOptions(null, false, true, false, ContextTeXPackage.None, DiagramTeXPackage.ConExpFX, new FitScale(
              80,
              120));
      final CheckBox arrowsCheckBox = CheckBoxBuilder.create().text("Arrow Relations").build();
      final CheckBox labelsCheckBox = CheckBoxBuilder.create().text("Concept Labels").selected(true).build();
      final CheckBox standAloneCheckBox = CheckBoxBuilder.create().disable(true).text("Stand-Alone Document").build();
      final RadioButton noneContextButton =
          RadioButtonBuilder
              .create()
              .text("Context Package: None")
              .selected(true)
              .userData(ContextTeXPackage.None)
              .build();
      final RadioButton ganterContextButton =
          RadioButtonBuilder.create().text("Context Package: Ganter").userData(ContextTeXPackage.Ganter).build();
      final RadioButton tabularContextButton =
          RadioButtonBuilder.create().text("Context Package: Tabular").userData(ContextTeXPackage.Tabular).build();
      final RadioButton noneDiagramButton =
          RadioButtonBuilder.create().text("Diagram Package: None").userData(DiagramTeXPackage.None).build();
      final RadioButton ganterDiagramButton =
          RadioButtonBuilder.create().text("Diagram Package: Ganter").userData(DiagramTeXPackage.Ganter).build();
      final RadioButton conExpFXDiagramButton =
          RadioButtonBuilder
              .create()
              .text("Diagram Package: ConExpFX")
              .selected(true)
              .userData(DiagramTeXPackage.ConExpFX)
              .build();
      final RadioButton fitButton =
          RadioButtonBuilder.create().text("Diagram Scale: Fit").userData(ScaleEnum.Fit).build();
      final RadioButton fitWidthButton =
          RadioButtonBuilder.create().text("Diagram Scale: Fit Width").userData(ScaleEnum.FitWidth).build();
      final RadioButton fitHeightButton =
          RadioButtonBuilder.create().text("Diagram Scale: Fit Height").userData(ScaleEnum.FitHeight).build();
      final RadioButton fitRatioButton =
          RadioButtonBuilder
              .create()
              .text("Diagram Scale: Fit Ratio")
              .selected(true)
              .userData(ScaleEnum.FitRatio)
              .build();
      final ListSpinner<Integer> widthSpinner = new ListSpinner<Integer>(1, 1000);
      final ListSpinner<Integer> heightSpinner = new ListSpinner<Integer>(1, 1000);
      widthSpinner.valueProperty().set(80);
      heightSpinner.valueProperty().set(120);
      widthSpinner.withPostfix("mm");
      heightSpinner.withPostfix("mm");
      widthSpinner.withAlignment(Pos.CENTER);
      heightSpinner.withAlignment(Pos.CENTER);
      widthSpinner.withArrowDirection(ArrowDirection.HORIZONTAL);
      heightSpinner.withArrowDirection(ArrowDirection.VERTICAL);
//      widthSpinner.withArrowPosition(ArrowPosition.SPLIT);
//      heightSpinner.withArrowPosition(ArrowPosition.SPLIT);
      widthSpinner.withEditable(true);
      widthSpinner.withStringConverter(new IntegerStringConverter());
      heightSpinner.withEditable(true);
      heightSpinner.withStringConverter(new IntegerStringConverter());
      widthSpinner.setMinWidth(100);
      widthSpinner.setMaxWidth(100);
      heightSpinner.setMinWidth(100);
      heightSpinner.setMaxWidth(100);
      final ToggleGroup contextGroup =
          ToggleGroupBuilder.create().toggles(noneContextButton, ganterContextButton, tabularContextButton).build();
      final ToggleGroup diagramGroup =
          ToggleGroupBuilder.create().toggles(noneDiagramButton, ganterDiagramButton, conExpFXDiagramButton).build();
      final ToggleGroup scaleGroup =
          ToggleGroupBuilder.create().toggles(fitButton, fitWidthButton, fitHeightButton, fitRatioButton).build();
      arrowsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          value.arrows = newValue;
        }
      });
      labelsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          value.labels = newValue;
        }
      });
      standAloneCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          value.standAlone = newValue;
        }
      });
      contextGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

        @Override
        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldToggle, Toggle newToggle) {
          value.contextTeXPackage = (ContextTeXPackage) newToggle.getUserData();
        }
      });
      diagramGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

        @Override
        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldToggle, Toggle newToggle) {
          value.diagramTeXPackage = (DiagramTeXPackage) newToggle.getUserData();
        }
      });
      scaleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

        @Override
        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldToggle, Toggle newToggle) {
          value.scale =
              ((ScaleEnum) newToggle.getUserData()).toOption(widthSpinner.valueProperty().get(), heightSpinner
                  .valueProperty()
                  .get());
          widthSpinner.disableProperty().set((ScaleEnum) newToggle.getUserData() == ScaleEnum.FitHeight);
          heightSpinner.disableProperty().set((ScaleEnum) newToggle.getUserData() == ScaleEnum.FitWidth);
        }
      });
      widthSpinner.valueProperty().addListener(new ChangeListener<Integer>() {

        @Override
        public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
          value.scale =
              ((ScaleEnum) scaleGroup.selectedToggleProperty().get().getUserData()).toOption(newValue, heightSpinner
                  .valueProperty()
                  .get());
        }
      });
      heightSpinner.valueProperty().addListener(new ChangeListener<Integer>() {

        @Override
        public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
          value.scale =
              ((ScaleEnum) scaleGroup.selectedToggleProperty().get().getUserData()).toOption(widthSpinner
                  .valueProperty()
                  .get(), newValue);
        }
      });
      box.getChildren().addAll(
          VBoxBuilder
              .create()
              .padding(new Insets(2, 0, 2, 0))
              .spacing(4)
              .children(arrowsCheckBox, labelsCheckBox, standAloneCheckBox)
              .build());
      box.getChildren().addAll(
          VBoxBuilder
              .create()
              .padding(new Insets(2, 0, 2, 0))
              .spacing(4)
              .children(noneContextButton, ganterContextButton, tabularContextButton)
              .build());
      box.getChildren().addAll(
          VBoxBuilder
              .create()
              .padding(new Insets(2, 0, 2, 0))
              .spacing(4)
              .children(noneDiagramButton, ganterDiagramButton, conExpFXDiagramButton)
              .build());
      box.getChildren().addAll(
          VBoxBuilder
              .create()
              .padding(new Insets(2, 0, 0, 0))
              .spacing(4)
              .children(fitButton, fitWidthButton, fitHeightButton, fitRatioButton)
              .build());
      box
          .getChildren()
          .addAll(
              HBoxBuilder
                  .create()
                  .padding(new Insets(0, 0, 2, 0))
                  .spacing(4)
                  .children(widthSpinner, heightSpinner)
                  .build());
    }
  }
}
