package conexp.fx.gui;

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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import conexp.fx.core.algorithm.lattice.IFox;
import conexp.fx.core.algorithm.lattice.IPred;
import conexp.fx.core.algorithm.nextclosure.NextClosures6;
import conexp.fx.core.builder.FileRequest;
import conexp.fx.core.builder.Request;
import conexp.fx.core.builder.Requests.Source;
import conexp.fx.core.builder.StringRequest;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.context.MatrixContext.AutomaticMode;
import conexp.fx.core.exporter.CFXExporter;
import conexp.fx.core.exporter.CXTExporter;
import conexp.fx.core.exporter.HTMLExporter;
import conexp.fx.core.exporter.PDFExporter;
import conexp.fx.core.exporter.PNGExporter;
import conexp.fx.core.exporter.SVGExporter;
import conexp.fx.core.exporter.TeXExporter;
import conexp.fx.core.exporter.TeXExporter.ContextTeXPackage;
import conexp.fx.core.exporter.TeXExporter.DiagramTeXPackage;
import conexp.fx.core.exporter.TeXExporter.FitScale;
import conexp.fx.core.exporter.TeXExporter.TeXOptions;
import conexp.fx.core.layout.ConceptLayout;
import conexp.fx.core.layout.ConceptMovement;
import conexp.fx.core.layout.GeneticLayouter;
import conexp.fx.core.quality.ConflictDistance;
import conexp.fx.core.quality.LayoutEvolution;
import conexp.fx.core.util.FileFormat;
import conexp.fx.gui.concept.ConceptWidget;
import conexp.fx.gui.context.MatrixContextWidget;
import conexp.fx.gui.context.StringMatrixContextWidget;
import conexp.fx.gui.dialog.FXDialog.Result;
import conexp.fx.gui.dialog.FXDialog.Return;
import conexp.fx.gui.dialog.TeXDialog;
import conexp.fx.gui.graph.ConceptGraph;
import conexp.fx.gui.implication.ImplicationWidget;
import conexp.fx.gui.task.BlockingExecutor;
import conexp.fx.gui.task.BlockingTask;
import conexp.fx.gui.task.ExecutorStatusBar;
import conexp.fx.gui.task.ImportTask;
import conexp.fx.gui.task.SeedsAndLabelsTask;

public final class FCAInstance<G, M> {

  public final Request<G, M>                     request;
  public final MatrixContext<G, M>               context;
  public final ConceptLattice<G, M>              lattice;
  public final ConceptLayout<G, M>               layout;
  public final ObservableList<Concept<G, M>>     concepts         = FXCollections.<Concept<G, M>> observableArrayList();
  public final ObservableList<Implication<G, M>> implications     = FXCollections
                                                                      .<Implication<G, M>> observableArrayList();

  public final ThreadPoolExecutor                tpe;
  public final ConflictDistance<G, M>            conflictDistance = new ConflictDistance<G, M>();
  public final BlockingExecutor                  executor         = new BlockingExecutor();
  public final StringProperty                    id               = new SimpleStringProperty("");
  public final BooleanProperty                   unsavedChanges   = new SimpleBooleanProperty(false);
  public File                                    file;
  public boolean                                 isStringTab      = false;

  public final ConExpFX                          conExpFX;
  public final MatrixContextWidget<G, M>         contextWidget;
  public final ConceptGraph<G, M>                conceptGraph;
  public final ConceptWidget<G, M>               conceptWidget;
  public final ImplicationWidget<G, M>           implicationWidget;
  public final BorderPane                        statusWidget;

