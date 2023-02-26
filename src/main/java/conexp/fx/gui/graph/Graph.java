package conexp.fx.gui.graph;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

import be.humphreys.simplevoronoi.GraphEdge;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.layout.LayoutEvolution;
import conexp.fx.core.math.GuavaIsomorphism;
import conexp.fx.core.math.VoronoiGenerator;
import conexp.fx.gui.graph.option.AnimationSpeed;
import conexp.fx.gui.graph.option.EdgeHighlight;
import conexp.fx.gui.graph.option.GraphTransformation;
import conexp.fx.gui.graph.transformation.PolarTransformation;
import conexp.fx.gui.graph.transformation.RotationXY;
import conexp.fx.gui.util.LaTeX;
import conexp.fx.gui.util.Platform2;
import conexp.fx.gui.util.SynchronizedPane;
import conexp.fx.gui.util.TransitionTimer;
import javafx.animation.FadeTransitionBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.LabelBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;

@SuppressWarnings("deprecation")
public abstract class Graph<T, N extends Node> extends BorderPane {

  public static final Color COLOR_DEFAULT       = Color.valueOf("#0576B0");
  public static final Color COLOR_LABEL_DEFAULT = Color.valueOf("#EEEEEE");

  protected abstract class Vertex {

    protected final T                          element;
    protected final N                          node;
    protected final ObservableValue<Point3D>   position;
    protected final ObservableList<UpperLabel> upperLabels     =
        FXCollections.observableList(new LinkedList<UpperLabel>());
    protected final ObservableList<LowerLabel> lowerLabels     =
        FXCollections.observableList(new LinkedList<LowerLabel>());
    protected final Set<Pair<Vertex, Edge>>    pendingVertices = new HashSet<Pair<Vertex, Edge>>();

    protected Vertex(
        final T element,
        final N node,
        final ObservableValue<Point3D> position,
        final Double layoutX,
        final Double layoutY) {
      super();
      this.element = element;
      this.node = node;
      this.position = position;
      this.node.translateXProperty().set(layoutX == null ? front.getWidth() / 2d : layoutX);
      this.node.translateYProperty().set(layoutY == null ? front.getHeight() / 2d : layoutY);
      synchronized (vertices) {
        vertices.put(element, this);
      }
      synchronized (controller.addVertices) {
        controller.addVertices.offer(this);
      }
    }

    protected abstract void init();

    protected abstract void dispose();
  }

  protected abstract class Edge {

    protected Vertex           lower;
    protected Vertex           upper;
    protected final Pair<T, T> elements;
    protected final Line       line          = new Line();
    protected DoubleBinding    opacity;
    private boolean            isInitialized = false;
    protected boolean          isDisposing   = false;

    protected Edge(final Pair<T, T> elements) {
      super();
      this.elements = elements;
      this.line.setStrokeWidth(2);
      synchronized (edges) {
        edges.put(elements, this);
      }
      synchronized (controller.addEdges) {
        controller.addEdges.offer(this);
      }
    }

    protected void initialize() throws NullPointerException {
      if (isInitialized)
        return;
      synchronized (vertices) {
        lower = vertices.get(elements.x());
        if (lower == null)
          throw new NullPointerException();
        upper = vertices.get(elements.y());
        if (upper == null)
          throw new NullPointerException();
      }
      bindStart();
      bindEnd();
      opacity = new DoubleBinding() {

        {
          bind(lower.node.opacityProperty(), upper.node.opacityProperty());
        }

        protected final double computeValue() {
          return (lower.node.opacityProperty().get() + upper.node.opacityProperty().get()) / 2d;
        }
      };
      isInitialized = true;
    }

    protected void dispose() {
      line.strokeWidthProperty().unbind();
      line.opacityProperty().unbind();
      unbindStart();
      unbindEnd();
    }

    protected final void bindOpacity() {
      line.opacityProperty().bind(opacity);
    }

    protected final void bindStart() {
      line.startXProperty().bind(lower.node.translateXProperty());
      line.startYProperty().bind(lower.node.translateYProperty());
    }

    protected final void bindEnd() {
      line.endXProperty().bind(upper.node.translateXProperty());
      line.endYProperty().bind(upper.node.translateYProperty());
    }

    protected final void unbindStart() {
      line.startXProperty().unbind();
      line.startYProperty().unbind();
    }

    protected final void unbindEnd() {
      line.endXProperty().unbind();
      line.endYProperty().unbind();
    }
  }

  protected abstract class Label {

