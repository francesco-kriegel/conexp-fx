package conexp.fx.gui.context;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import org.semanticweb.owlapi.model.OWLClassExpression;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.context.MatrixContext.Incidence;
import conexp.fx.core.util.Constants;
import conexp.fx.core.util.OWLtoString;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.cellpane.Cell;
import conexp.fx.gui.cellpane.CellPane;
import conexp.fx.gui.cellpane.InteractionMode;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.graph.ConceptGraph;
import conexp.fx.gui.util.ColorScheme;
import conexp.fx.gui.util.LaTeX;
import conexp.fx.gui.util.Platform2;
import de.tudresden.inf.tcs.fcalib.Implication;

public class MatrixContextWidget<G, M> extends BorderPane {

  public final class RowHeaderPane extends CellPane<RowHeaderPane, RowHeaderCell> {

    private RowHeaderPane() {
      super("DomainPane", InteractionMode.ROWS);
      this.rowHeightDefault.bind(MatrixContextWidget.this.cellSizeDefault);
      this.columnWidthDefault.bind(MatrixContextWidget.this.rowHeaderSizeDefault);
      this.zoomFactor.bind(MatrixContextWidget.this.zoomFactor);
      this.textSizeDefault.bind(MatrixContextWidget.this.textSizeDefault);
      this.animate.bind(MatrixContextWidget.this.animate);
      this.autoSizeRows.set(false);
      this.autoSizeColumns.set(true);
      this.maxRows.set(context.rowHeads().size());
      final RelationEventHandler<G, M> eventHandler = new RelationEventHandler<G, M>() {

        public final void handle(final RelationEvent<G, M> event) {
          Platform2.runOnFXThread(new Runnable() {

            public void run() {
              maxRows.set(context.rowHeads().size());
            }
          });
        }
      };
      context.addEventHandler(
          eventHandler,
          RelationEvent.ROWS_ADDED);
      context.addEventHandler(
          eventHandler,
          RelationEvent.ROWS_REMOVED);
      this.maxColumns.set(1);
      if (tab != null)
        this.interactionPane.addEventHandler(
            MouseEvent.MOUSE_EXITED,
            new EventHandler<MouseEvent>() {

              public final void handle(final MouseEvent event) {
                tab.conceptGraph.highlight(
                    false,
                    tab.conceptGraph.highlightRequests.dehighlight());
              }
            });
    }

    protected final RowHeaderCell createCell(final int row, final int column) {
      return new RowHeaderCell(row);
    }

    public final void highlightConcept(final Collection<Integer> domainIndices) {
      highlight(
          domainIndices,
          null);
    }
  }

  public final class ColHeaderPane extends CellPane<ColHeaderPane, ColHeaderCell> {

    private ColHeaderPane() {
      super("CodomainPane", InteractionMode.COLUMNS);
      this.rowHeightDefault.bind(MatrixContextWidget.this.colHeaderSizeDefault);
      this.columnWidthDefault.bind(MatrixContextWidget.this.cellSizeDefault);
      this.zoomFactor.bind(MatrixContextWidget.this.zoomFactor);
      this.textSizeDefault.bind(MatrixContextWidget.this.textSizeDefault);
      this.animate.bind(MatrixContextWidget.this.animate);
      this.autoSizeRows.set(true);
      this.autoSizeColumns.set(false);
      this.maxRows.set(1);
      this.maxColumns.set(context.colHeads().size());
      final RelationEventHandler<G, M> eventHandler = new RelationEventHandler<G, M>() {

        public final void handle(final RelationEvent<G, M> event) {
          Platform2.runOnFXThread(new Runnable() {

            public void run() {
              maxColumns.set(context.colHeads().size());
            }
          });
        }
      };
      context.addEventHandler(
          eventHandler,
          RelationEvent.COLUMNS_ADDED);
      context.addEventHandler(
          eventHandler,
          RelationEvent.COLUMNS_REMOVED);
      if (tab != null)
        this.interactionPane.addEventHandler(
            MouseEvent.MOUSE_EXITED,
            new EventHandler<MouseEvent>() {

              public final void handle(final MouseEvent event) {
                tab.conceptGraph.highlight(
                    false,
                    tab.conceptGraph.highlightRequests.dehighlight());
              }
            });
    }

