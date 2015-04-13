package conexp.fx.gui.dataset;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import conexp.fx.core.algorithm.exploration.AttributeExploration;
import conexp.fx.core.algorithm.lattice.IFox;
import conexp.fx.core.algorithm.lattice.IPred;
import conexp.fx.core.algorithm.nextclosures.NextClosures6;
import conexp.fx.core.algorithm.nextclosures.Result6;
import conexp.fx.core.builder.FileRequest;
import conexp.fx.core.builder.Request;
import conexp.fx.core.builder.Requests.Source;
import conexp.fx.core.builder.StringRequest;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.Context;
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
import conexp.fx.core.implication.Implication;
import conexp.fx.core.layout.ConceptLayout;
import conexp.fx.core.layout.ConceptMovement;
import conexp.fx.core.layout.GeneticLayouter;
import conexp.fx.core.quality.ConflictDistance;
import conexp.fx.core.quality.LayoutEvolution;
import conexp.fx.core.util.Constants;
import conexp.fx.core.util.FileFormat;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.concept.ConceptWidget;
import conexp.fx.gui.context.MatrixContextWidget;
import conexp.fx.gui.context.StringMatrixContextWidget;
import conexp.fx.gui.dialog.FXDialog.Result;
import conexp.fx.gui.dialog.FXDialog.Return;
import conexp.fx.gui.dialog.TeXDialog;
import conexp.fx.gui.graph.CircularGraph;
import conexp.fx.gui.graph.ConceptGraph;
import conexp.fx.gui.graph.PolarGraph;
import conexp.fx.gui.implication.ImplicationWidget;
import conexp.fx.gui.task.BlockingTask;
import conexp.fx.gui.task.ImportTask;
import conexp.fx.gui.task.SeedsAndLabelsTask;
import conexp.fx.gui.util.Platform2;

public final class FCADataset<G, M> extends Dataset {

  public final Request<G, M>                     request;
  public final MatrixContext<G, M>               context;
  public final ConceptLattice<G, M>              lattice;
  public final ConceptLayout<G, M>               layout;
  public final ObservableList<Concept<G, M>>     concepts            = FXCollections
                                                                         .<Concept<G, M>> observableArrayList();
  public final ObservableList<Implication<G, M>> implications        = FXCollections
                                                                         .<Implication<G, M>> observableArrayList();
  public final ObservableList<Implication<G, M>> partialImplications = FXCollections.observableArrayList();

  public final ConflictDistance<G, M>            conflictDistance    = new ConflictDistance<G, M>();
  public boolean                                 editable            = false;

  public final MatrixContextWidget<G, M>         contextWidget;
  public final ConceptGraph<G, M>                conceptGraph;
  public final ConceptWidget<G, M>               conceptWidget;
  public final ImplicationWidget<G, M>           implicationWidget;