    protected final ObservableValue<T>      element;
    protected final ObjectBinding<Vertex>   vertex;
    protected final ImageView               tex           = new ImageView();
    protected final Text                    text          = new Text();
    protected final boolean                 showLaTeX;
    protected final Rectangle               back          = new Rectangle();
    protected final StackPane               content       = StackPaneBuilder.create().children(back).build();
    protected final IntegerProperty         index         = new SimpleIntegerProperty(0);
    protected final ObjectProperty<Point2D> shift         = new SimpleObjectProperty<Point2D>(new Point2D(0, 0));
    protected boolean                       isInitialized = false;

    protected Label(final ObservableValue<T> element, final ObservableValue<String> string, final boolean showLaTeX) {
      super();
      this.showLaTeX = showLaTeX;
      if (showLaTeX) {
        content.getChildren().add(LabelBuilder.create().graphic(tex).build());
      } else {
        content.getChildren().add(text);
      }
      this.element = element;
      this.vertex = new ObjectBinding<Vertex>() {

        {
          bind(element);
        }

        protected final Vertex computeValue() {
          synchronized (vertices) {
            return vertices.get(element.getValue());
          }
        }
      };
      this.rebind(null, vertex.get());
      this.vertex.addListener(new ChangeListener<Vertex>() {

        public final void
            changed(final ObservableValue<? extends Vertex> observable, final Vertex from, final Vertex to) {
          rebind(from, to);
        }
      });
      if (showLaTeX) {
        this.tex.imageProperty().bind(LaTeX.toFXImageBinding(string, new FloatBinding() {

          {
            bind(zoom, textSize);
          }

          @Override
          protected final float computeValue() {
            return (float) (zoom.get() * textSize.get());
          }
        }));
      } else {
        this.text.textProperty().bind(string);
      }
      this.createContent();
      synchronized (controller.addLabels) {
        controller.addLabels.offer(this);
      }
    }

    protected abstract void rebind(final Vertex from, final Vertex to);

    private void createContent() {
      back.setFill(COLOR_LABEL_DEFAULT);
      back.setStrokeType(StrokeType.OUTSIDE);
      back.setStroke(Color.BLACK);
      back.setStrokeWidth(0.25d);
      back.setOpacity(0.8d);
      if (showLaTeX) {
        back.widthProperty().bind(new DoubleBinding() {

          {
            bind(tex.boundsInLocalProperty());
          }

          protected double computeValue() {
            return tex.boundsInLocalProperty().getValue().getWidth() + 4;
          }
        });
        back.heightProperty().bind(new DoubleBinding() {

          {
            bind(tex.boundsInLocalProperty());
          }

          protected double computeValue() {
            return tex.boundsInLocalProperty().getValue().getHeight();
          }
        });
      } else {
        back.widthProperty().bind(new DoubleBinding() {

          {
            bind(text.boundsInLocalProperty());
          }

          protected double computeValue() {
            return text.boundsInLocalProperty().getValue().getWidth() + 4;
          }
        });
        back.heightProperty().bind(new DoubleBinding() {

          {
            bind(text.boundsInLocalProperty());
          }

          protected double computeValue() {
            return text.boundsInLocalProperty().getValue().getHeight();
          }
        });
      }
    }

    protected void dispose() {
      index.unbind();
      synchronized (controller.removeLabels) {
        controller.removeLabels.add(this);
      }
    }
  }

  protected abstract class LowerLabel extends Label {

    protected LowerLabel(ObservableValue<T> element, ObservableValue<String> string, final boolean showLaTeX) {
      super(element, string, showLaTeX);
    }

    protected void rebind(final Vertex from, final Vertex to) {
      if (from != null)
        synchronized (from.lowerLabels) {
          from.lowerLabels.remove(this);
        }
      synchronized (to.lowerLabels) {
        to.lowerLabels.add(this);
      }
      index.bind(new IntegerBinding() {

        {
          bind(to.lowerLabels);
        }

        protected int computeValue() {
          synchronized (to.lowerLabels) {
            return to.lowerLabels.indexOf(LowerLabel.this);
          }
        }
      });
    }

    protected void dispose() {
      super.dispose();
      final Vertex v = vertex.get();
      synchronized (v.lowerLabels) {
        v.lowerLabels.remove(this);
      }
    }
  }

  protected abstract class UpperLabel extends Label {

    protected UpperLabel(ObservableValue<T> element, ObservableValue<String> string, final boolean showLaTeX) {
      super(element, string, showLaTeX);
    }

    protected void rebind(final Vertex from, final Vertex to) {
      if (from != null)
        synchronized (from.upperLabels) {
          from.upperLabels.remove(this);
        }
      synchronized (to.upperLabels) {
        to.upperLabels.add(this);
      }
      index.bind(new IntegerBinding() {

        {
          bind(to.upperLabels);
        }

        protected int computeValue() {
          synchronized (to.upperLabels) {
            return -to.upperLabels.indexOf(UpperLabel.this) - 1;
          }
        }
      });
    }