    protected final ColHeaderCell createCell(final int row, final int column) {
      return new ColHeaderCell(column);
    }

    public final void highlightConcept(final Collection<Integer> codomainIndices) {
      highlight(
          null,
          codomainIndices);
    }
  }

  private final class ContextPane extends CellPane<ContextPane, ContextCell> {

    private ContextPane() {
      super("FormalContextPane", InteractionMode.ROWS_AND_COLUMNS);
      this.textSizeDefault.bind(MatrixContextWidget.this.incidenceSizeDefault);
      this.zoomFactor.bind(MatrixContextWidget.this.zoomFactor);
      this.bind(
          rowHeaderPane,
          InteractionMode.ROWS);
      this.bind(
          colHeaderPane,
          InteractionMode.COLUMNS);
      if (tab != null)
        this.rowMap.addListener(new MapChangeListener<Integer, Integer>() {

          public final void onChanged(
              final javafx.collections.MapChangeListener.Change<? extends Integer, ? extends Integer> change) {
            tab.unsavedChanges.set(true);
          }
        });
      if (tab != null)
        this.columnMap.addListener(new MapChangeListener<Integer, Integer>() {

          public final void onChanged(
              final javafx.collections.MapChangeListener.Change<? extends Integer, ? extends Integer> change) {
            tab.unsavedChanges.set(true);
          }
        });
      if (tab != null)
        this.interactionPane.addEventHandler(
            MouseEvent.MOUSE_EXITED,
            new EventHandler<MouseEvent>() {

              public final void handle(final MouseEvent event) {
                tab.conceptGraph.highlight(
                    false,
                    tab.conceptGraph.highlightRequests.dehighlight());
              }
            });
    }

    protected final ContextCell createCell(final int row, final int column) {
      return new ContextCell(row, column);
    }
  }

  private final class RowHeaderCell extends Cell<RowHeaderCell, RowHeaderPane> {

    private ImageView view;

    private RowHeaderCell(final int row) {
      super(rowHeaderPane, row, 0, Pos.CENTER_RIGHT, TextAlignment.RIGHT, false, null);
      if (view == null) {
        view = ImageViewBuilder.create().build();
        this.contentPane.get().getChildren().add(
            LabelBuilder.create().graphic(
                view).build());
        this.contentPane.get().text.setOpacity(0);
      }
      this.dehighlightColor = Color.WHITE;
      this.contentPane.get().background.setFill(dehighlightColor);
      this.interactionPane.get().addEventHandler(
          MouseEvent.MOUSE_CLICKED,
          new EventHandler<MouseEvent>() {

            @SuppressWarnings("incomplete-switch")
            public final void handle(final MouseEvent event) {
              switch (event.getButton()) {
              case PRIMARY:
//                if (rowHeaderPane.rowOpacityMap.containsKey(contentCoordinates.get().x())) {
//                  rowHeaderPane.rowOpacityMap.remove(contentCoordinates.get().x());
////                final G object = context.getDomain().get(contentCoordinates.get().x());
////                context.selectObject(object);
//                } else {
//                  rowHeaderPane.rowOpacityMap.put(contentCoordinates.get().x(), Constants.HIDE_OPACITY);
////                final G object = context.getDomain().get(contentCoordinates.get().x());
////                context.deselectObject(object);
//                }
                break;
              case SECONDARY:
                if (MatrixContextWidget.this instanceof StringMatrixContextWidget) {
                  System.out.println("edit mode");
                  final G object = context.rowHeads().get(
                      contentCoordinates.get().x());
                  final TextField textField = TextFieldBuilder.create().text(
                      (String) object).build();
                  textField.addEventHandler(
                      KeyEvent.KEY_RELEASED,
                      new EventHandler<KeyEvent>() {

                        @SuppressWarnings({ "unchecked" })
                        public final void handle(final KeyEvent event) {
                          switch (event.getCode()) {
                          case ENTER:
//                    cache.clearObject(contentCoordinates.get().x());
                            context.rowHeads().set(
                                object,
                                (G) textField.getText().trim());
                            if (tab != null)
                              tab.unsavedChanges.set(true);
                          case ESCAPE:
                            interactionPane.get().getChildren().remove(
                                textField);
                          }
                        };
                      });
                  interactionPane.get().getChildren().add(
                      textField);
                  textField.focusedProperty().addListener(
                      new ChangeListener<Boolean>() {

                        public final void changed(
                            final ObservableValue<? extends Boolean> observable,
                            final Boolean oldValue,
                            final Boolean newValue) {
                          new Timer().schedule(
                              new TimerTask() {

                                public final void run() {
                                  Platform.runLater(new Runnable() {

                                    public final void run() {
                                      textField.selectAll();
                                    }
                                  });
                                }
                              },
                              20);
                        }
                      });
                  textField.requestFocus();
                } else {
                  System.out.println("no instance of MatrixContextWidget");
                }
              }
            }
          });
      if (tab != null)
        this.interactionPane.get().addEventHandler(
            MouseEvent.MOUSE_ENTERED,
            new EventHandler<MouseEvent>() {

              public final void handle(final MouseEvent event) {
                if (highlight.get())
                  tab.conceptGraph.highlight(
                      true,
                      tab.conceptGraph.highlightRequests.object(context.rowHeads().get(
                          contentCoordinates.get().x())));
              }
            });
      context.addEventHandler(
          new RelationEventHandler<G, M>() {

            public final void handle(final RelationEvent<G, M> event) {
              updateContent();
            }
          },
          RelationEvent.ROWS_SET);
    }