  public FCAInstance(final ConExpFX conExp, final Request<G, M> request) {
    super();
    this.conExpFX = conExp;
    this.tpe = conExp.tpe;
    this.request = request;
    this.context = request.createContext(AutomaticMode.REDUCE);
    this.context.id.bind(id);
    this.lattice = new ConceptLattice<G, M>(context);
    this.layout = new ConceptLayout<G, M>(lattice, null);
    this.layout.observe();
    this.id.set(request.getId());
    if (request.src != Source.FILE)
      unsavedChanges.set(true);
    else if (request instanceof FileRequest)
      file = ((FileRequest) request).file;
    this.layout.observe();
//    this.setContent(pane);
//    this.setGraphic(ImageViewBuilder
//        .create()
//        .image(new Image(GUI.class.getResourceAsStream("image/context.gif")))
//        .build());
//    this.textProperty().bind(new StringBinding() {
//
//      {
//        bind(fca.id, fca.unsavedChanges);
//      }
//
//      protected final String computeValue() {
//        return fca.id.get() + (fca.unsavedChanges.get() ? " *" : "");
//      }
//    });
//    this.setOnClosed(new EventHandler<Event>() {
//
//      public final void handle(final Event event) {
//        if (fca.unsavedChanges.get())
//          if (new FXDialog(conExp.primaryStage, Style.QUESTION, "Unsaved Changes", fca.id.get()
//              + " has unsaved changes. Do you want to save?", null).showAndWait().equals(Result.YES))
//            save();
//      }
//    });
    this.statusWidget = new ExecutorStatusBar<G, M>(executor).statusBar;
    if (request instanceof FileRequest) {
      this.isStringTab = true;
      this.contextWidget =
          (MatrixContextWidget<G, M>) new StringMatrixContextWidget((FCAInstance<String, String>) this);
      conExp.fileHistory.remove(((FileRequest) request).file);
      conExp.fileHistory.add(0, ((FileRequest) request).file);
    } else if (request instanceof StringRequest) {
      this.isStringTab = true;
      this.contextWidget =
          (MatrixContextWidget<G, M>) new StringMatrixContextWidget((FCAInstance<String, String>) this);
    } else
      this.contextWidget = new MatrixContextWidget<G, M>(this);
    this.conceptGraph = new ConceptGraph<G, M>(this);
    this.conceptWidget = new ConceptWidget<G, M>(this);
    this.implicationWidget = new ImplicationWidget<G, M>(this);
    lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      @Override
      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
        concepts.clear();
//        Platform.runLater(new Runnable() {
//
//          @Override
//          public final void run() {
        concepts.addAll(lattice.colHeads());
//          }
//        });
      }
    }, RelationEvent.ALL_CHANGED);
    this.initialize2();
  }

  public final void initialize() {
    executor.submit(new ImportTask<G, M>(this));
    executor.submit(new BlockingTask("Initialization") {

      @Override
      protected void _call() {
        updateProgress(0.1d, 1d);
        context.deselectAllAttributes();
        final Concept<G, M> top = new Concept<G, M>(context.rowHeads(), Collections.<M> emptySet());
        lattice.rowHeads().add(top);
        updateProgress(0.2d, 1d);
        updateMessage("Computing Object Labels...");
        synchronized (lattice.objectConcepts) {
          for (G g : context.rowHeads())
            lattice.objectConcepts.put(g, top);
        }
        updateProgress(0.4d, 1d);
        layout.invalidate();
        for (int col = 0; col < context.colHeads().size(); col++) {
          final int _col = col;
          executor.submit(new BlockingTask("Selecting " + _col) {

            @Override
            protected void _call() {
              select(context.colHeads().get(_col));
            }
          });
        }
      }
    });
  }

  public final void initialize2() {
    executor.submit(new ImportTask<G, M>(this));
    executor.submit(new BlockingTask("Initialization") {

      @Override
      protected void _call() {
        updateProgress(0.1d, 1d);
        final NextClosures6.Result<G, M> result = NextClosures6.compute(context, false);
        for (Entry<Set<M>, Set<M>> e : result.implications.entrySet())
          implications.add(new Implication<G, M>(e.getKey(), e.getValue(), result.supports.get(e.getKey())));
        List<Concept<G, M>> concepts = new ArrayList<Concept<G, M>>();
        concepts.addAll(result.concepts);
        Collections.sort(concepts, new Comparator<Concept<G, M>>() {

          public final int compare(final Concept<G, M> c1, final Concept<G, M> c2) {
            return (int) Math.signum(c1.intent().size() - c2.intent().size());
          }
        });
        lattice.rowHeads().addAll(concepts);
        updateProgress(0.2d, 1d);
        updateMessage("Computing Object Labels...");
        updateProgress(0.4d, 1d);
//        layout.invalidate();
      }
    });
    executor.submit(new SeedsAndLabelsTask<G, M>(FCAInstance.this));
    executor.submit(IPred.neighborhood(lattice));
    relayout(0, 64);
  }

  public final void addObject(final G object) {
    executor.submit(new BlockingTask("New Object") {

      protected final void _call() {
        updateProgress(0.5d, 1d);
        conceptGraph.controller.graphLock.lock();
        conceptGraph.highlightLock.lock();
        unsavedChanges.set(true);
        context.rowHeads().add(object);
        context.pushAllChangedEvent();
        lattice.rowHeads().clear();
      }
    });
  }

  public final void addAttribute(final M attribute) {
    executor.submit(new BlockingTask("New Attribute") {

      protected final void _call() {
        updateProgress(0.5d, 1d);
        conceptGraph.controller.graphLock.lock();
        conceptGraph.highlightLock.lock();
        unsavedChanges.set(true);
        context.colHeads().add(attribute);
        context.deselectAttribute(attribute);
        context.pushAllChangedEvent();
//        select(attribute);
      }
    });
    executor.submit(IFox.select(layout, attribute, conflictDistance, tpe));
    executor.submit(new BlockingTask("New Attribute") {

      protected final void _call() {
        updateProgress(0.5d, 1d);
        conceptGraph.controller.graphLock.unlock();
        conceptGraph.highlightLock.unlock();
      }
    });
  }

  public final void flip(final G object, final M attribute) {
    executor.submit(new BlockingTask("Context Flip") {

      protected final void _call() {
        updateProgress(0.5d, 1d);
        if (!context.selectedAttributes().contains(attribute)) {
          if (context.contains(object, attribute))
            context.remove(object, attribute);
          else
            context.addFast(object, attribute);
          return;
        } else {
          executor.submit(new BlockingTask("Flip Init") {

            protected final void _call() {
              updateProgress(0.5d, 1d);
              if (!context.selectedAttributes().contains(attribute)) {
                if (context.contains(object, attribute))
                  context.remove(object, attribute);
                else
                  context.addFast(object, attribute);
                return;
              }
              conceptGraph.controller.graphLock.lock();
              conceptGraph.highlightLock.lock();
            }
          });
          executor.submit(IFox.ignore(layout, attribute, conflictDistance, tpe));
          executor.submit(new BlockingTask("Context Flip") {

            protected final void _call() {
              updateProgress(0.5d, 1d);
              conceptGraph.controller.graphLock.unlock();
              conceptGraph.highlightLock.unlock();
              if (layout.lattice.context.contains(object, attribute))
                layout.lattice.context.remove(object, attribute);
              else
                layout.lattice.context.addFast(object, attribute);
              conceptGraph.controller.graphLock.lock();
              conceptGraph.highlightLock.lock();
            }
          });
          executor.submit(IFox.select(layout, attribute, conflictDistance, tpe));

          executor.submit(new BlockingTask("Flip Finish") {

            protected final void _call() {
              updateProgress(0.5d, 1d);
              conceptGraph.controller.graphLock.unlock();
              conceptGraph.highlightLock.unlock();
            }
          });
        }
      }
    });
  }

  public final void select(final M attribute) {

    executor.submit(new BlockingTask("Select Init") {

      protected final void _call() {
        updateProgress(0.5d, 1d);
        conceptGraph.controller.graphLock.lock();
        conceptGraph.highlightLock.lock();
      }
    });
    executor.submit(IFox.select(layout, attribute, conflictDistance, tpe));

    executor.submit(new BlockingTask("Select Finish") {

      protected final void _call() {
        updateProgress(0.5d, 1d);
        conceptGraph.controller.graphLock.unlock();
        conceptGraph.highlightLock.unlock();
      }
    });
  }

  public final void ignore(final M attribute) {

    executor.submit(new BlockingTask("Ignore Init") {

      protected final void _call() {
        updateProgress(0.5d, 1d);
        conceptGraph.controller.graphLock.lock();
        conceptGraph.highlightLock.lock();
      }
    });
    executor.submit(IFox.ignore(layout, attribute, conflictDistance, tpe));

    executor.submit(new BlockingTask("Ignore Finish") {

      protected final void _call() {
        updateProgress(0.5d, 1d);
        conceptGraph.controller.graphLock.unlock();
        conceptGraph.highlightLock.unlock();
      }
    });
  }

  public final void relayout(final int generationCount, final int populationSize) {
    executor.submit(GeneticLayouter.seeds(
        layout,
        false,
        generationCount,
        populationSize,
        conceptGraph.threeDimensions(),
        conflictDistance,
        tpe));
  }

  public final void refine(final int generationCount) {
    executor.submit(GeneticLayouter.seeds(
        layout,
        true,
        generationCount,
        1,
        conceptGraph.threeDimensions(),
        conflictDistance,
        tpe));
  }

  public final LayoutEvolution<G, M> qualityChart(final Concept<G, M> concept, final ConceptMovement movement) {
    final LayoutEvolution<G, M> qualityEvolution =
        new LayoutEvolution<G, M>(layout, concept, movement, 2d, 2d, 32, 1, 16, conflictDistance, tpe);
    executor.submit(LayoutEvolution.calculate(qualityEvolution));
    return qualityEvolution;
  }

  public final void storeToFile() {
    executor.submit(new BlockingTask("Store") {

      @SuppressWarnings("incomplete-switch")
      protected final void _call() {
        updateProgress(0.5d, 1d);
        switch (FileFormat.of(file, FileFormat.CFX, FileFormat.CXT).second()) {
        case CXT:
          CXTExporter.export(context, contextWidget.rowHeaderPane.rowMap, contextWidget.colHeaderPane.columnMap, file);
          break;
        case CFX:
          CFXExporter.export(
              context,
              contextWidget.rowHeaderPane.rowMap,
              contextWidget.colHeaderPane.columnMap,
              layout,
              file);
          break;
        }
        id.set(file.getName());
        unsavedChanges.set(false);
        conExpFX.fileHistory.remove(file);
        conExpFX.fileHistory.add(0, file);
      }
    });
  }

  public void export(FileFormat svg, File file2) {
    exportToFile(file2);
  }

  public void exportTeX(TeXOptions options) {
    try {
      new TeXExporter<G, M>(
          context,
          contextWidget.rowHeaderPane.rowMap,
          contextWidget.colHeaderPane.columnMap,
          layout,
          options).export();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public final void exportToFile(final File file) {
    executor.submit(new BlockingTask("Export") {

      @SuppressWarnings("incomplete-switch")
      protected final void _call() {
        updateProgress(0.5d, 1d);
        switch (FileFormat
            .of(file, FileFormat.TEX, FileFormat.PNG, FileFormat.SVG, FileFormat.PDF, FileFormat.HTML)
            .second()) {
        case TEX:
          exportTeX(new TeXOptions(
              file,
              false,
              true,
              false,
              ContextTeXPackage.Ganter,
              DiagramTeXPackage.Ganter,
              new FitScale(80, 120)));
          break;
        case PNG:
          PNGExporter.export(
              context,
              contextWidget.rowHeaderPane.rowMap,
              contextWidget.colHeaderPane.columnMap,
              layout,
              true,
              true,
              file);
          break;
        case SVG:
          SVGExporter.export(
              context,
              contextWidget.rowHeaderPane.rowMap,
              contextWidget.colHeaderPane.columnMap,
              layout,
              true,
              true,
              file);
          break;
        case PDF:
          PDFExporter.export(
              context,
              contextWidget.rowHeaderPane.rowMap,
              contextWidget.colHeaderPane.columnMap,
              layout,
              true,
              true,
              file);
          break;
        case HTML:
          HTMLExporter.export(
              context,
              contextWidget.rowHeaderPane.rowMap,
              contextWidget.colHeaderPane.columnMap,
              layout,
              true,
              true,
              file);
          break;
        }
      }
    });
  }

  public void calcImplications() {
    executor.submit(new BlockingTask("Clear implications list") {

      @Override
      protected void _call() {
        implications.clear();
      }
    });
//    executor.submit(NextImplication2.implications(context, implications));
  }

  public final void save() {
    if (file == null)
      saveAs();
    else
      storeToFile();
  }

  public final void saveAs() {
    final FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Formal Context File");
    if (conExpFX.lastDirectory != null)
      fileChooser.setInitialDirectory(conExpFX.lastDirectory);
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Context & Lattice (ConExpFX Format, *.cfx)", "*.cfx"));
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Only Context (Burmeister Format, *.cxt)", "*.cxt"));
    final File file = fileChooser.showSaveDialog(conExpFX.primaryStage);
    if (file != null) {
      this.file = file;
      conExpFX.lastDirectory = file.getParentFile();
      storeToFile();
    }
  }

  public final void export() {
    final FileChooser fileChooser = new FileChooser();
    if (conExpFX.lastDirectory != null)
      fileChooser.setInitialDirectory(conExpFX.lastDirectory);
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
    final File file = fileChooser.showSaveDialog(conExpFX.primaryStage);
    if (file != null) {
      conExpFX.lastDirectory = file.getParentFile();
      exportToFile(file);
    }
  }

  public void exportTeX() {
    final Return<TeXOptions> ret = new TeXDialog<G, M>(this.conExpFX.primaryStage).showAndWait();
    if (ret.result().equals(Result.OK)) {
      final FileChooser chooser = new FileChooser();
      chooser.getExtensionFilters().add(new ExtensionFilter("LaTeX File (*.tex)", "*.tex"));
      chooser.setInitialDirectory(conExpFX.lastDirectory);
      final File file = chooser.showSaveDialog(conExpFX.primaryStage);
      if (file == null)
        return;
      final TeXOptions value = ret.value();
      value.file = file;
      exportTeX(value);
    }
  }

  public final void shutdown() {

  }

}