    protected void dispose() {
      super.dispose();
      final Vertex v = vertex.get();
      synchronized (v.upperLabels) {
        v.upperLabels.remove(this);
      }
    }
  }

  protected class Tile extends Rectangle {

    protected final Rectangle2D rect;

    protected Tile(final LayoutEvolution<?, ?>.Value value, final Double initialX, final Double initialY) {
      this(value.rectangle);
      if (value.result > 1d)
        this.setFill(Color.RED);
      else
        this.setFill(Color.rgb((int) (255d * Math.pow(1d - value.result, 0.125d)), 255, 255));
      if (initialX != null)
        this.translateXProperty().set(initialX);
      if (initialY != null)
        this.translateYProperty().set(initialY);
    }

    private Tile(final Rectangle2D rect) {
      super();
      this.rect = rect;
      synchronized (controller.addTiles) {
        controller.addTiles.add(this);
      }
    }
  }

  protected class HighlightRequest {

    protected final Set<T>        elements;
    protected final EdgeHighlight edgeHighlightOption;
    protected final Color         vertexColor;
    protected final Color         edgeColor;
    protected final Color         objectLabelBackColor;
    protected final Color         objectLabelTextColor;
    protected final Color         attributeLabelBackColor;
    protected final Color         attributeLabelTextColor;

    protected HighlightRequest(
        final Set<T> elements,
        final EdgeHighlight edgeHighlightPolicy,
        final Color vertexColor,
        final Color edgeColor,
        final Color objectLabelBackColor,
        final Color objectLabelTextColor,
        final Color attributeLabelBackColor,
        final Color attributeLabelTextColor) {
      super();
      this.elements = elements;
      this.edgeHighlightOption = edgeHighlightPolicy;
      this.vertexColor = vertexColor;
      this.edgeColor = edgeColor;
      this.objectLabelBackColor = objectLabelBackColor;
      this.objectLabelTextColor = objectLabelTextColor;
      this.attributeLabelBackColor = attributeLabelBackColor;
      this.attributeLabelTextColor = attributeLabelTextColor;
    }
  }

  protected final class Config {

    public final GuavaIsomorphism<Point3D, Point3D> iso;
    public final BoundingBox                        box;
    public final double                             f, x0, y0, w, h;

    protected Config(
        final GuavaIsomorphism<Point3D, Point3D> iso,
        final BoundingBox box,
        final double f,
        final double x0,
        final double y0,
        final double w,
        final double h) {
      super();
      this.iso = iso;
      this.box = box;
      this.f = f;
      this.x0 = x0;
      this.y0 = y0;
      this.w = w;
      this.h = h;
    }

    protected final Point3D toPane(final Point3D p) {
      final Point3D q = iso.apply(p);
      final double z = (q.getZ() - box.getMinZ()) / (box.getMaxZ() - box.getMinZ());
      return new Point3D(q.getX() * f + x0, q.getY() * f + y0, z);
    }

    protected final Point3D toContent(final Point3D p) {
      return iso.invert(new Point3D((p.getX() - x0) / f, (p.getY() - y0) / f, 0));
    }

    protected final Point3D toContent(final Point2D p) {
      return iso.invert(new Point3D((p.getX() - x0) / f, (p.getY() - y0) / f, 0));
    }
  }

//  protected final class Cleaner {
//
////    private final class CleanTask extends TimerTask {
////
////      public void run() {
////        if (Platform.isFxApplicationThread())
////          clean();
////        else
////          Platform.runLater(new Runnable() {
////
////            public void run() {
////              clean();
////            }
////          });
////      }
////    }
////
////    private final Timer timer = new Timer();
////    private TimerTask   task  = new TimerTask() {
////
////      public void run() {}
////    };
////
////    protected final void schedule() {
////      task.cancel();
////      timer.purge();
////      task = new CleanTask();
////      timer.schedule(task, (int) (10d * speed.frameSize.toMillis()));
////    }
//
//    private Cleaner(final Observable... observable) {
//      Arrays.asList(observable).forEach(o -> o.addListener(__ -> clean()));
//    }
//  }

  public class Controller {

    protected final ObjectBinding<Config> config;
    private boolean                       isLocked        = false;
    private boolean                       isDragging      = false;
    private AnimationSpeed                speedBeforeDrag;
    protected final BooleanProperty       showVoronoi     = new SimpleBooleanProperty(false);
//    protected final Cleaner               cleaner         = new Cleaner();
    protected final Queue<Vertex>         addVertices     = new ConcurrentLinkedQueue<Vertex>();
    protected final Queue<Edge>           addEdges        = new ConcurrentLinkedQueue<Edge>();
    protected final Queue<Label>          addLabels       = new ConcurrentLinkedQueue<Label>();
    protected final Queue<Tile>           addTiles        = new ConcurrentLinkedQueue<Tile>();
    protected final Queue<Pair<T, T>>     removeVertices  = new ConcurrentLinkedQueue<Pair<T, T>>();
    protected final Queue<Pair<T, T>>     removeEdges     = new ConcurrentLinkedQueue<Pair<T, T>>();
    protected final Queue<Label>          removeLabels    = new ConcurrentLinkedQueue<Label>();
    protected final Set<Vertex>           pendingVertices = Collections3.newConcurrentHashSet();
    protected final Set<Edge>             pendingEdges    = Collections3.newConcurrentHashSet();
    public Vertex                         polarBottom     = null;