    public final void updateContent() {
      final String string = context.rowHeads().get(
          contentCoordinates.get().x()).toString();
      textContent.set(string);
      try {
        if (view == null) {
          view = ImageViewBuilder.create().build();
          this.contentPane.get().getChildren().add(
              LabelBuilder.create().graphic(
                  view).build());
          this.contentPane.get().text.setOpacity(0);
        }
        view.setImage(LaTeX.toFXImage(
            string,
            (float) (16d * zoomFactor.get())));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private final class ColHeaderCell extends Cell<ColHeaderCell, ColHeaderPane> {

    private ImageView view;

    private ColHeaderCell(final int column) {
      super(colHeaderPane, 0, column, Pos.CENTER_LEFT, TextAlignment.LEFT, true, null);
      if (view == null) {
        view = ImageViewBuilder.create().build();
        this.contentPane.get().getChildren().add(
            LabelBuilder.create().graphic(
                view).build());
        this.contentPane.get().text.setOpacity(0);
      }
      this.dehighlightColor = Color.WHITE;
      this.contentPane.get().background.setFill(dehighlightColor);
//      if (!tab.fca.context.selectedAttributes().contains(tab.fca.context.colHeads().get(contentCoordinates.get().y())))
//        colHeaderPane.columnOpacityMap.put(contentCoordinates.get().y(), Constants.HIDE_OPACITY);
      this.interactionPane.get().addEventHandler(
          MouseEvent.MOUSE_CLICKED,
          new EventHandler<MouseEvent>() {

            @SuppressWarnings("incomplete-switch")
            public final synchronized void handle(final MouseEvent event) {
              switch (event.getButton()) {
              case PRIMARY:
                synchronized (colHeaderPane.columnOpacityMap) {
                  if (colHeaderPane.columnOpacityMap.containsKey(contentCoordinates.get().y())) {
                    if (tab != null)
                      tab.conceptGraph.highlight(
                          false,
                          tab.conceptGraph.highlightRequests.dehighlight());
                    colHeaderPane.columnOpacityMap.remove(contentCoordinates.get().y());
                    if (tab != null)
                      tab.select(context.colHeads().get(
                          contentCoordinates.get().y()));
                  } else {
                    if (tab != null)
                      tab.conceptGraph.highlight(
                          false,
                          tab.conceptGraph.highlightRequests.dehighlight());
                    colHeaderPane.columnOpacityMap.put(
                        contentCoordinates.get().y(),
                        Constants.HIDE_OPACITY);
                    if (tab != null)
                      tab.ignore(context.colHeads().get(
                          contentCoordinates.get().y()));
                  }
                }
                break;
              case SECONDARY:
                if (MatrixContextWidget.this instanceof StringMatrixContextWidget) {
                  final M attribute = context.colHeads().get(
                      contentCoordinates.get().y());
                  final TextField textField = TextFieldBuilder.create().text(
                      (String) attribute).build();
                  textField.addEventHandler(
                      KeyEvent.KEY_RELEASED,
                      new EventHandler<KeyEvent>() {

                        @SuppressWarnings({ "unchecked" })
                        public final void handle(final KeyEvent event) {
                          switch (event.getCode()) {
                          case ENTER:
                            context.colHeads().set(
                                attribute,
                                (M) textField.getText().trim());
                            if (tab != null)
                              tab.unsavedChanges.set(true);
                          case ESCAPE:
                            interactionPane.get().getChildren().remove(
                                textField);
                          }
                        };
                      });
                  textField.rotateProperty().set(
                      -90);
                  textField.setMinSize(
                      colHeaderPane.rowHeight.get(),
                      cellSize.get());
                  textField.setMaxSize(
                      colHeaderPane.rowHeight.get(),
                      cellSize.get());
                  interactionPane.get().getChildren().add(
                      textField);
                  textField.focusedProperty().addListener(
                      new ChangeListener<Boolean>() {

                        public final void changed(
                            final ObservableValue<? extends Boolean> observable,
                            final Boolean oldValue,
                            final Boolean newValue) {
                          new Timer().schedule(
                              new TimerTask() {

                                public final void run() {
                                  Platform.runLater(new Runnable() {

                                    public final void run() {
                                      textField.selectAll();
                                    }
                                  });
                                }
                              },
                              20);
                        }
                      });
                  textField.requestFocus();
                }
              }
            }
          });
      if (tab != null)
        this.interactionPane.get().addEventHandler(
            MouseEvent.MOUSE_ENTERED,
            new EventHandler<MouseEvent>() {

              public final void handle(final MouseEvent event) {
                if (highlight.get()) {
                  final M m = context.colHeads().get(
                      contentCoordinates.get().y());
                  if (context.selectedAttributes().contains(
                      m))
                    tab.conceptGraph.highlight(
                        true,
                        tab.conceptGraph.highlightRequests.attribute(m));
                }
              }
            });
      context.addEventHandler(
          new RelationEventHandler<G, M>() {

            public final void handle(final RelationEvent<G, M> event) {
              updateContent();
            }
          },
          RelationEvent.COLUMNS_SET);
    }

    public final void updateContent() {
      final M m = context.colHeads().get(
          contentCoordinates.get().y());
      // TODO: The type check for OWLClassExpression is currently just a workaround.
      final String string =
          m instanceof OWLClassExpression ? OWLtoString.toString((OWLClassExpression) m) : m.toString();
      textContent.set(string);
      try {
        if (view == null) {
          view = ImageViewBuilder.create().build();
          this.contentPane.get().getChildren().add(
              LabelBuilder.create().graphic(
                  view).build());
          this.contentPane.get().text.setOpacity(0);
        }
        view.setImage(LaTeX.toFXImage(
            string,
            (float) (16d * zoomFactor.get())));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private final class ContextCell extends Cell<ContextCell, ContextPane> {

    private ContextCell(final int row, final int column) {
      super(contextPane, row, column, Pos.CENTER, TextAlignment.CENTER, false, null);
      if (tab != null)
        this.interactionPane.get().addEventHandler(
            MouseEvent.MOUSE_CLICKED,
            new EventHandler<MouseEvent>() {

              public final void handle(final MouseEvent event) {
                final G g = context.rowHeads().get(
                    contentCoordinates.get().x());
                final M m = context.colHeads().get(
                    contentCoordinates.get().y());
                tab.conceptGraph.highlight(
                    false,
                    tab.conceptGraph.highlightRequests.dehighlight());
                tab.flip(
                    g,
                    m);
                tab.unsavedChanges.set(true);
              }
            });
      else
        this.interactionPane.get().addEventHandler(
            MouseEvent.MOUSE_CLICKED,
            event -> {
              final G g = context.rowHeads().get(
                  contentCoordinates.get().x());
              final M m = context.colHeads().get(
                  contentCoordinates.get().y());
              if (context.contains(
                  g,
                  m))
                context.remove(
                    g,
                    m);
              else
                context.add(
                    g,
                    m);
            });
      if (tab != null)
        this.interactionPane.get().addEventHandler(
            MouseEvent.MOUSE_ENTERED,
            new EventHandler<MouseEvent>() {

              public final void handle(final MouseEvent event) {
                if (highlight.get()) {
                  final G g = context.rowHeads().get(
                      contentCoordinates.get().x());
                  final M m = context.colHeads().get(
                      contentCoordinates.get().y());
                  if (context.selectedAttributes().contains(
                      m))
                    if (textContent.get().equals(
                        Constants.CROSS_CHARACTER))
                      tab.conceptGraph.highlight(
                          true,
                          tab.conceptGraph.highlightRequests.incidence(
                              g,
                              m));
                    else if (textContent.get().equals(
                        Constants.DOWN_ARROW_CHARACTER))
                      tab.conceptGraph.highlight(
                          true,
                          tab.conceptGraph.highlightRequests.downArrow(
                              g,
                              m));
                    else if (textContent.get().equals(
                        Constants.UP_ARROW_CHARACTER))
                      tab.conceptGraph.highlight(
                          true,
                          tab.conceptGraph.highlightRequests.upArrow(
                              g,
                              m));
                    else if (textContent.get().equals(
                        Constants.BOTH_ARROW_CHARACTER))
                      tab.conceptGraph.highlight(
                          true,
                          tab.conceptGraph.highlightRequests.bothArrow(
                              g,
                              m));
                    else if (textContent.get().equals(
                        Constants.NO_CROSS_CHARACTER))
                      tab.conceptGraph.highlight(
                          true,
                          tab.conceptGraph.highlightRequests.nonIncidence(
                              g,
                              m));
                }
              }
            });
      context.addEventHandler(
          new RelationEventHandler<G, M>() {

            public final void handle(final RelationEvent<G, M> event) {
              updateContent();
            }
          },
          RelationEvent.ENTRIES_ADDED,
          RelationEvent.ENTRIES_REMOVED,
          RelationEvent.ALL_CHANGED,
          RelationEvent.SELECTION_CHANGED);
    }

    @SuppressWarnings("incomplete-switch")
    public final void updateContent() {
      final G g = context.rowHeads().get(
          contentCoordinates.get().x());
      final M m = context.colHeads().get(
          contentCoordinates.get().y());
      if (context.selectedAttributes().contains(
          m)) {
        final Pair<Incidence, Incidence> p = context.selection.getValue(
            g,
            m,
            showArrows.get(),
            showPaths.get());
        final Incidence first = p.first();
        final Incidence second = p.second();
        ContextCell.this.textContent.set(second != null && first == Incidence.NO_CROSS
            ? Constants.NO_CROSS_CHARACTER_BOLD : first.toString());
        switch (first) {
        case BOTH_ARROW:
          ContextCell.this.contentPane.get().text.setRotate(-45d);
          break;
        case DOWN_ARROW:
        case UP_ARROW:
        case CROSS:
        case NO_CROSS:
          ContextCell.this.contentPane.get().text.setRotate(0d);
          break;
        }
        if (second == null)
          ContextCell.this.contentPane.get().text.setFill(Color.BLACK);
        else
          switch (second) {
          case BOTH_PATH:
            ContextCell.this.contentPane.get().text.setFill(ConceptGraph.COLOR_INTERVAL);
            break;
          case DOWN_PATH:
            ContextCell.this.contentPane.get().text.setFill(ConceptGraph.COLOR_LOWER);
            break;
          case UP_PATH:
            ContextCell.this.contentPane.get().text.setFill(ConceptGraph.COLOR_UPPER);
            break;
          }
      } else {
        ContextCell.this.textContent.set(context.getValue(
            g,
            m,
            false).first().toString());
        ContextCell.this.contentPane.get().text.setRotate(0d);
      }
    }
  }

  protected final FCADataset<G, M>    tab;
  protected final MatrixContext<G, M> context;
  protected final GridPane            centerPane            = new GridPane();
  public final RowHeaderPane          rowHeaderPane;
  public final ColHeaderPane          colHeaderPane;
  public final ContextPane            contextPane;
  protected final ScrollBar           rowScrollBar;
  protected final ScrollBar           colScrollBar;
//  public final ListSpinner<Integer> zoomSpinner =new ListSpinner<Integer>(-4, 4, 1);
  public final Slider                 zoomSlider            = SliderBuilder.create().min(
                                                                -4d).max(
                                                                4d).value(
                                                                0d)
//                                                                .showTickMarks(true)
//                                                                .minorTickCount(17)
//                                                                .majorTickUnit(1d)
//                                                                .snapToTicks(true)
                                                                .blockIncrement(
                                                                    0.25d)
                                                                .build();
  public final DoubleProperty         zoomFactor            = new SimpleDoubleProperty(0.01d);
  public final IntegerProperty        rowHeaderSizeDefault  = new SimpleIntegerProperty(40);
  public final IntegerProperty        colHeaderSizeDefault  = new SimpleIntegerProperty(40);
  public final IntegerProperty        cellSizeDefault       = new SimpleIntegerProperty(20);
  public final IntegerProperty        textSizeDefault       = new SimpleIntegerProperty(16);
  public final IntegerProperty        incidenceSizeDefault  = new SimpleIntegerProperty(20);
  public final IntegerBinding         cellSize              = new IntegerBinding() {

                                                              {
                                                                super.bind(
                                                                    zoomFactor,
                                                                    cellSizeDefault);
                                                              }

                                                              protected int computeValue() {
                                                                return (int) (zoomFactor.get() * cellSizeDefault
                                                                    .doubleValue());
                                                              };
                                                            };
  public final IntegerBinding         textSize              = new IntegerBinding() {

                                                              {
                                                                super.bind(
                                                                    zoomFactor,
                                                                    textSizeDefault);
                                                              }

                                                              protected int computeValue() {
                                                                return (int) (zoomFactor.get() * textSizeDefault
                                                                    .doubleValue());
                                                              };
                                                            };
  public final IntegerBinding         incidenceSize         = new IntegerBinding() {

                                                              {
                                                                super.bind(
                                                                    zoomFactor,
                                                                    textSizeDefault);
                                                              }

                                                              protected int computeValue() {
                                                                return (int) (zoomFactor.get() * incidenceSizeDefault
                                                                    .doubleValue());
                                                              };
                                                            };
  public final BooleanProperty        animate               = new SimpleBooleanProperty(false);
  public final BooleanProperty        showArrows            = new SimpleBooleanProperty();
  public final BooleanProperty        showPaths             = new SimpleBooleanProperty();
  public final ToggleButton           highlightToggleButton = new ToggleButton();
  public final DoubleBinding          height;

  public MatrixContextWidget(final FCADataset<G, M> fcaInstance) {
    this(fcaInstance, true);
  }

  /**
   * @param fcaInstance
   * @param withToolbar
   * @param orContext
   *          may only be set if fcaInstance is null, otherwise unexpected behaviour may occur.
   */
  public MatrixContextWidget(
      final FCADataset<G, M> fcaInstance,
      final boolean withToolbar,
      final MatrixContext<G, M>... orContext) {
    super();
    this.tab = fcaInstance;
    if (fcaInstance == null && orContext[0] != null)
      this.context = orContext[0];
    else
      this.context = fcaInstance.context;
    this.rowHeaderPane = new RowHeaderPane();
    this.colHeaderPane = new ColHeaderPane();
    this.contextPane = new ContextPane();
    this.rowScrollBar = contextPane.getRowScrollBar();
    this.colScrollBar = contextPane.getColumnScrollBar();
    centerPane.setHgap(4);
    centerPane.setVgap(4);
    rowHeaderPane.colorScheme.setValue(ColorScheme.JAVA_FX);
    colHeaderPane.colorScheme.setValue(ColorScheme.JAVA_FX);
    contextPane.colorScheme.setValue(ColorScheme.JAVA_FX);
    final RowConstraints firstRowConstraints = new RowConstraints();
    firstRowConstraints.minHeightProperty().bind(
        colHeaderPane.rowHeight);
    firstRowConstraints.maxHeightProperty().bind(
        colHeaderPane.rowHeight);
    final RowConstraints secondRowConstraints = new RowConstraints();
    secondRowConstraints.minHeightProperty().bind(
        cellSize);
    secondRowConstraints.prefHeightProperty().bind(
        new IntegerBinding() {

          {
            super.bind(
                rowHeaderPane.maxRows,
                cellSize);
          }

          protected final int computeValue() {
            return context.rowHeads().size() * cellSize.get();
          }
        });
    final RowConstraints thirdRowConstraints = new RowConstraints();
    thirdRowConstraints.minHeightProperty().bind(
        cellSize);
    thirdRowConstraints.maxHeightProperty().bind(
        cellSize);
    centerPane.getRowConstraints().addAll(
        firstRowConstraints,
        secondRowConstraints,
        thirdRowConstraints);
    final ColumnConstraints firstColumnConstraints = new ColumnConstraints();
    firstColumnConstraints.minWidthProperty().bind(
        rowHeaderPane.columnWidth);
    firstColumnConstraints.maxWidthProperty().bind(
        rowHeaderPane.columnWidth);
    final ColumnConstraints secondColumnConstraints = new ColumnConstraints();
    secondColumnConstraints.minWidthProperty().bind(
        cellSize);
    secondColumnConstraints.maxWidthProperty().bind(
        new IntegerBinding() {

          {
            super.bind(
                colHeaderPane.maxColumns,
                cellSize);
          }

          protected final int computeValue() {
            return context.colHeads().size() * cellSize.get();
          }
        });
    final ColumnConstraints thirdColumnConstraints = new ColumnConstraints();
    thirdColumnConstraints.minWidthProperty().bind(
        cellSize);
    thirdColumnConstraints.maxWidthProperty().bind(
        cellSize);
    centerPane.getColumnConstraints().addAll(
        firstColumnConstraints,
        secondColumnConstraints,
        thirdColumnConstraints);
    centerPane.add(
        contextPane.getContentAndInteractionStackPane(),
        1,
        1);
    centerPane.add(
        rowHeaderPane.getContentAndInteractionStackPane(),
        0,
        1);
    centerPane.add(
        colHeaderPane.getContentAndInteractionStackPane(),
        1,
        0);
    centerPane.add(
        rowScrollBar,
        2,
        1);
    centerPane.add(
        colScrollBar,
        1,
        2);
    this.setCenter(centerPane);
    if (withToolbar)
      createToolBar();
    else
      zoomFactor.set(Math.pow(
          2d,
          0));
    final ChangeListener<Boolean> updateContentListener = new ChangeListener<Boolean>() {

      public final void changed(
          final ObservableValue<? extends Boolean> observable,
          final Boolean oldValue,
          final Boolean newValue) {
        contextPane.updateContent();
      }
    };
    showArrows.addListener(updateContentListener);
    showPaths.addListener(updateContentListener);
    height = new DoubleBinding() {

      {
        bind(
            colHeaderPane.heightProperty(),
            contextPane.heightProperty(),
            colHeaderPane.rowHeight,
            colHeaderPane.visibleRows,
            contextPane.rowHeight,
            contextPane.visibleRows);
      }

      @Override
      protected double computeValue() {
//        System.out.println(colHeaderPane.rowHeight.get());
//        System.out.println(colHeaderPane.visibleRows.get());
//        System.out.println(contextPane.rowHeight.get());
//        System.out.println(contextPane.visibleRows.get());
        final int h =
            colHeaderPane.rowHeight.get() * colHeaderPane.visibleRows.get() + contextPane.rowHeight.get()
                * contextPane.visibleRows.get();
//        System.out.println(h);
        return h;
      }
    };
    rowHeaderPane.toFront();
    colHeaderPane.toFront();
    final Timeline t = new Timeline();
    t.getKeyFrames().add(
        new KeyFrame(Duration.millis(1000),
//        new EventHandler<ActionEvent>() {
//
//      public final void handle(final ActionEvent event) {
//        final Timeline s = new Timeline();
//        s.getKeyFrames().add(
//            new KeyFrame(Duration.millis(1000), new KeyValue(zoomSlider.valueProperty(), 0.9d, Interpolator.EASE_OUT)));
//        Platform.runLater(new Runnable() {
//
//          
//          public final void run() {
//            s.play();
//          }
//        });
//      }
//    },
            new KeyValue(zoomSlider.valueProperty(), 0d, Interpolator.EASE_IN)));
    Platform.runLater(new Runnable() {

      public void run() {
        t.play();
      }
    });
  }

  private final void createToolBar() {
    zoomFactor.bind(new DoubleBinding() {

      {
        bind(zoomSlider.valueProperty());
      }

      protected double computeValue() {
        return Math.pow(
            2d,
            zoomSlider.valueProperty().get());
      }
    });
    final ToggleButton arrowsToggleButton =
        new ToggleButton(Constants.DOWN_ARROW_CHARACTER + Constants.UP_ARROW_CHARACTER);
    arrowsToggleButton.setSelected(false);
    arrowsToggleButton.setMinHeight(24);
    showArrows.bind(arrowsToggleButton.selectedProperty());
    final ToggleButton pathsToggleButton =
        new ToggleButton(Constants.DOWN_ARROW_CHARACTER + Constants.DOWN_ARROW_CHARACTER);
    pathsToggleButton.setDisable(true);
    pathsToggleButton.setSelected(false);
    pathsToggleButton.setMinHeight(24);
    showPaths.bind(pathsToggleButton.selectedProperty());
    arrowsToggleButton.addEventHandler(
        ActionEvent.ACTION,
        new EventHandler<ActionEvent>() {

          @Override
          public final void handle(final ActionEvent event) {
            if (showArrows.get()) {
              pathsToggleButton.setDisable(false);
            } else {
              pathsToggleButton.setSelected(false);
              pathsToggleButton.setDisable(true);
            }
          }
        });
    highlightToggleButton.setSelected(false);
    highlightToggleButton.setMinHeight(24);
    highlightToggleButton.setGraphic(ImageViewBuilder.create().image(
        new Image(ConExpFX.class.getResourceAsStream("image/16x16/flag.png"))).build());
    rowHeaderPane.highlight.bind(highlightToggleButton.selectedProperty());
    colHeaderPane.highlight.bind(highlightToggleButton.selectedProperty());
    contextPane.highlight.bind(highlightToggleButton.selectedProperty());
    arrowsToggleButton.setStyle("-fx-background-radius: 5 0 0 5, 5 0 0 5, 4 0 0 4, 3 0 0 3;");
    pathsToggleButton.setStyle("-fx-background-radius: 0, 0, 0, 0");
    highlightToggleButton.setStyle("-fx-background-radius: 0 5 5 0, 0 5 5 0, 0 4 4 0, 0 3 3 0;");
    HBox showBox = new HBox();
    showBox.setPadding(new Insets(0d));
    showBox.getChildren().addAll(
        arrowsToggleButton,
        pathsToggleButton,
        highlightToggleButton);
    final ToolBar toolBar = new ToolBar();
    toolBar.getItems().addAll(
        zoomSlider,
        showBox);
    this.setTop(toolBar);
    toolBar.toFront();
  }

  protected final void update() {
    rowHeaderPane.updateContent();
    colHeaderPane.updateContent();
    contextPane.updateContent();
  }

  public final void dehighlight() {
    rowHeaderPane.dehighlight();
    colHeaderPane.dehighlight();
    contextPane.dehighlight();
    contextPane.highlightConcept.set(false);
  }

  public final void highlight(final Concept<G, M> concept) {
    final Collection<Integer> domainIndices = context.rowHeads().indicesOf(
        concept.extent(),
        true);
    final Collection<Integer> codomainIndices = context.colHeads().indicesOf(
        concept.intent(),
        true);
    contextPane.highlightConcept.set(true);
    rowHeaderPane.highlightConcept(Collections2.transform(
        domainIndices,
        new Function<Integer, Integer>() {

          public final Integer apply(final Integer index) {
            for (Entry<Integer, Integer> entry : contextPane.rowMap.get().entrySet())
              if (entry.getValue().equals(
                  index))
                return entry.getKey();
            return index;
          }
        }));
    colHeaderPane.highlightConcept(Collections2.transform(
        codomainIndices,
        new Function<Integer, Integer>() {

          public final Integer apply(final Integer index) {
            for (Entry<Integer, Integer> entry : contextPane.columnMap.get().entrySet())
              if (entry.getValue().equals(
                  index))
                return entry.getKey();
            return index;
          }
        }));
  }

  public final void highlightImplication(final Implication<M> implication) {

  }
}
