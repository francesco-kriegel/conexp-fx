package conexp.fx.gui.graph;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.StrokeTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;
import jfxtras.scene.control.ListSpinner;

import org.semanticweb.owlapi.model.OWLClassExpression;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;
import conexp.fx.core.context.Concept;
import conexp.fx.core.layout.ConceptMovement;
import conexp.fx.core.math.Points;
import conexp.fx.core.quality.LayoutEvolution;
import conexp.fx.core.util.Constants;
import conexp.fx.core.util.OWLtoString;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.graph.option.AnimationSpeed;
import conexp.fx.gui.graph.option.AttributeLabelText;
import conexp.fx.gui.graph.option.EdgeHighlight;
import conexp.fx.gui.graph.option.EdgeStroke;
import conexp.fx.gui.graph.option.GraphTransformation;
import conexp.fx.gui.graph.option.ObjectLabelText;
import conexp.fx.gui.graph.option.VertexHighlight;
import conexp.fx.gui.graph.option.VertexRadius;
import conexp.fx.gui.lock.ALock;
import conexp.fx.gui.util.FXControls;
import conexp.fx.gui.util.NumberPropertyTransition;
import conexp.fx.gui.util.SearchBox;
import conexp.fx.gui.util.TransitionTimer;
import de.tudresden.inf.tcs.fcalib.Implication;

public final class ConceptGraph<G, M> extends Graph<Concept<G, M>, Circle> {

  public static final Color COLOR_CONCEPT      = Color.valueOf("#FFE206");
  public static final Color COLOR_INTERVAL     = Color.valueOf("#8100BC");
  public static final Color COLOR_LOWER        = Color.valueOf("#1EA266");
  public static final Color COLOR_UPPER        = Color.valueOf("#DD3558");
  public static final Color COLOR_UNCOMPARABLE = Color.valueOf("#EEEEEE");

  protected final class ConceptVertex extends Graph<Concept<G, M>, Circle>.Vertex {

    private final class DragHandler implements EventHandler<MouseEvent> {

      private DragHandler() {}

      private double          startX;
      private double          startY;
      private ConceptMovement movement;