    protected Controller(final Observable... observable) {
      super();
      this.config = new ObjectBinding<Config>() {

        {
          Arrays.asList(observable).forEach(this::bind);
          bind(front.widthProperty(), front.heightProperty(), transformation, showVoronoi, zoom, pan);
        }

        private Config value;

        protected Config computeValue() {
          if (!isDragging) {
            final BoundingBox box = getContentBoundingBox();
            final double w = front.widthProperty().get();
            final double h = front.heightProperty().get();
            final GuavaIsomorphism<Point3D, Point3D> iso;
            final double f, x0, y0;
            switch (transformation.get()) {
            case GRAPH_3D:
            case GRAPH_2D:
              iso = GuavaIsomorphism.<Point3D> identity();
              final double widthRatio = box.getWidth() < 0.001d ? Double.MAX_VALUE : w / box.getWidth();
              final double heightRatio = box.getHeight() < 0.001d ? Double.MAX_VALUE : h / box.getHeight();
              final double depthRatio = box.getDepth() < 0.001d ? Double.MAX_VALUE : w / box.getDepth();
              final double paneToLayoutRatio = Math.min(widthRatio, Math.min(heightRatio, depthRatio));
              f = (0.9d * (paneToLayoutRatio == Double.MAX_VALUE ? 1d : paneToLayoutRatio));
              x0 = (-box.getMinX() * f + (w - box.getWidth() * f) / 2d);
              y0 = (-box.getMinY() * f + (h - box.getHeight() * f) / 2d);
              break;
            case XY:
              iso = new RotationXY(new Point3D(0, 0, 0), -Math.PI * 3d / 4d);
              final Point3D top = iso.apply(new Point3D(box.getMaxX(), box.getMaxY(), 0));
              final Point3D left = iso.apply(new Point3D(box.getMaxX(), box.getMinY(), 0));
              final Point3D right = iso.apply(new Point3D(box.getMinX(), box.getMaxY(), 0));
              final Point3D bot = iso.apply(new Point3D(box.getMinX(), box.getMinY(), 0));
              final double width = Math.abs(right.getX() - left.getX());
              final double height = Math.abs(top.getY() - bot.getY());
              f = 0.9d * Math.min(w / width, h / height);
              x0 = -left.getX() * f + (w - width * f) / 2d;
              y0 = -top.getY() * f + (h - height * f) / 2d;
              break;
            case POLAR:
              iso = new PolarTransformation(box.getMinX(), box.getWidth());
              f = box.getHeight() < 0.001d ? 1d : 0.9d * (Math.min(w, h) / (2d * box.getHeight()));
              x0 = w / 2d;
              y0 = h / 2d;
              break;
            default:
              iso = GuavaIsomorphism.<Point3D> identity();
              f = 1d;
              x0 = 0d;
              y0 = 0d;
              break;
            }
            value = new Config(iso, box, f * zoom.get(), x0 + pan.get().getX(), y0 + pan.get().getY(), w, h);
          } else
            invalidate();
          return value;
        }
      };
      config.addListener(new InvalidationListener() {

        public final void invalidated(final Observable observable) {
          refresh();
        }
      });
      showVoronoi.addListener(new ChangeListener<Boolean>() {

        public final void changed(
            final ObservableValue<? extends Boolean> observable,
            final Boolean oldValue,
            final Boolean newValue) {
          if (!newValue)
            Platform2.runOnFXThread(back.getChildren()::clear);
        }
      });
      refresh();
//      new Timer().schedule(new TimerTask() {
//
//        @Override
//        public void run() {
//          refresh();
//        }
//      }, 0, 2l * (long) speed.frameSize.toMillis());
    }

    public boolean graphLock = false;

    public final void drag() {
      isDragging = true;
      speedBeforeDrag = speed;
      speed = AnimationSpeed.FASTESTEST;
    }

    public final void dragDone() {
      isDragging = false;
      speed = speedBeforeDrag;
//      refresh();
    }

    public final boolean isDragging() {
      return isDragging;
    }

    protected final void disposeVertex(final T element, final T to) {
//      if (element == null || to == null)
//        throw new NullPointerException();
      synchronized (removeVertices) {
        removeVertices.offer(Pair.of(element, to));
      }
    }