  public FCADataset(final Dataset parentDataset, final Request<G, M> request) {
    super(parentDataset);
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
//    final ExecutorStatusBar conExp.executorStatusBar = new ExecutorStatusBar();
//    conExp.executorStatusBar.bindTo(conExp.executor);
//    this.statusWidget = conExp.executorStatusBar.statusBar;
    if (request instanceof FileRequest) {
      this.editable = true;
      this.contextWidget = (MatrixContextWidget<G, M>) new StringMatrixContextWidget((FCADataset<String, String>) this);
      ConExpFX.instance.fileHistory.remove(((FileRequest) request).file);
      ConExpFX.instance.fileHistory.add(
          0,
          ((FileRequest) request).file);
    } else if (request instanceof StringRequest) {
      this.editable = true;
      this.contextWidget = (MatrixContextWidget<G, M>) new StringMatrixContextWidget((FCADataset<String, String>) this);
    } else
      this.contextWidget = new MatrixContextWidget<G, M>(this);
    this.conceptGraph = new ConceptGraph<G, M>(this);
    this.conceptWidget = new ConceptWidget<G, M>(this);
    this.implicationWidget = new ImplicationWidget<G, M>(this);
    lattice.addEventHandler(
        new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

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
        },
        RelationEvent.ALL_CHANGED);
    views.add(new DatasetView<Context<G, M>>("Context", contextWidget, context));
    views.add(new DatasetView<ConceptLayout<G, M>>("Lattice", conceptGraph, layout));
    views.add(new DatasetView<List<Concept<G, M>>>("Concepts", conceptWidget, concepts));
    views.add(new DatasetView<List<Implication<G, M>>>("Implications", implicationWidget, implications));
    defaultActiveViews.add("Lattice");
    actions.add(new DatasetAction("Polar Layout", () -> polarLayout()));
    actions.add(new DatasetAction("Circular Layout", () -> circularLayout()));
    if (editable)
      actions.add(new DatasetAction("Explore...", () -> AttributeExploration
          .withHumanExpert((MatrixContext<String, String>) context)));
    this.initialize2();
  }

  private final void polarLayout() {
    new PolarGraph<G, M>(lattice).show();
  }

  private final void circularLayout() {
    new CircularGraph<G, M>(lattice).show();
  }

  public final void initialize() {
    ConExpFX.instance.exe.submit(new ImportTask<G, M>(this));
    ConExpFX.instance.exe.submit(new BlockingTask("Initialization") {

      @Override
      protected void _call() {
        updateProgress(
            0.1d,
            1d);
        context.deselectAllAttributes();
        final Concept<G, M> top = new Concept<G, M>(context.rowHeads(), Collections.<M> emptySet());
        lattice.rowHeads().add(
            top);
        updateProgress(
            0.2d,
            1d);
        updateMessage("Computing Object Labels...");
        synchronized (lattice.objectConcepts) {
          for (G g : context.rowHeads())
            lattice.objectConcepts.put(
                g,
                top);
        }
        updateProgress(
            0.4d,
            1d);
        layout.invalidate();
        for (int col = 0; col < context.colHeads().size(); col++) {
          final int _col = col;
          ConExpFX.instance.exe.submit(new BlockingTask("Selecting " + _col) {

            @Override
            protected void _call() {
              select(context.colHeads().get(
                  _col));
            }
          });
        }
      }
    });
  }

  public final void initialize2() {
    ConExpFX.instance.exe.submit(new ImportTask<G, M>(this));
    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Initialization") {

      @Override
      protected void _call() {
        updateProgress(
            0.1d,
            1d);
        final Result6<G, M> result = NextClosures6.compute(
            context,
            true);
        for (Entry<Set<M>, Set<M>> e : result.implications.entrySet())
          implications.add(new Implication<G, M>(e.getKey(), e.getValue(), result.supports.get(e.getKey())));
//        List<Concept<G, M>> concepts = new ArrayList<Concept<G, M>>();
//        concepts.addAll(result.concepts);
        updateProgress(
            0.5d,
            1d);
        updateMessage("Sorting Concepts...");
        lattice.rowHeads().addAll(
            result.concepts.parallelStream().sorted(
                (c1, c2) -> (int) Math.signum(c1.intent().size() - c2.intent().size())).collect(
                Collectors.toList()));
        updateProgress(
            0.75d,
            1d);
        updateMessage("Computing Object Labels...");
        updateProgress(
            0.9d,
            1d);
//        layout.invalidate();
      }
    });
    ConExpFX.instance.exe.submit(new SeedsAndLabelsTask<G, M>(FCADataset.this));
    ConExpFX.instance.exe.submit(IPred.neighborhood(
        id.get(),
        lattice));
    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Computing Partial Implications") {

      @Override
      protected void _call() {
        conceptGraph.controller.graphLock.lock();
        conceptGraph.highlightLock.lock();
        updateProgress(
            0.25d,
            1d);
        implications.addAll(lattice.luxenburgerBase(
            0d,
            true));
        updateProgress(
            0.75d,
            1d);
        conceptGraph.controller.graphLock.unlock();
        conceptGraph.highlightLock.unlock();
      }
    });
    relayout(
        0,
        Constants.POPULATION);
  }

  public final void addObject(final G object) {
    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - New Object") {

      protected final void _call() {
        updateProgress(
            0.5d,
            1d);
        conceptGraph.controller.graphLock.lock();
        conceptGraph.highlightLock.lock();
        unsavedChanges.set(true);
        context.rowHeads().add(
            object);
        context.pushAllChangedEvent();
        lattice.rowHeads().clear();
      }
    });
  }

  public final void addAttribute(final M attribute) {
    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - New Attribute") {

      protected final void _call() {
        updateProgress(
            0.5d,
            1d);
        conceptGraph.controller.graphLock.lock();
        conceptGraph.highlightLock.lock();
        unsavedChanges.set(true);
        context.colHeads().add(
            attribute);
        context.deselectAttribute(attribute);
        context.pushAllChangedEvent();
//        select(attribute);
      }
    });
    ConExpFX.instance.exe.submit(IFox.select(
        id.get(),
        layout,
        attribute,
        conflictDistance,
        ConExpFX.instance.exe.tpe));
    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - New Attribute") {

      protected final void _call() {
        updateProgress(
            0.5d,
            1d);
        conceptGraph.controller.graphLock.unlock();
        conceptGraph.highlightLock.unlock();
      }
    });
  }

  public final void flip(final G object, final M attribute) {
    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Context Flip") {

      protected final void _call() {
        updateProgress(
            0.5d,
            1d);
        if (!context.selectedAttributes().contains(
            attribute)) {
          if (context.contains(
              object,
              attribute))
            context.remove(
                object,
                attribute);
          else
            context.addFast(
                object,
                attribute);
          return;
        } else {
          ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Flip Init") {

            protected final void _call() {
              updateProgress(
                  0.5d,
                  1d);
              if (!context.selectedAttributes().contains(
                  attribute)) {
                if (context.contains(
                    object,
                    attribute))
                  context.remove(
                      object,
                      attribute);
                else
                  context.addFast(
                      object,
                      attribute);
                return;
              }
              conceptGraph.controller.graphLock.lock();
              conceptGraph.highlightLock.lock();
            }
          });
          ConExpFX.instance.exe.submit(IFox.ignore(
              id.get(),
              layout,
              attribute,
              conflictDistance,
              ConExpFX.instance.exe.tpe));
          ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Context Flip") {

            protected final void _call() {
              updateProgress(
                  0.5d,
                  1d);
              conceptGraph.controller.graphLock.unlock();
              conceptGraph.highlightLock.unlock();
              if (layout.lattice.context.contains(
                  object,
                  attribute))
                layout.lattice.context.remove(
                    object,
                    attribute);
              else
                layout.lattice.context.addFast(
                    object,
                    attribute);
              conceptGraph.controller.graphLock.lock();
              conceptGraph.highlightLock.lock();
            }
          });
          ConExpFX.instance.exe.submit(IFox.select(
              id.get(),
              layout,
              attribute,
              conflictDistance,
              ConExpFX.instance.exe.tpe));

          ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Flip Finish") {

            protected final void _call() {
              updateProgress(
                  0.5d,
                  1d);
              conceptGraph.controller.graphLock.unlock();
              conceptGraph.highlightLock.unlock();
            }
          });
        }
      }
    });
    refine(1);
  }

  public final void select(final M attribute) {

    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Select Init") {

      protected final void _call() {
        updateProgress(
            0.5d,
            1d);
        conceptGraph.controller.graphLock.lock();
        conceptGraph.highlightLock.lock();
      }
    });
    ConExpFX.instance.exe.submit(IFox.select(
        id.get(),
        layout,
        attribute,
        conflictDistance,
        ConExpFX.instance.exe.tpe));

    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Select Finish") {

      protected final void _call() {
        updateProgress(
            0.5d,
            1d);
        conceptGraph.controller.graphLock.unlock();
        conceptGraph.highlightLock.unlock();
      }
    });
    refine(1);
  }

  public final void ignore(final M attribute) {

    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Ignore Init") {

      protected final void _call() {
        updateProgress(
            0.5d,
            1d);
        conceptGraph.controller.graphLock.lock();
        conceptGraph.highlightLock.lock();
      }
    });
    ConExpFX.instance.exe.submit(IFox.ignore(
        id.get(),
        layout,
        attribute,
        conflictDistance,
        ConExpFX.instance.exe.tpe));

    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Ignore Finish") {

      protected final void _call() {
        updateProgress(
            0.5d,
            1d);
        conceptGraph.controller.graphLock.unlock();
        conceptGraph.highlightLock.unlock();
      }
    });
    refine(1);
  }

  public final void relayout(final int generationCount, final int populationSize) {
    ConExpFX.instance.exe.submit(GeneticLayouter.seeds(
        id.get(),
        layout,
        false,
        generationCount,
        populationSize,
        conceptGraph.threeDimensions(),
        conceptGraph.polar(),
        conflictDistance,
        ConExpFX.instance.exe.tpe));
  }

  public final void refine(final int generationCount) {
    ConExpFX.instance.exe.submit(GeneticLayouter.seeds(
        id.get(),
        layout,
        true,
        generationCount,
        1,
        conceptGraph.threeDimensions(),
        conceptGraph.polar(),
        conflictDistance,
        ConExpFX.instance.exe.tpe));
  }

  public final LayoutEvolution<G, M> qualityChart(final Concept<G, M> concept, final ConceptMovement movement) {
    final LayoutEvolution<G, M> qualityEvolution =
        new LayoutEvolution<G, M>(
            layout,
            concept,
            movement,
            2d,
            2d,
            32,
            1,
            16,
            conflictDistance,
            ConExpFX.instance.exe.tpe);
    ConExpFX.instance.exe.submit(LayoutEvolution.calculate(qualityEvolution));
    return qualityEvolution;
  }

  public final void storeToFile() {
    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Store") {

      @SuppressWarnings("incomplete-switch")
      protected final void _call() {
        updateProgress(
            0.5d,
            1d);
        switch (FileFormat.of(
            file,
            FileFormat.CFX,
            FileFormat.CXT).second()) {
        case CXT:
          CXTExporter.export(
              context,
              contextWidget.rowHeaderPane.rowMap,
              contextWidget.colHeaderPane.columnMap,
              file);
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
        Platform2.runOnFXThread(() -> {
          id.set(file.getName());
          unsavedChanges.set(false);
          ConExpFX.instance.fileHistory.remove(file);
          ConExpFX.instance.fileHistory.add(
              0,
              file);
        });
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
    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Export") {

      @SuppressWarnings("incomplete-switch")
      protected final void _call() {
        updateProgress(
            0.5d,
            1d);
        switch (FileFormat.of(
            file,
            FileFormat.TEX,
            FileFormat.PNG,
            FileFormat.SVG,
            FileFormat.PDF,
            FileFormat.HTML).second()) {
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
    ConExpFX.instance.exe.submit(new BlockingTask(id.get() + " - Clear implications list") {

      @Override
      protected void _call() {
        implications.clear();
      }
    });
//    conExp.executor.submit(NextImplication2.implications(context, implications));
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
    fileChooser.getExtensionFilters().add(
        new ExtensionFilter("Context & Lattice (ConExpFX Format, *.cfx)", "*.cfx"));
    fileChooser.getExtensionFilters().add(
        new ExtensionFilter("Only Context (Burmeister Format, *.cxt)", "*.cxt"));
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
    final File file = fileChooser.showSaveDialog(ConExpFX.instance.primaryStage);
    if (file != null) {
      ConExpFX.instance.lastDirectory = file.getParentFile();
      exportToFile(file);
    }
  }

  @Deprecated
  public void exportTeX() {
    final Return<TeXOptions> ret = new TeXDialog<G, M>(ConExpFX.instance.primaryStage).showAndWait();
    if (ret.result().equals(
        Result.OK)) {
      final FileChooser chooser = new FileChooser();
      chooser.getExtensionFilters().add(
          new ExtensionFilter("LaTeX File (*.tex)", "*.tex"));
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