      public final void handle(final MouseEvent event) {
        event.consume();
        if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED))
          dragStart(event);
        else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED))
          dragDone(event);
        else
          drag(event);
      }

      private final void dragStart(final MouseEvent event) {
        controller.drag();
        startX = event.getX();
        startY = event.getY();
        movement =
            event.getButton().equals(MouseButton.PRIMARY) ? ConceptMovement.INTENT_CHAIN_SEEDS
                : ConceptMovement.LABEL_CHAIN_SEEDS;
        showQualityChart();
      }

      private final void drag(final MouseEvent event) {
        fca.layout.move(element, movement, Points.minus(getTarget(event), position.getValue()));
      }

      private final void dragDone(final MouseEvent event) {
        disposeQualityChart();
        controller.dragDone();
        fca.layout.invalidate();
      }

      private final void showQualityChart() {
        if (!controlBox.conflictChart.selectedProperty().get())
          return;
        qualityEvolution = fca.qualityChart(element, movement);
        qualityChartListener = new SetChangeListener<LayoutEvolution<G, M>.Value>() {

          public void onChanged(final SetChangeListener.Change<? extends LayoutEvolution<G, M>.Value> change) {
            if (change.wasAdded())
              new Tile(change.getElementAdded(), node.translateXProperty().get(), node.translateYProperty().get());
          }
        };
        qualityEvolution.values.addListener(qualityChartListener);
      }

      private final void disposeQualityChart() {
        if (qualityEvolution == null)
          return;
        qualityEvolution.values.removeListener(qualityChartListener);
        qualityEvolution = null;
        controller.clearBack();
      }

      private final Point3D getTarget(final MouseEvent event) {
        final Point2D p;
        if (event.getSource().equals(node))
          p = node.localToParent(event.getX(), event.getY());
        else {
          final StackPane content = (StackPane) event.getSource();
          final Point2D nodeP = node.localToParent(0, 0);
          final Point2D contentP = content.localToParent(startX, startY);
          final double dx = nodeP.getX() - contentP.getX();
          final double dy = nodeP.getY() - contentP.getY();
          p = Points.plus(content.localToParent(event.getX(), event.getY()), dx, dy);
        }
        return controller.config.get().toContent(p);
      }
    }

    private final class HighlightHandler implements EventHandler<MouseEvent> {

      private HighlightHandler() {}

      public final void handle(final MouseEvent event) {
        event.consume();
        if (!toolBar.highlight.isSelected() || controller.isDragging())
          return;
        if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED))
          highlight();
        else {
          fca.contextWidget.dehighlight();
          ConceptGraph.this.highlight(false, highlightRequests.dehighlight());
        }
      }
    }

    private final EventHandler<MouseEvent>                 dragHandler      = new DragHandler();
    private final EventHandler<MouseEvent>                 highlightHandler = new HighlightHandler();
    private SetChangeListener<LayoutEvolution<G, M>.Value> qualityChartListener;

    @SuppressWarnings("incomplete-switch")
    private ConceptVertex(final Concept<G, M> concept, final Double layoutX, final Double layoutY) {
      super(concept, new Circle(), fca.layout.positionBinding(concept), layoutX, layoutY);
      init();
      node.setFill(COLOR_DEFAULT);
      node.setStrokeType(StrokeType.OUTSIDE);
      node.setStroke(Color.BLACK);
      node.setStrokeWidth(1d);
      node.addEventHandler(MouseEvent.MOUSE_PRESSED, dragHandler);
      node.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
      node.addEventHandler(MouseEvent.MOUSE_RELEASED, dragHandler);
      node.addEventHandler(MouseEvent.MOUSE_ENTERED, highlightHandler);
      node.addEventHandler(MouseEvent.MOUSE_EXITED, highlightHandler);
      upperLabels.addListener(new ListChangeListener<UpperLabel>() {

        public void onChanged(ListChangeListener.Change<? extends UpperLabel> change) {
          while (change.next())
            if (change.wasRemoved())
              for (UpperLabel l : change.getRemoved()) {
                l.content.removeEventHandler(MouseEvent.MOUSE_PRESSED, dragHandler);
                l.content.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
                l.content.removeEventHandler(MouseEvent.MOUSE_RELEASED, dragHandler);
                l.content.removeEventHandler(MouseEvent.MOUSE_ENTERED, highlightHandler);
                l.content.removeEventHandler(MouseEvent.MOUSE_EXITED, highlightHandler);
                l.shift.unbind();
              }
            else if (change.wasAdded())
              for (UpperLabel l : change.getAddedSubList()) {
                l.content.addEventHandler(MouseEvent.MOUSE_PRESSED, dragHandler);
                l.content.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
                l.content.addEventHandler(MouseEvent.MOUSE_RELEASED, dragHandler);
                l.content.addEventHandler(MouseEvent.MOUSE_ENTERED, highlightHandler);
                l.content.addEventHandler(MouseEvent.MOUSE_EXITED, highlightHandler);
                l.shift.bind(new ObjectBinding<Point2D>() {

                  {
                    bind(node.radiusProperty());
                  }

                  protected Point2D computeValue() {
                    return new Point2D(0, -node.radiusProperty().get() - 1d);
                  }
                });
              }
        }
      });
      lowerLabels.addListener(new ListChangeListener<LowerLabel>() {

        public void onChanged(ListChangeListener.Change<? extends LowerLabel> change) {
          while (change.next())
            if (change.wasRemoved())
              for (LowerLabel l : change.getRemoved()) {
                l.content.removeEventHandler(MouseEvent.MOUSE_PRESSED, dragHandler);
                l.content.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
                l.content.removeEventHandler(MouseEvent.MOUSE_RELEASED, dragHandler);
                l.content.removeEventHandler(MouseEvent.MOUSE_ENTERED, highlightHandler);
                l.content.removeEventHandler(MouseEvent.MOUSE_EXITED, highlightHandler);
                l.shift.unbind();
              }
            else if (change.wasAdded())
              for (LowerLabel l : change.getAddedSubList()) {
                l.content.addEventHandler(MouseEvent.MOUSE_PRESSED, dragHandler);
                l.content.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
                l.content.addEventHandler(MouseEvent.MOUSE_RELEASED, dragHandler);
                l.content.addEventHandler(MouseEvent.MOUSE_ENTERED, highlightHandler);
                l.content.addEventHandler(MouseEvent.MOUSE_EXITED, highlightHandler);
                l.shift.bind(new ObjectBinding<Point2D>() {

                  {
                    bind(node.radiusProperty());
                  }

                  protected Point2D computeValue() {
                    return new Point2D(0, node.radiusProperty().get() + 1d);
                  }
                });
              }
        }
      });
      switch (controlBox.objectLabelText.getSelectionModel().selectedItemProperty().get()) {
      case EXTENT_SIZE:
      case EXTENT_PERCENTAGE:
      case OBJECT_LABELS_SIZE:
      case OBJECT_LABELS_PERCENTAGE:
        new ObjectLabel(concept);
      }
      switch (controlBox.attributeLabelText.getSelectionModel().selectedItemProperty().get()) {
      case INTENT_SIZE:
      case INTENT_PERCENTAGE:
      case ATTRIBUTE_LABELS_SIZE:
      case ATTRIBUTE_LABELS_PERCENTAGE:
        new AttributeLabel(concept);
      }
    }

    protected final void init() {
      node.radiusProperty().bind(new DoubleBinding() {

        {
          bind(element.intent(), controlBox.vertexRadius.getSelectionModel().selectedItemProperty());
        }

        public final double computeValue() {
          return controlBox.vertexRadius
              .getSelectionModel()
              .selectedItemProperty()
              .get()
              .get(fca.context, fca.lattice, element);
        }
      });
    }

    @SuppressWarnings("incomplete-switch")
    private final void highlight() {
//      node.toFront();
//      for (AttributeLabel l : upperLabels)
//        l.content.toFront();
//      for (ObjectLabel l : lowerLabels)
//        l.content.toFront();
      final VertexHighlight vertexHighlightOption =
          controlBox.vertexHighlight.getSelectionModel().selectedItemProperty().get();
      if (vertexHighlightOption.equals(VertexHighlight.NONE))
        return;
      fca.contextWidget.highlight(element);
      switch (vertexHighlightOption) {
      case CONCEPT:
        ConceptGraph.this.highlight(true, highlightRequests.concept(element));
        break;
      case UPPER_NEIGHBORS:
        ConceptGraph.this
            .highlight(true, highlightRequests.concept(element), highlightRequests.upperNeighbors(element));
        break;
      case LOWER_NEIGHBORS:
        ConceptGraph.this
            .highlight(true, highlightRequests.concept(element), highlightRequests.lowerNeighbors(element));
        break;
      case NEIGHBORS:
        ConceptGraph.this.highlight(
            true,
            highlightRequests.concept(element),
            highlightRequests.upperNeighbors(element),
            highlightRequests.lowerNeighbors(element));
        break;
      case FILTER:
        ConceptGraph.this.highlight(true, highlightRequests.concept(element), highlightRequests.strictFilter(element));
        break;
      case IDEAL:
        ConceptGraph.this.highlight(true, highlightRequests.concept(element), highlightRequests.strictIdeal(element));
        break;
      case FILTER_IDEAL:
        ConceptGraph.this.highlight(
            true,
            highlightRequests.concept(element),
            highlightRequests.strictFilter(element),
            highlightRequests.strictIdeal(element));
        break;
      }
    }

    protected final void dispose() {
      node.radiusProperty().unbind();
    }
  }

  protected final class ConceptEdge extends Graph<Concept<G, M>, Circle>.Edge {

    private ConceptEdge(final Pair<Concept<G, M>, Concept<G, M>> concepts) {
      super(concepts);
      line.strokeWidthProperty().bind(new DoubleBinding() {

        {
          bind(concepts.first().intent(), concepts.second().intent(), controlBox.edgeStroke
              .getSelectionModel()
              .selectedItemProperty());
        }

        protected final double computeValue() {
          return controlBox.edgeStroke
              .getSelectionModel()
              .selectedItemProperty()
              .get()
              .get(fca.context, fca.lattice, concepts);
        }
      });
    }
  }

  protected final class ObjectLabel extends Graph<Concept<G, M>, Circle>.LowerLabel {

    private ObjectLabel(final G object, final Concept<G, M> concept) {
      super(new ObjectBinding<Concept<G, M>>() {

        private final G       g = object;
        private Concept<G, M> c = concept;
        {
          fca.layout.lattice.objectConcepts.addListener(new MapChangeListener<G, Concept<G, M>>() {

            public final void onChanged(final MapChangeListener.Change<? extends G, ? extends Concept<G, M>> change) {
              if (change.wasRemoved() && change.wasAdded() && change.getKey().equals(g)) {
                c = change.getValueAdded();
                invalidate();
              }
            }
          });
        }

        protected final Concept<G, M> computeValue() {
          return c;
        }
      }, new SimpleStringProperty(object.toString()), true);
      text.styleProperty().bind(controlBox.textSizeBinding);
      synchronized (objectLabels) {
        objectLabels.put(object, this);
      }
    }

    private ObjectLabel(final Concept<G, M> concept) {
      super(new SimpleObjectProperty<Concept<G, M>>(concept), new StringBinding() {

        {
          bind(concept.intent(), controlBox.objectLabelText.getSelectionModel().selectedItemProperty());
        }

        protected final String computeValue() {
          return controlBox.objectLabelText
              .getSelectionModel()
              .selectedItemProperty()
              .get()
              .get(fca.context, fca.lattice, concept);
        }
      }, true);
      text.styleProperty().bind(controlBox.textSizeBinding);
    }

    protected final void dispose() {
      synchronized (objectLabels) {
        objectLabels.values().remove(this);
      }
      super.dispose();
    }
  }

  protected final class AttributeLabel extends Graph<Concept<G, M>, Circle>.UpperLabel {

    private AttributeLabel(final M attribute, final Concept<G, M> concept) {
      super(new ObjectBinding<Concept<G, M>>() {

        private final M       m = attribute;
        private Concept<G, M> c = concept;
        {
          fca.layout.lattice.attributeConcepts.addListener(new MapChangeListener<M, Concept<G, M>>() {

            public final void onChanged(final MapChangeListener.Change<? extends M, ? extends Concept<G, M>> change) {
              if (change.wasRemoved() && change.wasAdded() && change.getKey().equals(m)) {
                c = change.getValueAdded();
                invalidate();
              }
            }
          });
        }

        protected final Concept<G, M> computeValue() {
          return c;
        }
      },
      // TODO: the type check for OWLClassExpression is currently a workaround, and will be removed in the future.
          new SimpleStringProperty(attribute instanceof OWLClassExpression
              ? OWLtoString.toString((OWLClassExpression) attribute) : attribute.toString()), true);
      text.styleProperty().bind(controlBox.textSizeBinding);
      synchronized (attributeLabels) {
        attributeLabels.put(attribute, this);
      }
    }

    private AttributeLabel(final Concept<G, M> concept) {
      super(new SimpleObjectProperty<Concept<G, M>>(concept), new StringBinding() {

        {
          bind(concept.intent(), controlBox.attributeLabelText.getSelectionModel().selectedItemProperty());
        }

        protected final String computeValue() {
          return controlBox.attributeLabelText
              .getSelectionModel()
              .selectedItemProperty()
              .get()
              .get(fca.context, fca.lattice, concept);
        }
      }, true);
      text.styleProperty().bind(controlBox.textSizeBinding);
    }

    protected final void dispose() {
      synchronized (attributeLabels) {
        attributeLabels.values().remove(this);
      }
      super.dispose();
    }
  }

  private final class CFXControlBox {

    private final VBox                          content            = new VBox();
    private final ChoiceBox<AnimationSpeed>     animationSpeed     = FXControls.newChoiceBox(
                                                                       AnimationSpeed.DEFAULT,
                                                                       AnimationSpeed.values());
    private final ListSpinner<Integer>          labelTextSize      = FXControls.newListSpinner(
                                                                       12,
                                                                       4,
                                                                       5,
                                                                       6,
                                                                       7,
                                                                       8,
                                                                       9,
                                                                       10,
                                                                       11,
                                                                       12,
                                                                       13,
                                                                       14,
                                                                       16,
                                                                       18,
                                                                       20,
                                                                       24);
    private final StringBinding                 textSizeBinding    = new StringBinding() {

                                                                     {
                                                                       bind(labelTextSize.valueProperty());
                                                                     }

                                                                     protected final String computeValue() {
                                                                       return "-fx-font-size: "
                                                                           + labelTextSize.valueProperty().get() + ";";
                                                                     }
                                                                   };
    private final ChoiceBox<ObjectLabelText>    objectLabelText    = FXControls.newChoiceBox(
                                                                       ObjectLabelText.EXTENT_PERCENTAGE,
                                                                       ObjectLabelText.values());
    private final ChoiceBox<AttributeLabelText> attributeLabelText = FXControls.newChoiceBox(
                                                                       AttributeLabelText.ATTRIBUTE_LABELS,
                                                                       AttributeLabelText.values());
    private final ChoiceBox<VertexRadius>       vertexRadius       = FXControls.newChoiceBox(
                                                                       VertexRadius.NORMAL,
                                                                       VertexRadius.values());
    private final ChoiceBox<EdgeStroke>         edgeStroke         = FXControls.newChoiceBox(
                                                                       EdgeStroke.SMALL,
                                                                       EdgeStroke.values());
    private final ChoiceBox<VertexHighlight>    vertexHighlight    = FXControls.newChoiceBox(
                                                                       VertexHighlight.FILTER_IDEAL,
                                                                       VertexHighlight.values());
    private final Slider                        generations        = SliderBuilder
                                                                       .create()
                                                                       .min(0)
                                                                       .max(16)
                                                                       .value(Constants.GENERATIONS)
                                                                       .majorTickUnit(1)
                                                                       .minorTickCount(0)
                                                                       .snapToTicks(true)
                                                                       .build();
    private final Slider                        population         = SliderBuilder
                                                                       .create()
                                                                       .min(1)
                                                                       .max(64)
                                                                       .value(Constants.POPULATION)
                                                                       .majorTickUnit(1)
                                                                       .minorTickCount(0)
                                                                       .snapToTicks(true)
                                                                       .build();
    private final CheckBox                      conflictChart      = new CheckBox("Conflict Chart");
    private final CheckBox                      voronoiChart       = new CheckBox("Voronoi Chart");
    private final CheckBox                      hideBottom         = new CheckBox("Hide Bottom Concept");
    private final CheckBox                      hideTop            = new CheckBox("Hide Top Concept");

    private CFXControlBox() {
      createContent();
      createListeners();
    }

    private final void createContent() {
      final double inset = 4d;
      content.setPadding(new Insets(inset));
      content.setSpacing(1d);
      content.getChildren().add(RectangleBuilder.create().height(inset).build());
      content.getChildren().add(new Text("Animation Speed:"));
      content.getChildren().add(animationSpeed);
      content.getChildren().add(RectangleBuilder.create().height(inset).build());
      content.getChildren().add(new Text("Label Text Size:"));
      content.getChildren().add(labelTextSize);
      content.getChildren().add(RectangleBuilder.create().height(inset).build());
      content.getChildren().add(new Text("Object Label Text:"));
      content.getChildren().add(objectLabelText);
      content.getChildren().add(RectangleBuilder.create().height(inset).build());
      content.getChildren().add(new Text("Attribute Label Text:"));
      content.getChildren().add(attributeLabelText);
      content.getChildren().add(RectangleBuilder.create().height(inset).build());
      content.getChildren().add(new Text("Concept Vertex Radius:"));
      content.getChildren().add(vertexRadius);
      content.getChildren().add(RectangleBuilder.create().height(inset).build());
      content.getChildren().add(new Text("Concept Edge Stroke:"));
      content.getChildren().add(edgeStroke);
      content.getChildren().add(RectangleBuilder.create().height(inset).build());
      content.getChildren().add(new Text("Concept Vertex Highlight:"));
      content.getChildren().add(vertexHighlight);
      content.getChildren().add(RectangleBuilder.create().height(4d * inset).build());
      content.getChildren().add(FXControls.newText(new StringBinding() {

        {
          bind(generations.valueProperty());
        }

        protected String computeValue() {
          return "Generations: " + (int) generations.valueProperty().get();
        }
      }));
      content.getChildren().add(generations);
      content.getChildren().add(RectangleBuilder.create().height(inset).build());
      content.getChildren().add(FXControls.newText(new StringBinding() {

        {
          bind(population.valueProperty());
        }

        protected String computeValue() {
          return "Population: " + (int) population.valueProperty().get();
        }
      }));
      content.getChildren().add(population);
      content.getChildren().add(RectangleBuilder.create().height(4d * inset).build());
      content.getChildren().add(conflictChart);
      content.getChildren().add(RectangleBuilder.create().height(inset).build());
      content.getChildren().add(voronoiChart);
      content.getChildren().add(RectangleBuilder.create().height(inset).build());
      content.getChildren().add(hideBottom);
      content.getChildren().add(RectangleBuilder.create().height(inset).build());
      content.getChildren().add(hideTop);
    }

    private final void createListeners() {
      front.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {

        public final void handle(final MouseEvent event) {
          final boolean isShown = getRight() != null;
          final boolean shouldShow = event.getX() > front.widthProperty().get() - 50;
          if (isShown == shouldShow)
            return;
          if (!shouldShow)
            setRight(null);
          else
            setRight(content);
        }
      });
      ConceptGraph.this.setOnMouseExited(e -> setRight(null));
      animationSpeed.valueProperty().addListener(new ChangeListener<AnimationSpeed>() {

        public final void changed(
            final ObservableValue<? extends AnimationSpeed> observable,
            final AnimationSpeed oldValue,
            final AnimationSpeed newValue) {
          speed = newValue;
        }
      });
      objectLabelText.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ObjectLabelText>() {

        public final void changed(
            final ObservableValue<? extends ObjectLabelText> observable,
            final ObjectLabelText oldValue,
            final ObjectLabelText newValue) {
          final boolean wasNoneOrObjectLabels =
              oldValue == ObjectLabelText.NONE || oldValue == ObjectLabelText.OBJECT_LABELS;
          switch (newValue) {
          case NONE:
            synchronized (objectLabels) {
              for (ObjectLabel l : objectLabels.values())
                l.dispose();
            }
            synchronized (vertices) {
              for (Vertex cv : vertices.values())
                synchronized (cv.lowerLabels) {
                  for (LowerLabel l : cv.lowerLabels)
                    l.dispose();
                }
            }
            break;
          case OBJECT_LABELS:
            synchronized (vertices) {
              for (Vertex cv : vertices.values())
                synchronized (cv.lowerLabels) {
                  for (LowerLabel l : cv.lowerLabels)
                    l.dispose();
                }
            }
            synchronized (fca.layout.lattice.objectConcepts) {
              for (Entry<G, Concept<G, M>> e : fca.layout.lattice.objectConcepts.entrySet())
                new ObjectLabel(e.getKey(), e.getValue());
            }
            break;
          case EXTENT_SIZE:
          case EXTENT_PERCENTAGE:
          case OBJECT_LABELS_SIZE:
          case OBJECT_LABELS_PERCENTAGE:
            if (wasNoneOrObjectLabels) {
              synchronized (objectLabels) {
                for (ObjectLabel l : objectLabels.values())
                  l.dispose();
              }
              for (Concept<G, M> c : fca.layout.lattice.rowHeads())
                new ObjectLabel(c);
            }
            break;
          }
          fca.layout.invalidate();
        }
      });
      attributeLabelText
          .getSelectionModel()
          .selectedItemProperty()
          .addListener(new ChangeListener<AttributeLabelText>() {

            public final void changed(
                final ObservableValue<? extends AttributeLabelText> observable,
                final AttributeLabelText oldValue,
                final AttributeLabelText newValue) {
              final boolean wasNoneOrAttributeLabels =
                  oldValue == AttributeLabelText.NONE || oldValue == AttributeLabelText.ATTRIBUTE_LABELS;
              switch (newValue) {
              case NONE:
                synchronized (attributeLabels) {
                  for (AttributeLabel u : attributeLabels.values())
                    u.dispose();
                }
                synchronized (vertices) {
                  for (Vertex cv : vertices.values())
                    synchronized (cv.upperLabels) {
                      for (UpperLabel u : cv.upperLabels)
                        u.dispose();
                    }
                }
                break;
              case ATTRIBUTE_LABELS:
                synchronized (vertices) {
                  for (Vertex cv : vertices.values())
                    synchronized (cv.upperLabels) {
                      for (UpperLabel u : cv.upperLabels)
                        u.dispose();
                    }
                }
                synchronized (fca.layout.lattice.attributeConcepts) {
                  for (Entry<M, Concept<G, M>> e : fca.layout.lattice.attributeConcepts.entrySet())
                    new AttributeLabel(e.getKey(), e.getValue());
                }
                break;
              case SEED_LABELS:
                synchronized (vertices) {
                  for (Vertex cv : vertices.values())
                    synchronized (cv.upperLabels) {
                      for (UpperLabel u : cv.upperLabels)
                        u.dispose();
                    }
                }
                synchronized (fca.layout.lattice.attributeConcepts) {
                  for (Entry<M, Concept<G, M>> e : fca.layout.lattice.attributeConcepts.entrySet())
                    if (fca.layout.seeds.containsKey(e.getKey()))
                      new AttributeLabel(e.getKey(), e.getValue());
                }
                break;
              case INTENT_SIZE:
              case INTENT_PERCENTAGE:
              case ATTRIBUTE_LABELS_SIZE:
              case ATTRIBUTE_LABELS_PERCENTAGE:
                if (wasNoneOrAttributeLabels) {
                  synchronized (attributeLabels) {
                    for (AttributeLabel u : attributeLabels.values())
                      u.dispose();
                  }
                  for (Concept<G, M> c : fca.layout.lattice.rowHeads())
                    new AttributeLabel(c);
                }
                break;
              }
              fca.layout.invalidate();
            }
          });
      controller.showVoronoi.bind(voronoiChart.selectedProperty());
      hideBottom.selectedProperty().addListener(new ChangeListener<Boolean>() {

        public void changed(final ObservableValue<? extends Boolean> observable, final Boolean wasHidden, Boolean hide) {
          if (hide == wasHidden)
            return;
          fca.layout.invalidate();
          if (hide) {
//                vertices.get(fca.layout.bottomConcept.get()).node.opacityProperty().bind(zero);
//            vertices.get(fca.context.selection.bottomConcept()).node.opacityProperty().set(0.05d);
//            .bind(new SimpleDoubleProperty(0.05d));
            for (Edge e : upperEdges(fca.context.selection.bottomConcept())) {
              e.line.opacityProperty().unbind();
              e.line.opacityProperty().bind(new SimpleDoubleProperty(0.05d));
            }
          } else {
//                vertices.get(fca.layout.bottomConcept.get()).node.opacityProperty().unbind();
//                vertices.get(fca.layout.bottomConcept.get()).node.opacityProperty().set(1d);
//            vertices.get(fca.context.selection.bottomConcept()).node.opacityProperty().set(1d);
            for (Edge e : upperEdges(fca.context.selection.bottomConcept())) {
              e.line.opacityProperty().unbind();
              e.line.opacityProperty().bind(e.opacity);
            }
          }
        }
      });
      hideTop.selectedProperty().addListener(new ChangeListener<Boolean>() {

        public void changed(final ObservableValue<? extends Boolean> observable, final Boolean wasHidden, Boolean hide) {
          if (hide == wasHidden)
            return;
          fca.layout.invalidate();
          if (hide) {
//                vertices.get(fca.layout.bottomConcept.get()).node.opacityProperty().bind(zero);
            for (Edge e : lowerEdges(fca.context.selection.topConcept())) {
              e.line.opacityProperty().unbind();
              e.line.opacityProperty().bind(new SimpleDoubleProperty(0.05d));
            }
          } else {
//                vertices.get(fca.layout.bottomConcept.get()).node.opacityProperty().unbind();
//                vertices.get(fca.layout.bottomConcept.get()).node.opacityProperty().set(1d);
            for (Edge e : lowerEdges(fca.context.selection.topConcept())) {
              e.line.opacityProperty().unbind();
              e.line.opacityProperty().bind(e.opacity);
            }
          }
        }
      });
    }
  }

  private final class CFXToolBar {

    private final Button       relayout  = new Button();
    private final Button       adjust    = new Button();
    private final ToggleButton highlight = new ToggleButton();
    private final ToggleButton labels    = new ToggleButton();

    private CFXToolBar() {
      final ToolBar toolBar = new ToolBar();
//      final Button exportButton = ButtonBuilder.create().graphic(
//          ImageViewBuilder.create().image(
//              new Image(ConExpFX.class.getResourceAsStream("image/16x16/briefcase.png"))).build()).onAction(
//          new EventHandler<ActionEvent>() {
//
//            @Override
//            public final void handle(final ActionEvent event) {
//              fca.exportTeX();
//            }
//          }).minHeight(
//          24).build();
      toolBar.getItems().addAll(createTransformationBox(), createLayoutBox(), createShowBox(),
//          exportButton,
          createSpace(),
          createSearchBox());
      relayout.setGraphic(ImageViewBuilder
          .create()
          .image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/refresh.png")))
          .build());
      adjust.setGraphic(ImageViewBuilder
          .create()
          .image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/process.png")))
          .build());
      highlight.setGraphic(ImageViewBuilder
          .create()
          .image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/flag.png")))
          .build());
      labels.setGraphic(ImageViewBuilder
          .create()
          .image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/comments.png")))
          .build());
      setTop(toolBar);
    }

    private final HBox createTransformationBox() {
      final ToggleGroup transformationToggleGroup = new ToggleGroup();
      final ToggleButton transformation2DButton = new ToggleButton();
      final ToggleButton transformation3DButton = new ToggleButton();// 2\u00bdD
      final ToggleButton transformationXYButton = new ToggleButton();
      final ToggleButton transformationPolarButton = new ToggleButton();
//      final ToggleButton transformationCircularButton = new ToggleButton();
      transformation2DButton.setGraphic(ImageViewBuilder
          .create()
          .image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/wired.png")))
          .build());
      transformation3DButton.setGraphic(ImageViewBuilder
          .create()
          .image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/wired.png")))
          .build());
      transformationXYButton.setGraphic(ImageViewBuilder
          .create()
          .image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/chart.png")))
          .build());
      transformationPolarButton.setGraphic(ImageViewBuilder
          .create()
          .image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/target.png")))
          .build());
//      transformationCircularButton.setGraphic(ImageViewBuilder
//          .create()
//          .image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/chart_pie.png")))
//          .build());
//      transformation2DButton.setOnAction(new EventHandler<ActionEvent>() {
//
//        public final void handle(final ActionEvent event) {
//          transformation2DButton.toFront();
//        }
//      });
//      transformation3DButton.setOnAction(new EventHandler<ActionEvent>() {
//
//        public final void handle(final ActionEvent event) {
//          transformation3DButton.toFront();
//        }
//      });
//      transformationXYButton.setOnAction(new EventHandler<ActionEvent>() {
//
//        public final void handle(final ActionEvent event) {
//          transformationXYButton.toFront();
//        }
//      });
//      transformationPolarButton.setOnAction(new EventHandler<ActionEvent>() {
//
//        public final void handle(final ActionEvent event) {
//          transformationPolarButton.toFront();
//        }
//      });
//      transformationCircularButton.setOnAction(new EventHandler<ActionEvent>() {
//
//        public final void handle(final ActionEvent event) {
//          transformationCircularButton.toFront();
//        }
//      });
      transformation2DButton.setUserData(GraphTransformation.GRAPH_2D);
      transformation3DButton.setUserData(GraphTransformation.GRAPH_3D);
      transformationXYButton.setUserData(GraphTransformation.XY);
      transformationPolarButton.setUserData(GraphTransformation.POLAR);
//      transformationCircularButton.setUserData(GraphTransformation.CIRCULAR);
      transformation2DButton.setToggleGroup(transformationToggleGroup);
      transformation3DButton.setToggleGroup(transformationToggleGroup);
      transformationXYButton.setToggleGroup(transformationToggleGroup);
      transformationPolarButton.setToggleGroup(transformationToggleGroup);
//      transformationCircularButton.setToggleGroup(transformationToggleGroup);
      transformationToggleGroup.selectToggle(transformation2DButton);
      transformation.bind(new ObjectBinding<GraphTransformation>() {

        private GraphTransformation lastValue = GraphTransformation.GRAPH_3D;
        {
          bind(transformationToggleGroup.selectedToggleProperty());
        }

        protected final GraphTransformation computeValue() {
          final Toggle toggle = transformationToggleGroup.selectedToggleProperty().get();
          if (toggle != null) {
            lastValue = (GraphTransformation) ((ToggleButton) toggle).getUserData();
            switch (lastValue) {
            case GRAPH_2D:
              fca.layout.deleteZ();
            case GRAPH_3D:
              controlBox.conflictChart.setDisable(false);
              // if (conceptEdgeStrokeChoiceBox.getValue() == ConceptEdgeStrokeOption.NONE)
              // conceptEdgeStrokeChoiceBox.setValue(ConceptEdgeStrokeOption.NORMAL);
              break;
            case XY:
            case POLAR:
              controlBox.conflictChart.selectedProperty().set(false);
              controlBox.conflictChart.setDisable(true);
              // conceptEdgeStrokeChoiceBox.setValue(ConceptEdgeStrokeOption.NONE);
              // conceptVertexRadiusChoiceBox.setValue(ConceptVertexRadiusOption.BY_INTENT);
              break;
            case CIRCULAR:
//              new CircularGraph<G, M>().show(fca.lattice);
            }
            return lastValue;
          } else {
            transformationToggleGroup.selectToggle(lastValue == GraphTransformation.GRAPH_3D ? transformation3DButton
                : lastValue == GraphTransformation.XY ? transformationXYButton : transformationPolarButton);
            return lastValue;
          }
        }
      });
//      transformation.addListener((observable, oldTransformation, newTransformation) -> {
//        Platform.runLater(() -> fca.relayout(1, 4));
//      });
      final HBox transformationBox = new HBox();
      transformationBox.setPadding(new Insets(0d));
      transformation2DButton.setStyle("-fx-background-radius: 5 0 0 5, 5 0 0 5, 4 0 0 4, 3 0 0 3;");
      transformation3DButton.setStyle("-fx-background-radius: 0, 0, 0, 0;");
      transformationXYButton.setStyle("-fx-background-radius: 0, 0, 0, 0;");
      transformationPolarButton
//      .setStyle("-fx-background-radius: 0, 0, 0, 0;");
//      transformationCircularButton
          .setStyle("-fx-background-radius: 0 5 5 0, 0 5 5 0, 0 4 4 0, 0 3 3 0;");
      transformation2DButton.setMinHeight(24);
      transformation3DButton.setMinHeight(24);
      transformationXYButton.setMinHeight(24);
      transformationPolarButton.setMinHeight(24);
//      transformationCircularButton.setMinHeight(24);
      transformationBox.getChildren().addAll(
          transformation2DButton,
          transformation3DButton,
          transformationXYButton,
          transformationPolarButton
//          ,transformationCircularButton
          );
      return transformationBox;
    }

    private final HBox createLayoutBox() {
      relayout.setOnAction(new EventHandler<ActionEvent>() {

        public final void handle(final ActionEvent event) {
//          relayout.toFront();
          fca.relayout((int) controlBox.generations.getValue(), (int) controlBox.population.getValue());
        }
      });
      adjust.setOnAction(new EventHandler<ActionEvent>() {

        public final void handle(final ActionEvent event) {
//          adjust.toFront();
          fca.refine((int) controlBox.generations.getValue());
        }
      });
      relayout.setMinHeight(24);
      adjust.setMinHeight(24);
      relayout.setStyle("-fx-background-radius: 5 0 0 5, 5 0 0 5, 4 0 0 4, 3 0 0 3;");
      adjust.setStyle("-fx-background-radius: 0 5 5 0, 0 5 5 0, 0 4 4 0, 0 3 3 0;");
      final HBox layoutBox = new HBox();
      layoutBox.setPadding(new Insets(0d));
      layoutBox.getChildren().addAll(relayout, adjust);
      return layoutBox;
    }

    private final HBox createShowBox() {
      highlight.setSelected(false);
      highlight.setMinHeight(24);
      labels.setSelected(true);
      labels.selectedProperty().addListener(new ChangeListener<Boolean>() {

        public final void changed(
            final ObservableValue<? extends Boolean> observable,
            final Boolean wasSelected,
            final Boolean isSelected) {
          if (isSelected) {
            controlBox.objectLabelText.getSelectionModel().select(ObjectLabelText.OBJECT_LABELS);
            controlBox.attributeLabelText.getSelectionModel().select(AttributeLabelText.ATTRIBUTE_LABELS);
          } else {
            controlBox.objectLabelText.getSelectionModel().select(ObjectLabelText.NONE);
            controlBox.attributeLabelText.getSelectionModel().select(AttributeLabelText.NONE);
          }
        }
      });
      labels.setMinHeight(24);
      highlight.setStyle("-fx-background-radius: 5 0 0 5, 5 0 0 5, 4 0 0 4, 3 0 0 3;");
      labels.setStyle("-fx-background-radius: 0 5 5 0, 0 5 5 0, 0 4 4 0, 0 3 3 0;");
      final HBox showBox = new HBox();
      showBox.setPadding(new Insets(0d));
      showBox.getChildren().addAll(highlight, labels);
      return showBox;
    }

    private final HBox createSpace() {
      final HBox space = new HBox();
      HBox.setHgrow(space, Priority.ALWAYS);
      return space;
    }

    private final SearchBox createSearchBox() {
      final SearchBox searchBox = new SearchBox();
      searchBox.textBox.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {

        public final void handle(final MouseEvent event) {
          ConceptGraph.this.requestFocus();
        }
      });
      searchBox.textBox.textProperty().addListener(new ChangeListener<String>() {

        public final void changed(
            final ObservableValue<? extends String> observable,
            final String oldValue,
            final String newValue) {
          if (newValue.equals(""))
            highlight(false, highlightRequests.dehighlight());
          else {
            final Set<Concept<G, M>> concepts = new HashSet<Concept<G, M>>();
            for (G g : fca.context.selection.rowHeads())
              if (g.toString().toLowerCase().contains(newValue.toLowerCase()))
                concepts.add(fca.lattice.objectConcepts.get(g));
            for (M m : fca.context.selection.colHeads())
              if (m.toString().toLowerCase().contains(newValue.toLowerCase()))
                concepts.add(fca.lattice.attributeConcepts.get(m));
            highlight(true, Iterables.concat(Iterables.transform(
                concepts,
                new Function<Concept<G, M>, Iterable<HighlightRequest>>() {

                  public final Iterable<HighlightRequest> apply(final Concept<G, M> concept) {
                    return highlightRequests.concept(concept);
                  }
                })));
          }
        }
      });
      return searchBox;
    }
  }

  private final FCADataset<G, M>       fca;
  private final CFXControlBox          controlBox;
  private final CFXToolBar             toolBar;
  private final Map<G, ObjectLabel>    objectLabels      = new ConcurrentHashMap<G, ObjectLabel>();
  private final Map<M, AttributeLabel> attributeLabels   = new ConcurrentHashMap<M, AttributeLabel>();
  private LayoutEvolution<G, M>        qualityEvolution  = null;
  public final HighlightRequests       highlightRequests = new HighlightRequests();
  private boolean                      dontHighlight     = false;
  private TransitionTimer              highlightTimer    = new TransitionTimer(
                                                             Duration.seconds(1),
                                                             new EventHandler<ActionEvent>() {

                                                               public final void handle(final ActionEvent event) {
                                                                 dontHighlight = false;
                                                               }
                                                             });
  public final HighlightLock           highlightLock     = new HighlightLock();

  public final class HighlightLock extends ALock {

    protected HighlightLock() {
      super("highlight");
    }

    @Override
    public final void lock() {
      highlightTimer.stop();
      dontHighlight = true;
    }

    @Override
    public final void unlock() {
      highlightTimer.stop();
      highlightTimer.play();
    }

    @Override
    public final boolean isLocked() {
      return dontHighlight;
    }
  }

  public ConceptGraph(final FCADataset<G, M> fcaInstance) {
    super(fcaInstance.layout);
    this.fca = fcaInstance;
    this.controlBox = new CFXControlBox();
    this.toolBar = new CFXToolBar();
    this.initLayoutListeners();
    this.initMouseListeners();
  }

  private final void initLayoutListeners() {
    initVertexListeners();
    initEdgeListeners();
    initLabelListeners();
    fca.layout.lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
//        Platform.runLater(new Runnable() {
//
//          public final void run() {
            controller.graphLock.lock();
            front.getChildren().clear();
            // edges.clear();
            // vertices.clear();
            // addEdges.clear();
            // addVertices.clear();
            // deleteVertices.clear();
            // deleteEdges.clear();
            // pendingVertices.clear();
            // pendingEdges.clear();
            controller.polarBottom = null;
            controller.graphLock.unlock();
//          }
//        });
      }
    }, RelationEvent.ROWS_CLEARED);
  }

  private final void initVertexListeners() {
    fca.layout.lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
//        synchronized (fca.layout.generators) {
          for (final Concept<G, M> concept : event.getRows())
            if (fca.layout.generators.containsKey(concept)) {
              synchronized (fca.layout.generators) {
              final Concept<G, M> generator = fca.layout.generators.remove(concept);
              final Circle gContent = vertices.get(generator).node;
              new ConceptVertex(concept, gContent.translateXProperty().get(), gContent.translateYProperty().get());}
            } else
              new ConceptVertex(concept, null, null);
//        }
      }
    }, RelationEvent.ROWS_ADDED);
    fca.layout.lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