    protected final void disposeEdge(final Pair<T, T> elements) {
//      if (elements.first() == null || elements.second() == null)
//        throw new NullPointerException();
      synchronized (removeEdges) {
        removeEdges.offer(elements);
      }
    }

    protected final void clearBack() {
      Platform2.runOnFXThread(back::clear);
    }

    private final void clean() {
//      final Collection<Node> circles = Collections2.transform(vertices.values(), new Function<Vertex, Node>() {
//
//        public final Node apply(final Vertex v) {
//          return v.node;
//        }
//      });
//      final Collection<Node> lines = Collections2.transform(edges.values(), new Function<Edge, Node>() {
//
//        public final Node apply(final Edge e) {
//          return e.line;
//        }
//      });
//      final Collection<Node> labels =
//          Collections3.union(Collections2.transform(vertices.values(), new Function<Vertex, Collection<Node>>() {
//
//            public final Collection<Node> apply(final Vertex v) {
//              return Collections2
//                  .transform(Collections3.union(v.lowerLabels, v.upperLabels), new Function<Label, Node>() {
//
//                public final Node apply(final Label l) {
//                  return l.content;
//                }
//              });
//            }
//          }));
////      Platform2.runOnFXThread(() -> front.getChildren().retainAll(Collections3.union(circles, lines, labels)));
//      front.getChildren().retainAll(Collections3.union(circles, lines, labels));
      final Set<Node> children = Collections3.newConcurrentHashSet();
      synchronized (vertices) {
        vertices.values().parallelStream().map(v -> v.node).forEach(children::add);
        vertices.values().parallelStream().map(v -> v.lowerLabels.parallelStream()).reduce(Stream::concat).ifPresent(
            s -> s.map(l -> l.content).forEach(children::add));
        vertices.values().parallelStream().map(v -> v.upperLabels.parallelStream()).reduce(Stream::concat).ifPresent(
            s -> s.map(l -> l.content).forEach(children::add));
      }
      synchronized (edges) {
        edges.values().parallelStream().map(e -> e.line).forEach(children::add);
      }
      front.getChildren().retainAll(children);
    }

    public synchronized final void refresh() {
      if (isLocked)
        return;
      final Timeline t = new Timeline();
//      if (!isLocked) {
      final Config c = config.get();
      removeContent(t);
      addContent(t);
      refreshBottom(c, t);
      refreshVertices(c, t);
      if (showVoronoi.get())
        refreshVoronoi(c, t);
      else
        refreshTiles(c, t);
//      } else {
//        t.getKeyFrames().add(new KeyFrame(speed.frameSize));
//      }
      t.setOnFinished(__ -> {
        clean();
//        refresh();
      });
      Platform2.runOnFXThread(t::play);
    }

    private final void addContent(final Timeline t) {
      synchronized (addVertices) {
        while (!addVertices.isEmpty()) {
          final Vertex v = addVertices.poll();
          final Transition fadeIn = fadeIn(v.node);
          t.getKeyFrames().add(new KeyFrame(Duration.ONE, __ -> {
            front.add(v.node);
            fadeIn.play();
          }));
        }
      }
      synchronized (addEdges) {
        while (!addEdges.isEmpty()) {
          final Edge e = addEdges.poll();
          e.initialize();
          t.getKeyFrames().add(new KeyFrame(Duration.ONE, __ -> {
            e.initialize();
            front.add(e.line);
            e.line.toBack();
            fadeIn(e).play();
          }));
        }
      }
      synchronized (addLabels) {
        while (!addLabels.isEmpty()) {
          final Label l = addLabels.poll();
          l.content.opacityProperty().set(0d);
          final Transition fadeIn =
              FadeTransitionBuilder.create().node(l.content).duration(speed.frameSize).toValue(0d).build();
          final Transition fadeIn2 = fadeIn(l.content);
          fadeIn.setOnFinished(__ -> {
            l.content.toFront();
            l.content.layoutXProperty().set(l.shift.getValue().getX() - l.content.widthProperty().get() / 2d);
            l.content.layoutYProperty().set(
                l.shift.getValue().getY() + l.index.doubleValue() * l.content.heightProperty().get());
            l.content.setVisible(true);
            l.isInitialized = true;
            fadeIn2.play();
          });
          t.getKeyFrames().add(new KeyFrame(Duration.ZERO, __ -> {
            l.content.setVisible(false);
            front.add(l.content);
            fadeIn.play();
          }));
        }
      }
    }

