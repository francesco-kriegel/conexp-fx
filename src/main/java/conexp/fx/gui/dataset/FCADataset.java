package conexp.fx.gui.dataset;

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
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import conexp.fx.core.algorithm.exploration.AttributeExploration;
import conexp.fx.core.algorithm.exploration.ParallelAttributeExploration;
import conexp.fx.core.algorithm.lattice.IFox2;
import conexp.fx.core.algorithm.lattice.IPred;
import conexp.fx.core.algorithm.nextclosures.NextClosures2Bit;
import conexp.fx.core.builder.FileRequest;
import conexp.fx.core.builder.Request;
import conexp.fx.core.builder.Requests.Source;
import conexp.fx.core.builder.StringRequest;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.Context;
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
import conexp.fx.core.layout.AdditiveConceptLayout;
import conexp.fx.core.layout.AdditiveConceptLayout.Type;
import conexp.fx.core.layout.ConceptMovement;
import conexp.fx.core.layout.GeneticLayouter;
import conexp.fx.core.layout.LayoutEvolution;
import conexp.fx.core.layout.QualityMeasure;
import conexp.fx.core.util.Constants;
import conexp.fx.core.util.FileFormat;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.context.ConceptWidget;
import conexp.fx.gui.context.ImplicationWidget;
import conexp.fx.gui.context.MatrixContextWidget;
import conexp.fx.gui.dialog.FXDialog.Answer;
import conexp.fx.gui.dialog.FXDialog.Return;
import conexp.fx.gui.dialog.TeXDialog;
import conexp.fx.gui.graph.CircularGraph;
import conexp.fx.gui.graph.ConceptGraph;
import conexp.fx.gui.graph.PolarGraph;
import conexp.fx.gui.task.TimeTask;
import conexp.fx.gui.util.Platform2;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public final class FCADataset<G, M> extends Dataset {

  public final Request<G, M>                                     request;
  public final MatrixContext<G, M>                               context;
  public final ConceptLattice<G, M>                              lattice;
  public final AdditiveConceptLayout<G, M>                       layout;
//  private final SimpleConceptLayout<G, M>                        l;
  public final ObservableList<Concept<G, M>>                     concepts            =
      FXCollections.<Concept<G, M>> observableArrayList();
  public final ObservableList<Implication<G, M>>                 implications        =
      FXCollections.<Implication<G, M>> observableArrayList();
  public final ObservableList<Implication<G, M>>                 partialImplications =
      FXCollections.observableArrayList();

  public final QualityMeasure<G, M, Pair<Concept<G, M>, Double>> conflictDistance    =
      QualityMeasure.conflictDistance();
//  public final EdgeIntersections<G, M> edgeIntersections = new EdgeIntersections<G, M>();
  public boolean                                                 editable            = false;

  public final MatrixContextWidget<G, M>                         contextWidget;
  public final ConceptGraph<G, M>                                conceptGraph;
  public final ConceptWidget<G, M>                               conceptWidget;
  public final ImplicationWidget<G, M>                           implicationWidget;

  public FCADataset(final Dataset parentDataset, final Request<G, M> request) {
    super(parentDataset);
    this.request = request;
    this.context = request.createContext(AutomaticMode.REDUCE);
    this.context.id.bind(id);
    this.lattice = new ConceptLattice<G, M>(context);
    this.layout = new AdditiveConceptLayout<G, M>(lattice, null, null, Type.HYBRID);
    this.layout.observe();
    this.id.set(request.getId());
    if (request.src != Source.FILE)
      unsavedChanges.set(true);
    else if (request instanceof FileRequest)
      file = ((FileRequest) request).file;
//    this.layout.observe();
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
//    final ExecutorStatusBar conExp.executorStatusBar = new ExecutorStatusBar();
//    conExp.executorStatusBar.bindTo(conExp.executor);
//    this.statusWidget = conExp.executorStatusBar.statusBar;
    if (request instanceof FileRequest) {
      this.editable = true;
//      this.contextWidget = (MatrixContextWidget<G, M>) new StringMatrixContextWidget((FCADataset<String, String>) this);
    } else if (request instanceof StringRequest) {
      this.editable = true;
//      this.contextWidget = (MatrixContextWidget<G, M>) new StringMatrixContextWidget((FCADataset<String, String>) this);
    } // else
    this.contextWidget = new MatrixContextWidget<G, M>(this);
    this.conceptGraph = new ConceptGraph<G, M>(this);
    this.conceptWidget = new ConceptWidget<G, M>(this);
    this.implicationWidget = new ImplicationWidget<G, M>(this);
    lattice.addEventHandler(event -> {
      concepts.clear();
      concepts.addAll(lattice.colHeads());
    } , RelationEvent.ALL_CHANGED);
    views.add(new DatasetView<Context<G, M>>("Context", contextWidget, context));
    views.add(new DatasetView<AdditiveConceptLayout<G, M>>("Lattice", conceptGraph, layout));
//    this.l = new SimpleConceptLayout<G, M>(lattice);
//    views.add(new DatasetView<SimpleConceptLayout<G, M>>("Force Lattice", new ConceptGraph<G, M>(this, l), l));
    views.add(new DatasetView<List<Concept<G, M>>>("Concepts", conceptWidget, concepts));
    views.add(new DatasetView<List<Implication<G, M>>>("Implications", implicationWidget, implications));
    defaultActiveViews.add("Lattice");
    actions.add(new DatasetAction("Polar Layout", () -> polarLayout()));
    actions.add(new DatasetAction("Circular Layout", () -> circularLayout()));
    if (editable) {
      actions.add(new DatasetAction("Explore...", () -> {
        implications.clear();
        try {
          AttributeExploration.withHumanExpert(((MatrixContext<String, String>) context).getSelection()).start();
        } catch (InterruptedException __) {}
      }));
      actions.add(new DatasetAction("Explore (parallel)...", () -> {
        implications.clear();
        ConExpFX.execute(ParallelAttributeExploration.createExplorationTask((FCADataset<String, String>) this));
      }));
    }
    actions.add(new DatasetAction("Refresh", () -> reinitializeWithNextClosures()));
    Platform2.runOnFXThread(() -> initializeWithNextClosures());
  }

  private final void polarLayout() {
    new PolarGraph<G, M>(lattice).show();
  }

  private final void circularLayout() {
    new CircularGraph<G, M>(lattice).show();
  }

  public final void initializeWithIFox() {
    ConExpFX.execute(TimeTask.create(this, "Importing Formal Context", () -> {
      request.setContent();
      context.addEventHandler(
          __ -> Platform.runLater(() -> unsavedChanges.set(true)),
          RelationEvent.ROWS,
          RelationEvent.COLUMNS,
          RelationEvent.ENTRIES);
    }));
    ConExpFX.execute(new TimeTask<Void>("Initialization") {

      @Override
      protected Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
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
          ConExpFX.execute(new TimeTask<Void>("Selecting " + _col) {

            @Override
            protected Void call() {
              updateProgress(0d, 1d);
              if (isCancelled())
                return null;
              selectAttribute(context.colHeads().get(_col));
              updateProgress(1d, 1d);
              return null;
            }
          });
        }
        updateProgress(1d, 1d);
        return null;
      }
    });
  }

  public final void initializeWithNextClosures() {
    ConExpFX.execute(TimeTask.create(this, "Importing Formal Context", () -> {
      request.setContent();
//    context.addEventHandler(__ -> reinitializeWithNextClosures(), RelationEvent.ROWS);
      context.addEventHandler(
          __ -> Platform.runLater(() -> unsavedChanges.set(true)),
          RelationEvent.ROWS,
          RelationEvent.COLUMNS,
          RelationEvent.ENTRIES);
    }));
    _reinitializeWithNextClosures();
  }

  private final void reinitializeWithNextClosures() {
    ConExpFX.execute(TimeTask.create(this, "Reinit", () -> {
      layout.seedsG.clear();
      layout.seedsM.clear();
      lattice.objectConcepts.clear();
      lattice.attributeConcepts.clear();
      conceptGraph.removeContent();
      lattice.rowHeads().clear();
      concepts.clear();
      implications.clear();
      partialImplications.clear();
    } , true));
    _reinitializeWithNextClosures();
  }

  private final void _reinitializeWithNextClosures() {
    ConExpFX.execute(new TimeTask<Void>(this, "Locking Graph") {

      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateProgress(0.5d, 1d);
        conceptGraph.controller.graphLock = true;
        conceptGraph.highlightLock = true;
        updateProgress(1d, 1d);
        return null;
      }
    });
    ConExpFX.execute(TimeTask.create(this, "Reduce Formal Context", () -> context.pushAllChangedEvent()
//      context.initHandlers(true, MatrixContext.AutomaticMode.REDUCE)
//  final int rows = this.fcaInstance.context.rowHeads().size();
//  final int cols = this.fcaInstance.context.colHeads().size();
//  this.fcaInstance.context.initHandlers(true, MatrixContext.AutomaticMode.fromSize(rows,cols));
    ));
    ConExpFX.execute(NextClosures2Bit.createTask(this));
    ConExpFX.execute(
        TimeTask.create(
            this,
            "Sorting Formal Concepts",
            () -> lattice.rowHeads().addAll(
                concepts
                    .parallelStream()
                    .sorted((c1, c2) -> (int) Math.signum(c1.intent().size() - c2.intent().size()))
                    .collect(Collectors.toList()))));
    ConExpFX.execute(GeneticLayouter.initialSeeds(this));
    ConExpFX.execute(IPred.neighborhoodP(this));
    ConExpFX.execute(new TimeTask<Void>(this, "Unlocking Graph") {

      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateProgress(0.5d, 1d);
        conceptGraph.controller.graphLock = false;
        conceptGraph.highlightLock = false;
        updateProgress(1d, 1d);
        return null;
      }
    });
    ConExpFX.execute(TimeTask.create(this, "Initialize Concept Lattice Graph", () -> {
//      Platform2.runOnFXThread(() -> layout.invalidate());
//      new Thread(() -> lattice.pushAllChangedEvent()).start();
      lattice.pushAllChangedEvent();
//      Platform2.runOnFXThread(() -> layout.invalidate());
    }));
    ConExpFX.execute(
        TimeTask.create(
            this,
            "Computing Partial Implications",
            () -> implications.addAll(lattice.luxenburgerBase(0d, true))));
