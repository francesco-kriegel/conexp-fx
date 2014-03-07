package conexp.fx.core.quality;

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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.concurrent.BlockingTask;
import conexp.fx.core.context.Concept;
import conexp.fx.core.layout.ConceptLayout;
import conexp.fx.core.layout.ConceptMovement;
import conexp.fx.core.math.Points;

public final class LayoutEvolution<G, M>
{
  public static final <G, M> BlockingTask calculate(final LayoutEvolution<G, M> qualityChart)
  {
    return new BlockingTask("Quality Measure Chart")
      {
        @Override
        protected final void _call()
        {
          updateMessage("Simulating Concept Movement...");
          qualityChart.progressProperty.addListener(new ChangeListener<Number>()
            {
              @Override
              public final void changed(
                  final ObservableValue<? extends Number> observable,
                  final Number oldValue,
                  final Number newValue)
              {
                updateProgress(newValue.doubleValue(), 1d);
              }
            });
          qualityChart.calculate();
        }
      };
  }

  public final class Value
    implements Comparable<Value>
  {
    public final Point3D         delta;
    public final Rectangle2D     rectangle;
    public final double          result;
    public final Concept<G, M>   hint;
    public final Map<M, Point3D> seeds;

    private Value(
        final Point3D delta,
        final Rectangle2D rectangle,
        final Pair<Concept<G, M>, Double> result,
        final Map<M, Point3D> seeds)
    {
      super();
      this.delta = delta;
      this.rectangle = rectangle;
      this.result = result.second();
      this.hint = result.first();
      this.seeds = seeds;
    }

    private Value(
        final Point3D delta,
        final Rectangle2D rectangle,
        final double result,
        final Concept<G, M> hint,
        final Map<M, Point3D> seeds)
    {
      super();
      this.delta = delta;
      this.rectangle = rectangle;
      this.result = result;
      this.hint = hint;
      this.seeds = seeds;
    }

    @Override
    public final int compareTo(final Value other)
    {
      return (int) Math.signum(other.result - this.result);
    }
  }

  private final ThreadPoolExecutor     tpe;
  private final ConflictDistance<G, M> conflictDistance;
  private final ConceptLayout<G, M>    layout;
  private final Concept<G, M>          concept;
  private final ConceptMovement  movement;
  private double                       minY;
  private final int                    toSide;
  private final int                    zoomInto;
  private final int                    steps;
  public final DoubleProperty          progressProperty = new SimpleDoubleProperty(0d);
  public Value                         best;
  public final ObservableSet<Value>    values           = FXCollections.observableSet(new HashSet<Value>());
  private final List<Value>            lastValues;
  private final Set<Value>             nextValues       = new HashSet<Value>();
  private final double                 numPerDim;

  public LayoutEvolution(
      final ConceptLayout<G, M> layout,
      final Concept<G, M> concept,
      final ConceptMovement movement,
      final double widthFactor,
      final double heightFactor,
      final int toSide,
      final int steps,
      final int zoomInto,
      final ConflictDistance<G, M> conflictDistance,
      final ThreadPoolExecutor tpe)
  {
    super();
    this.layout = layout;
    this.concept = concept;
    this.movement = movement;
    this.toSide = toSide;
    this.numPerDim = 1d + 2d * toSide;
    this.zoomInto = zoomInto;
    this.steps = steps;
    this.conflictDistance = conflictDistance;
    this.tpe = tpe;
    this.lastValues = new ArrayList<Value>((int) Math.pow(1d + 2d * toSide, 2d));
    final BoundingBox layoutBounds = layout.getCurrentBoundingBox();
    final double width = widthFactor * layoutBounds.getWidth();
    final double height = heightFactor * layoutBounds.getHeight();
    final Point3D origin = layout.positionBinding(concept).getValue();
    final Rectangle2D rectangle =
        new Rectangle2D(origin.getX() - width / 2d, origin.getY() - height / 2d, width, height);
    final Value firstValue =
        new Value(Points.ZERO, rectangle, conflictDistance.apply(layout), layout.clone()._seeds);
    if (concept.equals(layout.lattice.context.selection.topConcept())) {
      best = firstValue;
      return;
    }
    switch (movement) {
      case LABEL_SEED:
        try {
          minY =
              layout
                  .positionBinding(Iterators.getOnlyElement(layout.lattice.row(concept).iterator()))
                  .getValue()
                  .getY()
                  - origin.getY();
        } catch (IllegalArgumentException | NoSuchElementException e) {
          best = firstValue;
          return;
        }
        break;
      case INTENT_SEEDS:
      case INTENT_CHAIN_SEEDS:
      default:
        minY = -origin.getY();
        break;
    }
    values.add(firstValue);
    nextValues.add(firstValue);
  }

  public final Value calculate()
  {
    if (!nextValues.isEmpty())
      for (int step = 0; step < steps; step++)
        zoomIn();
    return best;
  }

  private final void zoomIn()
  {
    final Set<Future<?>> futures = new HashSet<Future<?>>();
    for (Value nextValue : nextValues) {
      values.remove(nextValue);
      final double tileWidth = nextValue.rectangle.getWidth() / numPerDim;
      final double tileHeight = nextValue.rectangle.getHeight() / numPerDim;
      final Value value =
          new Value(nextValue.delta, new Rectangle2D(
              nextValue.rectangle.getMinX() + toSide * tileWidth,
              nextValue.rectangle.getMinY() + toSide * tileHeight,
              tileWidth,
              tileHeight), nextValue.result, nextValue.hint, nextValue.seeds);
      values.add(value);
      lastValues.add(value);
      for (double i = -toSide; i <= toSide; i++)
        for (double j = -toSide; j <= toSide; j++)
          if (i != 0d || j != 0d)
            futures.add(tpe.submit(create(nextValue, i, j, tileWidth, tileHeight)));
    }
    for (Future<?> f : futures)
      try {
        f.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    nextValues.clear();
    Collections.sort(lastValues);
    for (Value nextValue : Iterables.limit(lastValues, zoomInto))
      nextValues.add(nextValue);
    lastValues.clear();
  }

  private final Runnable create(
      final Value nextValue,
      final double i,
      final double j,
      final double tileWidth,
      final double tileHeight)
  {
    return new Runnable()
      {
        @Override
        public void run()
        {
          final Point3D delta = Points.plus(nextValue.delta, i * tileWidth, j * tileHeight);
          if (delta.getY() < minY)
            return;
          final ConceptLayout<G, M> movedLayout = layout.clone();
          movedLayout.move(concept, movement, delta);
          final Value value =
              new Value(delta, new Rectangle2D(
                  nextValue.rectangle.getMinX() + (i + toSide) * tileWidth,
                  nextValue.rectangle.getMinY() + (j + toSide) * tileHeight,
                  tileWidth,
                  tileHeight), conflictDistance.apply(movedLayout), movedLayout._seeds);
          synchronized (values) {
            values.add(value);
          }
          synchronized (lastValues) {
            lastValues.add(value);
          }
//        synchronized (best) {
          if (best == null || value.result >= best.result)
            best = value;
//        }
        }
      };
  }
}