    private final void removeContent(final Timeline t) {
      synchronized (removeVertices) {
        synchronized (removeEdges) {
          removeEdges.removeAll(removeVertices);
          for (Pair<T, T> e = removeEdges.poll(); e != null; e = removeEdges.poll()) {
            final Edge edge;
            synchronized (edges) {
              edge = edges.remove(e);
            }
            // TODO
//            if (edge != null) {
            edge.isDisposing = true;
            edge.line.opacityProperty().unbind();
            synchronized (pendingEdges) {
              pendingEdges.add(edge);
            }
//            } else {
//              System.err.println(new NullPointerException());
//            }
          }
        }
        for (Pair<T, T> v = removeVertices.poll(); v != null; v = removeVertices.poll()) {
          final Vertex vertex;
          synchronized (vertices) {
            vertex = vertices.remove(v.first());
          }
          synchronized (vertex.lowerLabels) {
            for (LowerLabel l : vertex.lowerLabels)
              l.dispose();
          }
          synchronized (vertex.upperLabels) {
            for (UpperLabel u : vertex.upperLabels)
              u.dispose();
          }
          if (v.second() == null)
            synchronized (pendingVertices) {
              pendingVertices.add(vertex);
            }
          else {
            final Vertex to;
            synchronized (vertices) {
              to = vertices.get(v.second());
            }
            final Edge edge;
            synchronized (edges) {
              edge = edges.remove(v);
            }
//            if (edge != null) {
            edge.isDisposing = true;
            edge.line.opacityProperty().unbind();
            synchronized (to.pendingVertices) {
              to.pendingVertices.add(Pair.of(vertex, edge));
            }
//            }
          }
        }
      }
      synchronized (pendingEdges) {
        for (Edge edge : pendingEdges)
          t.getKeyFrames().add(new KeyFrame(speed.frameSize, dispose(edge), fadeOut(edge.line)));
      }
      synchronized (pendingVertices) {
        for (Vertex vertex : pendingVertices)
          t.getKeyFrames().add(new KeyFrame(speed.frameSize, dispose(vertex), fadeOut(vertex.node)));
      }
      synchronized (removeLabels) {
        for (Label label : removeLabels)
          t.getKeyFrames().add(new KeyFrame(speed.frameSize, dispose(label), fadeOut(label.content)));
      }
    }

    private final void refreshBottom(final Config c, final Timeline t) {
      switch (transformation.get()) {
      case GRAPH_3D:
      case GRAPH_2D:
      case XY:
        if (polarBottom != null)
          resetPolarBottom(c, t);
        break;
      case POLAR:
        if (polarBottom == null)
          initPolarBottom(c, t);
        if (polarBottom != null)
          drawPolarBottom(c, t);
        break;
      }
    }

//    private final Predicate<Label> isInitialized = new Predicate<Label>()
//                                                   {
//                                                     public final boolean apply(final Label label)
//                                                     {
//                                                       return label.isInitialized;
//                                                     };
//                                                   };
    private final void refreshVertices(final Config c, final Timeline t) {
      synchronized (vertices) {
        for (Vertex v : vertices.values()) {
          if (!v.equals(polarBottom)) {
            final Point3D p = c.toPane(v.position.getValue());
            t.getKeyFrames().add(
                new KeyFrame(speed.frameSize, translateX(v.node, p), translateY(v.node, p), fadeZ(v.node, p)));
            synchronized (v.lowerLabels) {
              for (LowerLabel l : v.lowerLabels)
                if (l.isInitialized)
                  t.getKeyFrames().add(
                      new KeyFrame(
                          speed.frameSize,
                          translateX(l.content, p),
                          translateY(l.content, p),
                          fadeZ(l.content, p),
                          layoutX(l),
                          layoutY(l)));
                else
                  t.getKeyFrames().add(
                      new KeyFrame(
                          speed.frameSize,
                          translateX(l.content, p),
                          translateY(l.content, p),
                          fadeZ(l.content, p)));
            }
            synchronized (v.upperLabels) {
              for (UpperLabel u : v.upperLabels)
                if (u.isInitialized)
                  t.getKeyFrames().add(
                      new KeyFrame(
                          speed.frameSize,
                          translateX(u.content, p),
                          translateY(u.content, p),
                          fadeZ(u.content, p),
                          layoutX(u),
                          layoutY(u)));
                else
                  t.getKeyFrames().add(
                      new KeyFrame(
                          speed.frameSize,
                          translateX(u.content, p),
                          translateY(u.content, p),
                          fadeZ(u.content, p)));
            }
            synchronized (v.pendingVertices) {
              for (Pair<Vertex, Edge> pv : v.pendingVertices)
                t.getKeyFrames().add(
                    new KeyFrame(
                        speed.frameSize,
                        dispose(v, pv),
                        translateX(pv.first().node, p),
                        translateY(pv.first().node, p),
                        fadeZ(pv.first().node, p),
                        fadeOut(pv.first().node),
                        fadeOut(pv.second().line)));
            }
          }
        }
      }
    }