//ConExpFX.execute(new TimeTask<Void>(this, "Computing Partial Implications") {
//
//@Override
//protected Void call() {
//  updateProgress(0d, 1d);
//  if (isCancelled())
//    return null;
//  conceptGraph.controller.graphLock = true;
//  conceptGraph.highlightLock = true;
//  updateProgress(0.25d, 1d);
//  implications.addAll(lattice.luxenburgerBase(0d, true));
//  updateProgress(0.75d, 1d);
//  conceptGraph.controller.graphLock = false;
//  conceptGraph.highlightLock = false;
//  updateProgress(1d, 1d);
//  return null;
//}
//});
    relayout(0, Constants.POPULATION);
    ConExpFX.execute(TimeTask.create(this, "Initialize Concept Lattice Graph", layout::invalidate, true));
//    ConExpFX.execute(TimeTask.create(this, "Force Layouter", () -> {
//      for (Concept<G, M> c : lattice.rowHeads())
//        l.getOrAddPosition(c).setValue(layout.getPosition(c).getValue());
//      l.start();
//    }));
  }

  public final void addObject(final G object, final int index) {
    ConExpFX.execute(new TimeTask<Void>(this, "New Object") {

      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateProgress(0.5d, 1d);
//        conceptGraph.controller.graphLock = true;
//        conceptGraph.highlightLock = true;
//        context.pushAllChangedEvent();
        if (index == -1)
          context.rowHeads().add(object);
        else
          context.rowHeads().add(index, object);
        Platform2.runOnFXThread(() -> {
          unsavedChanges.set(true);
        });
//        lattice.rowHeads().clear();
        updateProgress(1d, 1d);
        return null;
      }
    });
    this.reinitializeWithNextClosures();
  }

  public final void addAttribute(final M attribute, final int index) {
    ConExpFX.execute(new TimeTask<Void>(this, "New Attribute") {

      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateProgress(0.5d, 1d);
        conceptGraph.controller.graphLock = true;
        conceptGraph.highlightLock = true;
        Platform2.runOnFXThread(() -> unsavedChanges.set(true));
        if (index == -1)
          context.colHeads().add(attribute);
        else
          context.colHeads().add(index, attribute);
        context.deselectAttribute(attribute);
        context.pushAllChangedEvent();
        // select(attribute);
        updateProgress(1d, 1d);
        return null;
      }
    });
    ConExpFX.execute(IFox2.select(id.get(), layout, attribute, conflictDistance, ConExpFX.getThreadPool()));
    ConExpFX.execute(new TimeTask<Void>(this, "New Attribute") {

      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateProgress(0.5d, 1d);
        conceptGraph.controller.graphLock = false;
        conceptGraph.highlightLock = false;
        updateProgress(1d, 1d);
        return null;
      }
    });
  }

  public final void removeObject(final G object) {
    ConExpFX.execute(new TimeTask<Void>(this, "Removing Object") {

      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateProgress(0.5d, 1d);
        // conceptGraph.controller.graphLock = true;
        // conceptGraph.highlightLock = true;
        // context.pushAllChangedEvent();
        context.rowHeads().remove(object);
        Platform2.runOnFXThread(() -> unsavedChanges.set(true));
        // lattice.rowHeads().clear();
        updateProgress(1d, 1d);
        return null;
      }
    });
    this.reinitializeWithNextClosures();
  }

  public final void removeAttribute(final M attribute) {
    ConExpFX.execute(TimeTask.create(this, "Removing Attribute", () -> {
      conceptGraph.controller.graphLock = true;
      conceptGraph.highlightLock = true;
      Platform2.runOnFXThread(() -> unsavedChanges.set(true));
//      context.deselectAttribute(attribute);
//      context.pushAllChangedEvent();
    }));
    if (context.selectedAttributes().contains(attribute))
      ConExpFX.execute(IFox2.ignore(id.get(), layout, attribute, conflictDistance, ConExpFX.getThreadPool()));
    ConExpFX.execute(TimeTask.create(this, "Removing Attribute", () -> {
      context.colHeads().remove(attribute);
      context.pushAllChangedEvent();
      conceptGraph.controller.graphLock = false;
      conceptGraph.highlightLock = false;
    }));
  }

  public final void renameObject(final G oldName, final G newName) {
    if (oldName.equals(newName))
      return;
    ConExpFX.execute(TimeTask.create(this, "Renaming Object", () -> {
      context.rowHeads().set(oldName, newName);
      Platform2.runOnFXThread(() -> unsavedChanges.set(true));
    }));
    this.reinitializeWithNextClosures();
  }

  public final void renameAttribute(final M oldName, final M newName) {
    if (oldName.equals(newName))
      return;
    ConExpFX.execute(TimeTask.create(this, "Renaming Attribute", () -> {
      context.colHeads().set(oldName, newName);
      Platform2.runOnFXThread(() -> unsavedChanges.set(true));
    }));
    this.reinitializeWithNextClosures();
  }

  public final void flip(final G object, final M attribute) {
    if (!context.selectedAttributes().contains(attribute) || !context.selectedObjects().contains(object)) {
      if (context.contains(object, attribute))
        context.remove(object, attribute);
      else
        context.addFast(object, attribute);
    } else {
      ConExpFX.execute(
          TimeTask.compose(
              FCADataset.this,
              "Flipping Incidence",
              new TimeTask<Void>(FCADataset.this, "Flip Init") {

                protected final Void call() {
                  updateProgress(0d, 1d);
                  // if (isCancelled())
                  // return null;
                  updateProgress(0.5d, 1d);
                  conceptGraph.controller.graphLock = true;
                  conceptGraph.highlightLock = true;
                  updateProgress(1d, 1d);
                  return null;
                }
              },
              IFox2.ignore(id.get(), layout, attribute, conflictDistance, ConExpFX.getThreadPool()),
              new TimeTask<Void>(FCADataset.this, "Context Flip") {

                protected final Void call() {
                  updateProgress(0d, 1d);
                  // if (isCancelled())
                  // return null;
                  updateProgress(0.5d, 1d);
                  conceptGraph.controller.graphLock = false;
                  conceptGraph.highlightLock = false;
                  if (layout.lattice.context.contains(object, attribute))
                    layout.lattice.context.remove(object, attribute);
                  else
                    layout.lattice.context.addFast(object, attribute);
                  conceptGraph.controller.graphLock = true;
                  conceptGraph.highlightLock = true;
                  updateProgress(1d, 1d);
                  return null;
                }
              },
              IFox2.select(id.get(), layout, attribute, conflictDistance, ConExpFX.getThreadPool()),
              new TimeTask<Void>(FCADataset.this, "Flip Finish") {

                protected final Void call() {
                  updateProgress(0d, 1d);
                  // if (isCancelled())
                  // return null;
                  updateProgress(0.5d, 1d);
                  conceptGraph.controller.graphLock = false;
                  conceptGraph.highlightLock = false;
                  updateProgress(1d, 1d);
                  return null;
                }
              }));
      relayout(2, 2);
    }
  }

  public final void selectObject(final G object) {
    ConExpFX.execute(TimeTask.create(this, "Selecting Object", () -> context.selectObject(object)));
    this.reinitializeWithNextClosures();
  }

  public final void ignoreObject(final G object) {
    ConExpFX.execute(TimeTask.create(this, "Unselecting Object", () -> context.deselectObject(object)));
    this.reinitializeWithNextClosures();
  }

  public final void selectAttribute(final M attribute) {
    ConExpFX.execute(TimeTask.compose(
        FCADataset.this,
        "Selecting Attribute",
        new TimeTask<Void>(this, "Select Init") {

          protected final Void call() {
            updateProgress(0d, 1d);
            if (isCancelled())
              return null;
            updateProgress(0.5d, 1d);
            conceptGraph.controller.graphLock = true;
            conceptGraph.highlightLock = true;
            updateProgress(1d, 1d);
            return null;
          }
        },
        IFox2.select(id.get(), layout, attribute, conflictDistance, ConExpFX.getThreadPool()),
        new TimeTask<Void>(this, "Select Finish") {

          protected final Void call() {
            updateProgress(0d, 1d);
            if (isCancelled())
              return null;
            updateProgress(0.5d, 1d);
            conceptGraph.controller.graphLock = false;
            conceptGraph.highlightLock = false;
            updateProgress(1d, 1d);
            return null;
          }
        }));
    relayout(2, 2);
  }

  public final void ignoreAttribute(final M attribute) {
    ConExpFX
        .execute(TimeTask.compose(
            FCADataset.this,
            "Unselecting attribute",
            new TimeTask<Void>(this, "Ignore Init") {

              protected final Void call() {
                updateProgress(0d, 1d);
                if (isCancelled())
                  return null;
                updateProgress(0.5d, 1d);
                conceptGraph.controller.graphLock = true;
                conceptGraph.highlightLock = true;
                updateProgress(1d, 1d);
                return null;
              }
            },
            IFox2.ignore(id.get(), layout, attribute, conflictDistance, ConExpFX.getThreadPool()),
            new TimeTask<Void>(this, "Ignore Finish") {

              protected final Void call() {
                updateProgress(0d, 1d);
                if (isCancelled())
                  return null;
                updateProgress(0.5d, 1d);
                conceptGraph.controller.graphLock = false;
                conceptGraph.highlightLock = false;
                updateProgress(1d, 1d);
                return null;
              }
            }));
    relayout(2, 2);
  }

  public final void relayout(final int generationCount, final int populationSize) {
    ConExpFX.execute(GeneticLayouter.seeds(this, false, generationCount, populationSize));
  }

  public final void refine(final int generationCount) {
    ConExpFX.execute(GeneticLayouter.seeds(this, true, generationCount, 1));
  }

  public final LayoutEvolution<G, M> qualityChart(final Concept<G, M> concept, final ConceptMovement movement) {
    final LayoutEvolution<G, M> qualityEvolution = new LayoutEvolution<G, M>(
        layout,
        concept,
        movement,
        2d,
        2d,
        32,
        1,
        16,
        conflictDistance,
        ConExpFX.instance.executor.tpe);
    ConExpFX.execute(LayoutEvolution.calculate(qualityEvolution));
    return qualityEvolution;
  }

  public final void storeToFile() {
    ConExpFX.execute(new TimeTask<Void>(this, "Store") {

      @SuppressWarnings("incomplete-switch")
      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateProgress(0.5d, 1d);
        switch (FileFormat.of(file, FileFormat.CFX, FileFormat.CXT).second()) {
        case CXT:
          CXTExporter.export(context, contextWidget.rowHeaderPane.rowMap, contextWidget.colHeaderPane.columnMap, file);
          break;
        case CFX:
          CFXExporter
              .export(context, contextWidget.rowHeaderPane.rowMap, contextWidget.colHeaderPane.columnMap, layout, file);
          break;
        }
        Platform2.runOnFXThread(() -> {
          id.set(file.getName());
          unsavedChanges.set(false);
          ConExpFX.instance.fileHistory.remove(file);
          ConExpFX.instance.fileHistory.add(0, file);
        });
        updateProgress(1d, 1d);
        return null;
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
    ConExpFX.execute(new TimeTask<Void>(this, "Export") {

      @SuppressWarnings("incomplete-switch")
      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateProgress(0.5d, 1d);
        switch (FileFormat
            .of(file, FileFormat.TEX, FileFormat.PNG, FileFormat.SVG, FileFormat.PDF, FileFormat.HTML)
            .second()) {
        case TEX:
          exportTeX(
              new TeXOptions(
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
        updateProgress(1d, 1d);
        return null;
      }
    });
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
    if (ConExpFX.instance.lastDirectory != null)
      fileChooser.setInitialDirectory(ConExpFX.instance.lastDirectory);
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Context & Lattice (ConExpFX Format, *.cfx)", "*.cfx"));
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Only Context (Burmeister Format, *.cxt)", "*.cxt"));
    final File file = fileChooser.showSaveDialog(ConExpFX.instance.primaryStage);
    if (file != null) {
      this.file = file;
      ConExpFX.instance.lastDirectory = file.getParentFile();
      storeToFile();
    }
  }

  public final void export() {
    final FileChooser fileChooser = new FileChooser();
    if (ConExpFX.instance.lastDirectory != null)
      fileChooser.setInitialDirectory(ConExpFX.instance.lastDirectory);
    fileChooser
        .getExtensionFilters()
        .add(new ExtensionFilter("Context & Lattice (TeX - Ganter's fca.sty, *.tex)", "*.tex"));
    fileChooser
        .getExtensionFilters()
        .add(new ExtensionFilter("Only Lattice (Portable Network Graphics, *.png)", "*.png"));
    fileChooser
        .getExtensionFilters()
        .add(new ExtensionFilter("Only Lattice (Scalable Vector Graphics, *.svg)", "*.svg"));
    fileChooser
        .getExtensionFilters()
        .add(new ExtensionFilter("Only Lattice (Portable Document Format, *.pdf)", "*.pdf"));
    fileChooser
        .getExtensionFilters()
        .add(new ExtensionFilter("Only Context (Hypertext Markup Language, *.html)", "*.html"));
    final File file = fileChooser.showSaveDialog(ConExpFX.instance.primaryStage);
    if (file != null) {
      ConExpFX.instance.lastDirectory = file.getParentFile();
      exportToFile(file);
    }
  }

  @Deprecated
  public void exportTeX() {
    final Return<TeXOptions> ret = new TeXDialog<G, M>(ConExpFX.instance.primaryStage).showAndWait();
    if (ret.result().equals(Answer.OK)) {
      final FileChooser chooser = new FileChooser();
      chooser.getExtensionFilters().add(new ExtensionFilter("LaTeX File (*.tex)", "*.tex"));
      chooser.setInitialDirectory(ConExpFX.instance.lastDirectory);
      final File file = chooser.showSaveDialog(ConExpFX.instance.primaryStage);
      if (file == null)
        return;
      final TeXOptions value = ret.value();
      value.file = file;
      exportTeX(value);
    }
  }

  public final void shutdown() {

  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

}