//        synchronized (fca.layout.generators) {
          for (Concept<G, M> concept : event.getRows())
            if (fca.layout.generators.containsKey(concept)) {
              synchronized (fca.layout.generators) {
              final Concept<G, M> generator = fca.layout.generators.remove(concept);
              controller.disposeVertex(concept, generator);}
            } else
              controller.disposeVertex(concept, null);
//        }
      }
    }, RelationEvent.ROWS_REMOVED);
  }

  private final void initEdgeListeners() {
    fca.layout.lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
        for (final Pair<Concept<G, M>, Concept<G, M>> concepts : event.getEntries())
          new ConceptEdge(concepts);
      }
    }, RelationEvent.ENTRIES_ADDED);
    fca.layout.lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
        for (final Pair<Concept<G, M>, Concept<G, M>> concepts : event.getEntries())
          controller.disposeEdge(concepts);
      }
    }, RelationEvent.ENTRIES_REMOVED);
  }

  private final void initLabelListeners() {
    fca.layout.lattice.objectConcepts.addListener(new MapChangeListener<G, Concept<G, M>>() {

      public final void onChanged(final MapChangeListener.Change<? extends G, ? extends Concept<G, M>> change) {
        if (!controlBox.objectLabelText.getSelectionModel().getSelectedItem().equals(ObjectLabelText.OBJECT_LABELS))
          return;
        if (change.wasRemoved() && change.wasAdded())
          return;
        if (change.wasAdded())
          new ObjectLabel(change.getKey(), change.getValueAdded());
        else if (change.wasRemoved())
          objectLabels.get(change.getKey()).dispose();
      }
    });
    fca.layout.lattice.attributeConcepts.addListener(new MapChangeListener<M, Concept<G, M>>() {

      public final void onChanged(final MapChangeListener.Change<? extends M, ? extends Concept<G, M>> change) {
        if (!controlBox.attributeLabelText
            .getSelectionModel()
            .getSelectedItem()
            .equals(AttributeLabelText.ATTRIBUTE_LABELS))
          return;
        if (change.wasRemoved() && change.wasAdded())
          return;
        if (change.wasAdded())
          new AttributeLabel(change.getKey(), change.getValueAdded());
        else if (change.wasRemoved())
          attributeLabels.get(change.getKey()).dispose();
      }
    });
  }

  private final void initMouseListeners() {
    front.addEventHandler(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {

      public final void handle(final ScrollEvent event) {
        if (transformation.get().equals(GraphTransformation.GRAPH_3D))
          fca.layout.rotate(event.getDeltaX() / (80d * Math.PI));
//          fca.layout.rotate((event.getDeltaX() == 0 ? event.getDeltaY() : event.getDeltaX()) / (80d * Math.PI));
      }
    });
    final EventHandler<MouseEvent> lariatSelectionHandler = new EventHandler<MouseEvent>() {

      private Path path = null;

      public final void handle(final MouseEvent event) {
        if (!event.getButton().equals(MouseButton.PRIMARY))
          return;
        if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
          path = new Path(new MoveTo(event.getX(), event.getY()));
          path.getStrokeDashArray().addAll(2d, 4d);
          front.getChildren().add(path);
        } else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
          if (path != null && path.getElements().size() > 1) {
            path.getElements().add(new ClosePath());
            path.setFill(Color.RED);
            final Set<Concept<G, M>> concepts =
                new HashSet<Concept<G, M>>(Maps.filterValues(vertices, new Predicate<Vertex>() {

                  public final boolean apply(final Vertex v) {
                    return path.contains(v.node.translateXProperty().get(), v.node.translateYProperty().get());
                  }
                }).keySet());
            final Concept<G, M> supremum = fca.layout.lattice.supremum(concepts);
            final Concept<G, M> infimum = fca.layout.lattice.infimum(concepts);
            highlight(true, Iterables.concat(
                highlightRequests.filter(supremum),
                highlightRequests.ideal(infimum),
                highlightRequests.interval(infimum, supremum)), Iterables.concat(Iterables.transform(
                concepts,
                new Function<Concept<G, M>, Iterable<HighlightRequest>>() {

                  public final Iterable<HighlightRequest> apply(final Concept<G, M> concept) {
                    return highlightRequests.concept(concept);
                  }
                })));
          }
          front.getChildren().remove(path);
          path = null;
        } else if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED))
          path.getElements().add(new LineTo(event.getX(), event.getY()));
      }
    };
    front.addEventHandler(MouseEvent.MOUSE_PRESSED, lariatSelectionHandler);
    front.addEventHandler(MouseEvent.MOUSE_DRAGGED, lariatSelectionHandler);
    front.addEventHandler(MouseEvent.MOUSE_RELEASED, lariatSelectionHandler);
  }

  protected final BoundingBox getContentBoundingBox() {
    if (fca == null || fca.layout == null || controlBox == null)
      return new BoundingBox(0, 0, 0, 0, 0, 0);
    return fca.layout.getCurrentBoundingBox(controlBox.hideBottom.isSelected(), controlBox.hideTop.isSelected());
  }

  protected final void initPolarBottom(final Config c, final Timeline t) {
    synchronized (vertices) {
      controller.polarBottom = vertices.get(fca.context.selection.bottomConcept());
    }
    // controller.bottom = Iterables.find(vertices.values(), new Predicate<ConceptVertex>()
//      {
//        public final boolean apply(final ConceptVertex v)
//        {
//          return lowerEdges(v.element).isEmpty();
//        }
//      });
    if (controller.polarBottom != null) {
      controller.polarBottom.node.radiusProperty().unbind();
      for (Edge e : upperEdges(controller.polarBottom.element))
        e.unbindStart();
      t.getKeyFrames().add(new KeyFrame(Duration.ONE, new EventHandler<ActionEvent>() {

        public final void handle(final ActionEvent event) {
          controller.polarBottom.node.fillProperty().set(
              new RadialGradient(0d, 0d, 0.5d, 0.5d, 0.5d, true, CycleMethod.NO_CYCLE, new Stop[] {
                  new Stop(0d, Color.TRANSPARENT), new Stop(0.95d, Color.WHITE), new Stop(0.975d, Color.DODGERBLUE),
                  new Stop(1d, Color.BLACK) }));
          controller.polarBottom.node.toBack();
        }
      }));
    }
  }

  protected final void drawPolarBottom(final Config c, final Timeline t) {
    t.getKeyFrames().add(
        new KeyFrame(
            speed.frameSize,
            controller.fadeZ(controller.polarBottom.node, new Point3D(0, 0, 0)),
            new KeyValue(controller.polarBottom.node.translateXProperty(), c.x0),
            new KeyValue(controller.polarBottom.node.translateYProperty(), c.y0),
            new KeyValue(controller.polarBottom.node.radiusProperty(), 0.9d * Math.min(c.x0, c.y0) * zoom.get())));
    for (Edge e : upperEdges(controller.polarBottom.element)) {
      final Point3D p = c.iso.apply(e.upper.position.getValue());
      final Point2D q = Points.projectOnCircle(0, 0, c.box.getHeight(), p.getX(), p.getY());
      t.getKeyFrames().add(
          new KeyFrame(speed.frameSize, new KeyValue(e.line.startXProperty(), c.x0 + c.f * q.getX()), new KeyValue(
              e.line.startYProperty(),
              c.y0 + c.f * q.getY())));
    }
  }

  protected final void resetPolarBottom(final Config c, final Timeline t) {
    final Point3D q = c.toPane(controller.polarBottom.position.getValue());
    t.getKeyFrames().add(
        new KeyFrame(speed.frameSize, controller.translateX(controller.polarBottom.node, q), controller.translateY(
            controller.polarBottom.node,
            q)));
    for (final Edge e : upperEdges(controller.polarBottom.element)) {
      t.getKeyFrames().add(
          new KeyFrame(speed.frameSize, new KeyValue(e.line.startXProperty(), q.getX()), new KeyValue(e.line
              .startYProperty(), q.getY())));
    }
    t.getKeyFrames().add(new KeyFrame(Duration.ONE, new EventHandler<ActionEvent>() {

      public final void handle(final ActionEvent event) {
        new NumberPropertyTransition(
            speed.frameSize,
            controller.polarBottom.node.radiusProperty(),
            5d,
            new EventHandler<ActionEvent>() {

              public final void handle(final ActionEvent event) {
                controller.polarBottom.init();
                for (final Edge e : upperEdges(controller.polarBottom.element))
                  e.bindStart();
                controller.polarBottom.node.fillProperty().set(COLOR_DEFAULT);
                controller.polarBottom.node.toFront();
                controller.polarBottom = null;
              }
            }).play();
      }
    }));
  }

  @SafeVarargs
  public final void highlight(final boolean fadeComplement, final Iterable<HighlightRequest>... requests) {
    if (!controller.graphLock.isLocked() && !dontHighlight && toolBar.highlight.isSelected()) {
      final Timeline t = new Timeline();
      for (Iterable<HighlightRequest> it : requests)
        for (final HighlightRequest r : it)
          buildTimeline(t, r, true);
      if (fadeComplement)
        buildTimeline(t, constructComplementHighlightRequest(requests), false);
      t.play();
    }
  }

  @SuppressWarnings("incomplete-switch")
  private final void buildTimeline(final Timeline t, final HighlightRequest r, final boolean toFront) {
    if (toFront)
      synchronized (vertices) {
        for (final Concept<G, M> c : r.elements) {
          final Vertex v = vertices.get(c);
          if (v != null) {
            Iterable<Edge> edges = null;
            switch (r.edgeHighlightOption) {
            case CONTAINS_BOTH:
              edges = Collections2.filter(upperEdges(v.element), new Predicate<Edge>() {

                public final boolean apply(final Edge c) {
                  return r.elements.contains(c.elements.second());
                }
              });
              break;
            case CONTAINS_ONE:
              edges = Iterables.concat(upperEdges(v.element), lowerEdges(v.element));
              break;
            case CONTAINS_LOWER:
              edges = upperEdges(v.element);
              break;
            case CONTAINS_UPPER:
              edges = lowerEdges(v.element);
              break;
            }
            if (edges != null)
              for (final Edge e : edges)
                t.getKeyFrames().add(new KeyFrame(Duration.ONE, new EventHandler<ActionEvent>() {

                  public final void handle(final ActionEvent event) {
                    // e.line.toFront();
                    if (!e.line.opacityProperty().isBound())
                      e.line.opacityProperty().set(1d);
                    new StrokeTransition(speed.frameSize, e.line, (Color) e.line.getStroke(), r.edgeColor).play();
                  };
                }));
            if (!v.equals(controller.polarBottom))
              t.getKeyFrames().add(new KeyFrame(Duration.ONE, new EventHandler<ActionEvent>() {

                public final void handle(final ActionEvent event) {
                  // v.node.toFront();
                  if (!threeDimensions() && !v.node.opacityProperty().isBound())
                    v.node.opacityProperty().set(1d);
                  new FillTransition(speed.frameSize, v.node, v.node.getFill() instanceof Color ? (Color) v.node
                      .getFill() : Color.TRANSPARENT, r.vertexColor).play();
                };
              }));
            for (final UpperLabel l : v.upperLabels)
              t.getKeyFrames().add(new KeyFrame(Duration.ONE, new EventHandler<ActionEvent>() {

                public void handle(ActionEvent event) {
                  // l.content.toFront();
                  if (!threeDimensions())
                    l.content.opacityProperty().set(1d);
                  new FillTransition(speed.frameSize, l.back, (Color) l.back.getFill(), r.attributeLabelBackColor)
                      .play();
                  new FillTransition(speed.frameSize, l.text, (Color) l.text.getFill(), r.attributeLabelTextColor)
                      .play();
                };
              }));
            for (final LowerLabel l : v.lowerLabels)
              t.getKeyFrames().add(new KeyFrame(Duration.ONE, new EventHandler<ActionEvent>() {

                public void handle(ActionEvent event) {
                  // l.content.toFront();
                  if (!threeDimensions())
                    l.content.opacityProperty().set(1d);
                  new FillTransition(speed.frameSize, l.back, (Color) l.back.getFill(), r.objectLabelBackColor).play();
                  new FillTransition(speed.frameSize, l.text, (Color) l.text.getFill(), r.objectLabelTextColor).play();
                };
              }));
          }
        }
      }
    else
      synchronized (vertices) {
        for (final Concept<G, M> c : r.elements) {
          final Vertex v = vertices.get(c);
          if (v != null) {
            for (final UpperLabel l : v.upperLabels)
              t.getKeyFrames().add(new KeyFrame(Duration.ONE, new EventHandler<ActionEvent>() {

                public void handle(ActionEvent event) {
                  // l.content.toBack();
                  if (!threeDimensions())
                    l.content.opacityProperty().set(0.2d);
                  new FillTransition(speed.frameSize, l.back, (Color) l.back.getFill(), r.attributeLabelBackColor)
                      .play();
                  new FillTransition(speed.frameSize, l.text, (Color) l.text.getFill(), r.attributeLabelTextColor)
                      .play();
                };
              }));
            for (final LowerLabel l : v.lowerLabels)
              t.getKeyFrames().add(new KeyFrame(Duration.ONE, new EventHandler<ActionEvent>() {

                public void handle(ActionEvent event) {
                  // l.content.toBack();
                  if (!threeDimensions())
                    l.content.opacityProperty().set(0.2d);
                  new FillTransition(speed.frameSize, l.back, (Color) l.back.getFill(), r.objectLabelBackColor).play();
                  new FillTransition(speed.frameSize, l.text, (Color) l.text.getFill(), r.objectLabelTextColor).play();
                };
              }));
            if (!v.equals(controller.polarBottom))
              t.getKeyFrames().add(new KeyFrame(Duration.ONE, new EventHandler<ActionEvent>() {

                public final void handle(final ActionEvent event) {
                  // v.node.toBack();
                  if (!threeDimensions() && !v.node.opacityProperty().isBound())
                    v.node.opacityProperty().set(0.2d);
                  new FillTransition(speed.frameSize, v.node, v.node.getFill() instanceof Color ? (Color) v.node
                      .getFill() : Color.TRANSPARENT, r.vertexColor).play();
                };
              }));
            Iterable<Edge> edges = null;
            switch (r.edgeHighlightOption) {
            case CONTAINS_BOTH:
              edges = Collections2.filter(upperEdges(v.element), new Predicate<Edge>() {

                public final boolean apply(final Edge c) {
                  return r.elements.contains(c.elements.second());
                }
              });
              break;
            case CONTAINS_ONE:
              edges = Iterables.concat(upperEdges(v.element), lowerEdges(v.element));
              break;
            case CONTAINS_LOWER:
              edges = upperEdges(v.element);
              break;
            case CONTAINS_UPPER:
              edges = lowerEdges(v.element);
              break;
            }
            if (edges != null)
              for (final Edge e : edges)
                t.getKeyFrames().add(new KeyFrame(Duration.ONE, new EventHandler<ActionEvent>() {

                  public final void handle(final ActionEvent event) {
                    // e.line.toBack();
                    if (!e.line.opacityProperty().isBound())
                      e.line.opacityProperty().set(0.2d);
                    new StrokeTransition(speed.frameSize, e.line, (Color) e.line.getStroke(), r.edgeColor).play();
                  };
                }));
          }
        }
      }
  }

  private final HighlightRequest constructComplementHighlightRequest(
      @SuppressWarnings("unchecked") final Iterable<HighlightRequest>... requests) {
    return new HighlightRequest(
        Sets.newHashSet(fca.layout.lattice.complement(Sets.newHashSet(Iterables.concat(Iterables.transform(
            Iterables.concat(Arrays.asList(requests)),
            new Function<HighlightRequest, Iterable<Concept<G, M>>>() {

              public final Iterable<Concept<G, M>> apply(final HighlightRequest request) {
                return request.elements;
              }
            }))))),
        EdgeHighlight.CONTAINS_ONE,
        COLOR_UNCOMPARABLE,
        COLOR_UNCOMPARABLE,
        COLOR_UNCOMPARABLE,
        Color.WHITE,
        COLOR_UNCOMPARABLE,
        Color.WHITE);
  }

  public final class HighlightRequests {

    private HighlightRequests() {}

    public final Iterable<HighlightRequest> dehighlight() {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return Collections.<HighlightRequest> singleton(new HighlightRequest(
          vertices.keySet(),
          EdgeHighlight.CONTAINS_UPPER,
          COLOR_DEFAULT,
          Color.BLACK,
          COLOR_LABEL_DEFAULT,
          Color.BLACK,
          COLOR_LABEL_DEFAULT,
          Color.BLACK));
    }

    public final Iterable<HighlightRequest> concept(final Concept<G, M> concept) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return Collections.<HighlightRequest> singleton(new HighlightRequest(
          Collections.singleton(concept),
          EdgeHighlight.CONTAINS_ONE,
          COLOR_CONCEPT,
          COLOR_CONCEPT,
          COLOR_CONCEPT,
          Color.BLACK,
          COLOR_CONCEPT,
          Color.BLACK));
    }

    public final Iterable<HighlightRequest> implication(final Implication<M> implication) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      final Set<Concept<G, M>> conceptsP = new HashSet<Concept<G, M>>();
      final Set<Concept<G, M>> conceptsC = new HashSet<Concept<G, M>>();
      final Set<Concept<G, M>> concepts = new HashSet<Concept<G, M>>();
      final Set<Concept<G, M>> inner = new HashSet<Concept<G, M>>();
      for (M m : implication.getPremise())
        conceptsP.add(fca.context.attributeConcept(m));
      for (M m : implication.getConclusion())
        conceptsC.add(fca.context.attributeConcept(m));
      for (G g : fca.context.colAnd(implication.getPremise()))
        concepts.add(fca.context.objectConcept(g));