    private final void refreshTiles(final Config c, final Timeline t) {
      synchronized (addTiles) {
        while (!addTiles.isEmpty()) {
          final Tile tile = addTiles.poll();
          back.add(tile);
          t.getKeyFrames().add(
              new KeyFrame(
                  speed.frameSize,
                  new KeyValue(tile.translateXProperty(), c.x0 + c.f * tile.rect.getMinX()),
                  new KeyValue(tile.translateYProperty(), c.y0 + c.f * tile.rect.getMinY()),
                  new KeyValue(tile.widthProperty(), c.f * tile.rect.getWidth()),
                  new KeyValue(tile.heightProperty(), c.f * tile.rect.getHeight())));
        }
      }
      // for (Tile tile : Collections3.getTypedElements(back.getChildren(), Tile.class))
      // t.getKeyFrames().add(
      // new KeyFrame(
      // speed.frameSize,
      // new KeyValue(tile.translateXProperty(), c.x0 + c.f * tile.rect.getMinX()),
      // new KeyValue(tile.translateYProperty(), c.y0 + c.f * tile.rect.getMinY()),
      // new KeyValue(tile.widthProperty(), c.f * tile.rect.getWidth()),
      // new KeyValue(tile.heightProperty(), c.f * tile.rect.getHeight())));
    }

    private final void refreshVoronoi(final Config c, final Timeline t) {
      t.getKeyFrames().add(new KeyFrame(Duration.ONE, __ -> new TransitionTimer(speed.frameSize, ___ -> {
        back.clear();
        synchronized (vertices) {
          for (GraphEdge e : VoronoiGenerator.generate(
              Collections2.transform(
                  Collections2.filter(vertices.values(), Predicates.not(Predicates.equalTo(polarBottom))),
                  new Function<Vertex, Point3D>() {

                    public final Point3D apply(final Vertex v) {
                      return new Point3D(v.node.translateXProperty().get(), v.node.translateYProperty().get(), 0);
                    }
                  }),
              0,
              c.w,
              0,
              c.h)) {
            final Line l = new Line(e.x1, e.y1, e.x2, e.y2);
            l.setStroke(Color.RED);
            back.add(l);
          }
        }
      }).play()));
    }

    protected final KeyValue translateX(final Node n, final Point3D p) {
      return new KeyValue(n.translateXProperty(), p.getX());
    }

    protected final KeyValue translateY(final Node n, final Point3D p) {
      return new KeyValue(n.translateYProperty(), p.getY());
    }

    protected final KeyValue fadeZ(final Node n, final Point3D p) {
      return new KeyValue(n.opacityProperty(), Math.pow(1d - 0.9d * p.getZ(), 2.5d));
    }

    private final KeyValue layoutX(final Label label) {
      return new KeyValue(
          label.content.layoutXProperty(),
          label.shift.getValue().getX() - label.content.widthProperty().get() / 2d);
    }

    private final KeyValue layoutY(final Label label) {
      return new KeyValue(
          label.content.layoutYProperty(),
          label.shift.getValue().getY() + label.index.doubleValue() * label.content.heightProperty().get());
    }

    protected final Transition fadeIn(final Node n) {
      n.opacityProperty().set(0d);
      return FadeTransitionBuilder.create().node(n).duration(speed.frameSize).toValue(1d).build();
    }

    protected final Transition fadeIn(final Edge e) {
      e.line.opacityProperty().set(0d);
      return FadeTransitionBuilder
          .create()
          .node(e.line)
          .duration(speed.frameSize)
          .toValue(e.opacity.get())
          .onFinished(__ -> {
            if (!e.isDisposing)
              e.bindOpacity();
          })
          .build();
    }

    protected final KeyValue fadeOut(final Node n) {
      return new KeyValue(n.opacityProperty(), 0d);
    }

    private final EventHandler<ActionEvent> dispose(final Vertex vertex) {
      return __ -> {
        synchronized (pendingVertices) {
          pendingVertices.remove(vertex);
        }
        vertex.dispose();
        front.remove(vertex.node);
        synchronized (vertex.pendingVertices) {
          if (!vertex.pendingVertices.isEmpty())
            synchronized (pendingVertices) {
              synchronized (pendingEdges) {
                for (Pair<Vertex, Edge> p : vertex.pendingVertices) {
                  pendingVertices.add(p.first());
                  pendingEdges.add(p.second());
                }
              }
            }
        }
      };
    }

    private final EventHandler<ActionEvent> dispose(final Edge edge) {
      return __ -> {
        synchronized (pendingEdges) {
          pendingEdges.remove(edge);
        }
        edge.dispose();
        front.remove(edge.line);
      };
    }

    private final EventHandler<ActionEvent> dispose(final Vertex v, final Pair<Vertex, Edge> pv) {
      return __ -> {
        synchronized (v.pendingVertices) {
          v.pendingVertices.remove(pv);
        }
        pv.second().dispose();
        pv.first().dispose();
        front.remove(pv.second().line);
        front.remove(pv.first().node);
        synchronized (pv.first().pendingVertices) {
          if (!pv.first().pendingVertices.isEmpty())
            synchronized (pendingVertices) {
              synchronized (pendingEdges) {
                for (Pair<Vertex, Edge> p : pv.first().pendingVertices) {
                  pendingVertices.add(p.first());
                  pendingEdges.add(p.second());
                }
              }
            }
        }
      };
    }

    private final EventHandler<ActionEvent> dispose(final Label label) {
      return __ -> {
        synchronized (removeLabels) {
          removeLabels.remove(label);
        }
        front.remove(label.content);
      };
    }
  }

  public final Controller                             controller;
  protected final Map<T, Vertex>                      vertices       = new ConcurrentHashMap<T, Vertex>();
  protected final Map<Pair<T, T>, Edge>               edges          = new ConcurrentHashMap<Pair<T, T>, Edge>();
  protected final SynchronizedPane                    front          = new SynchronizedPane();
  protected final SynchronizedPane                    back           = new SynchronizedPane();
  protected AnimationSpeed                            speed          = AnimationSpeed.DEFAULT;
  protected final ObjectProperty<GraphTransformation> transformation =
      new SimpleObjectProperty<GraphTransformation>(GraphTransformation.GRAPH_3D);
  protected final DoubleProperty                      zoom           = new SimpleDoubleProperty(1d);
  protected final IntegerProperty                     textSize       = new SimpleIntegerProperty(12);
  protected final ObjectProperty<Point2D>             pan            =
      new SimpleObjectProperty<Point2D>(new Point2D(0d, 0d));

  protected Graph(final Observable... observable) {
    super();
    this.controller = new Controller(observable);
//    new Cleaner(observable);
    this.setCenter(StackPaneBuilder.create().children(back, front).build());
    front.addEventHandler(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {

      public final void handle(final ScrollEvent event) {
        if (event.getDeltaY() > 0)
          zoom.set(zoom.get() * 1.1d);
        else if (event.getDeltaY() < 0)
          zoom.set(zoom.get() / 1.1d);
      }
    });
    final EventHandler<MouseEvent> panHandler = new EventHandler<MouseEvent>() {

      private double startX, startY;
      private double panX, panY;

      public final void handle(final MouseEvent event) {
        if (!event.getButton().equals(MouseButton.SECONDARY))
          return;
        if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
          startX = event.getSceneX();
          startY = event.getSceneY();
          panX = pan.get().getX();
          panY = pan.get().getY();
        } else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
          // do nothing
        } else if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
          double deltaX = event.getSceneX() - startX;
          double deltaY = event.getSceneY() - startY;
          pan.set(new Point2D(panX + deltaX, panY + deltaY));
        }
      }
    };
    front.addEventHandler(MouseEvent.MOUSE_PRESSED, panHandler);
    front.addEventHandler(MouseEvent.MOUSE_DRAGGED, panHandler);
    front.addEventHandler(MouseEvent.MOUSE_RELEASED, panHandler);
  }

  protected final Collection<Edge> lowerEdges(final T element) {
    synchronized (edges) {
      return
//          new HashSet<Edge>(
      Maps.filterKeys(edges, new Predicate<Pair<T, T>>() {

        public final boolean apply(final Pair<T, T> p) {
          return p.second().equals(element);
        }
      }).values()
//          )
      ;
    }
  }

  protected final Collection<Edge> upperEdges(final T element) {
    synchronized (edges) {
      return
//          new HashSet<Edge>(
      Maps.filterKeys(edges, new Predicate<Pair<T, T>>() {

        public final boolean apply(final Pair<T, T> p) {
          return p.first().equals(element);
        }
      }).values()
//              )
      ;
    }
  }

  protected abstract BoundingBox getContentBoundingBox();

  protected abstract void initPolarBottom(final Config c, final Timeline t);

  protected abstract void drawPolarBottom(final Config c, final Timeline t);

  protected abstract void resetPolarBottom(final Config c, final Timeline t);

  public abstract void
      highlight(boolean fadeComplement, @SuppressWarnings("unchecked") Iterable<HighlightRequest>... requests);

  public void removeContent() {
    front.getChildren().clear();
    back.getChildren().clear();
    edges.clear();
    vertices.clear();
//    edges.keySet().forEach(controller::disposeEdge);
//    vertices.keySet().forEach(v -> controller.disposeVertex(v, null));
//    edges.clear();
//    vertices.clear();
  }

}