//      final Collection<Concept<G, M>> lower =
//          Collections3.union(Collections2.transform(
//              conceptsC,
//              new Function<Concept<G, M>, Collection<Concept<G, M>>>() {
//
//                @Override
//                public Collection<Concept<G, M>> apply(Concept<G, M> mm) {
//                  return Collections3.difference(fca.lattice.ideal(mm), Collections.singleton(mm));
//                }
//              }));
//      final Collection<Concept<G, M>> upper =
//          Collections3.union(Collections2.transform(
//              conceptsP,
//              new Function<Concept<G, M>, Collection<Concept<G, M>>>() {
//
//                @Override
//                public Collection<Concept<G, M>> apply(Concept<G, M> mm) {
//                  return Collections3.difference(fca.lattice.filter(mm), Collections.singleton(mm));
//                }
//              }));
//      inner.addAll(Collections3.intersection(lower, upper));
      return Sets.<HighlightRequest> newHashSet(new HighlightRequest(
          fca.lattice.ideal(fca.lattice.supremum(concepts)),
          EdgeHighlight.CONTAINS_BOTH,
          COLOR_CONCEPT,
          COLOR_CONCEPT,
          COLOR_CONCEPT,
          Color.WHITE,
          COLOR_CONCEPT,
          Color.WHITE), new HighlightRequest(
          conceptsP,
          EdgeHighlight.CONTAINS_LOWER,
          COLOR_UPPER,
          COLOR_UPPER,
          COLOR_UPPER,
          Color.BLACK,
          COLOR_UPPER,
          Color.BLACK), new HighlightRequest(
          conceptsC,
          EdgeHighlight.CONTAINS_UPPER,
          COLOR_LOWER,
          COLOR_LOWER,
          COLOR_LOWER,
          Color.BLACK,
          COLOR_LOWER,
          Color.BLACK), new HighlightRequest(
          inner,
          EdgeHighlight.CONTAINS_BOTH,
          COLOR_INTERVAL,
          COLOR_INTERVAL,
          COLOR_INTERVAL,
          Color.WHITE,
          COLOR_INTERVAL,
          Color.WHITE));
    }

    public final Iterable<HighlightRequest> upperNeighbors(final Concept<G, M> concept) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return Collections.<HighlightRequest> singleton(new HighlightRequest(
          fca.layout.lattice.upperNeighbors(concept),
          EdgeHighlight.CONTAINS_LOWER,
          COLOR_UPPER,
          COLOR_UPPER,
          COLOR_LABEL_DEFAULT,
          Color.BLACK,
          COLOR_UPPER,
          Color.WHITE));
    }

    public final Iterable<HighlightRequest> lowerNeighbors(final Concept<G, M> concept) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return Collections.<HighlightRequest> singleton(new HighlightRequest(
          fca.layout.lattice.lowerNeighbors(concept),
          EdgeHighlight.CONTAINS_UPPER,
          COLOR_LOWER,
          COLOR_LOWER,
          COLOR_LOWER,
          Color.WHITE,
          COLOR_LABEL_DEFAULT,
          Color.BLACK));
    }

    public final Iterable<HighlightRequest> filter(final Concept<G, M> concept) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return Collections.<HighlightRequest> singleton(new HighlightRequest(
          fca.layout.lattice.filter(concept),
          EdgeHighlight.CONTAINS_LOWER,
          COLOR_UPPER,
          COLOR_UPPER,
          COLOR_LABEL_DEFAULT,
          Color.BLACK,
          COLOR_UPPER,
          Color.WHITE));
    }

    public final Iterable<HighlightRequest> ideal(final Concept<G, M> concept) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return Collections.<HighlightRequest> singleton(new HighlightRequest(
          fca.layout.lattice.ideal(concept),
          EdgeHighlight.CONTAINS_UPPER,
          COLOR_LOWER,
          COLOR_LOWER,
          COLOR_LOWER,
          Color.WHITE,
          COLOR_LABEL_DEFAULT,
          Color.BLACK));
    }

    public final Iterable<HighlightRequest> strictFilter(final Concept<G, M> concept) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return Collections.<HighlightRequest> singleton(new HighlightRequest(
          Sets.difference(fca.layout.lattice.filter(concept), Collections.singleton(concept)),
          EdgeHighlight.CONTAINS_LOWER,
          COLOR_UPPER,
          COLOR_UPPER,
          COLOR_LABEL_DEFAULT,
          Color.BLACK,
          COLOR_UPPER,
          Color.WHITE));
    }

    public final Iterable<HighlightRequest> strictIdeal(final Concept<G, M> concept) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return Collections.<HighlightRequest> singleton(new HighlightRequest(
          Sets.difference(fca.layout.lattice.ideal(concept), Collections.singleton(concept)),
          EdgeHighlight.CONTAINS_UPPER,
          COLOR_LOWER,
          COLOR_LOWER,
          COLOR_LOWER,
          Color.WHITE,
          COLOR_LABEL_DEFAULT,
          Color.BLACK));
    }

    public final Iterable<HighlightRequest>
        interval(final Concept<G, M> lowerConcept, final Concept<G, M> upperConcept) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return Collections.<HighlightRequest> singleton(new HighlightRequest(
          fca.layout.lattice.interval(lowerConcept, upperConcept),
          EdgeHighlight.CONTAINS_BOTH,
          COLOR_INTERVAL,
          COLOR_INTERVAL,
          COLOR_INTERVAL,
          Color.WHITE,
          COLOR_INTERVAL,
          Color.WHITE));
    }

    public final Iterable<HighlightRequest> object(final G object) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return filter(fca.context.selection.objectConcept(object));
    }

    public final Iterable<HighlightRequest> attribute(final M attribute) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return ideal(fca.context.selection.attributeConcept(attribute));
    }

    public final Iterable<HighlightRequest> incidence(final G object, final M attribute) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return interval(fca.context.selection.objectConcept(object), fca.context.selection.attributeConcept(attribute));
    }

    public final Iterable<HighlightRequest> nonIncidence(final G object, final M attribute) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      return Iterables.concat(
          filter(fca.context.selection.objectConcept(object)),
          ideal(fca.context.selection.attributeConcept(attribute)));
    }

    public final Iterable<HighlightRequest> downArrow(final G object, final M attribute) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      final Concept<G, M> objectConcept = fca.context.selection.objectConcept(object);
      final Concept<G, M> attributeConcept = fca.context.selection.attributeConcept(attribute);
      final Concept<G, M> uniqueLowerNeighborOfObjectConcept = fca.lattice.infimum(objectConcept, attributeConcept);
      return Iterables.concat(
          filter(objectConcept),
          ideal(attributeConcept),
          concept(objectConcept),
          concept(uniqueLowerNeighborOfObjectConcept));
    }

    public final Iterable<HighlightRequest> upArrow(final G object, final M attribute) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      final Concept<G, M> objectConcept = fca.context.selection.objectConcept(object);
      final Concept<G, M> attributeConcept = fca.context.selection.attributeConcept(attribute);
      final Concept<G, M> uniqueUpperNeighborOfAttributeConcept = fca.lattice.supremum(objectConcept, attributeConcept);
      return Iterables.concat(
          filter(objectConcept),
          ideal(attributeConcept),
          concept(attributeConcept),
          concept(uniqueUpperNeighborOfAttributeConcept));
    }

    @SuppressWarnings("unchecked")
    public final Iterable<HighlightRequest> bothArrow(final G object, final M attribute) {
      if (controller.graphLock.isLocked() || dontHighlight || !toolBar.highlight.isSelected())
        return Collections.emptySet();
      final Concept<G, M> objectConcept = fca.context.selection.objectConcept(object);
      final Concept<G, M> attributeConcept = fca.context.selection.attributeConcept(attribute);
      final Concept<G, M> uniqueUpperNeighborOfAttributeConcept = fca.lattice.supremum(objectConcept, attributeConcept);
      final Concept<G, M> uniqueLowerNeighborOfObjectConcept = fca.lattice.infimum(objectConcept, attributeConcept);
      return Iterables.concat(
          filter(objectConcept),
          ideal(attributeConcept),
          concept(objectConcept),
          concept(attributeConcept),
          concept(uniqueLowerNeighborOfObjectConcept),
          concept(uniqueUpperNeighborOfAttributeConcept));
    }
  }

  public final boolean threeDimensions() {
    return transformation.get().equals(GraphTransformation.GRAPH_3D);
  }

  public final boolean polar() {
    return transformation.get().equals(GraphTransformation.POLAR);
  }
  
  @Override
  public void removeContent() {
    super.removeContent();
    objectLabels.clear();
    attributeLabels.clear();
  }
}
