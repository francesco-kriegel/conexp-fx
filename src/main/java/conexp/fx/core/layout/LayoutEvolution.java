package conexp.fx.core.layout;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2023 Francesco Kriegel
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.gui.task.TimeTask;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;

public final class LayoutEvolution<G, M> {

  public static final <G, M> TimeTask<Void> calculate(final LayoutEvolution<G, M> qualityChart) {
    return new TimeTask<Void>("Quality Measure Chart") {

      @Override
      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateMessage("Simulating Concept Movement...");
        qualityChart.progressProperty.addListener(new ChangeListener<Number>() {

          @Override
          public final void changed(
              final ObservableValue<? extends Number> observable,
              final Number oldValue,
              final Number newValue) {
            updateProgress(newValue.doubleValue(), 1d);
          }
        });
        qualityChart.calculate();
        updateProgress(1d, 1d);
        return null;
      }
    };
  }

  public final class Value implements Comparable<Value> {

    public final Point3D         delta;
    public final Rectangle2D     rectangle;
    public final double          result;
    public final Concept<G, M>   hint;
    public final Map<G, Point3D> seedsG;
    public final Map<M, Point3D> seedsM;

    private Value(
        final Point3D delta,
        final Rectangle2D rectangle,
        final Pair<Concept<G, M>, Double> result,
        final Map<G, Point3D> seedsG,
        final Map<M, Point3D> seedsM) {
      this(delta, rectangle, result.second(), result.first(), seedsG, seedsM);
    }

    private Value(
        final Point3D delta,
        final Rectangle2D rectangle,
        final double result,
        final Concept<G, M> hint,
        final Map<G, Point3D> seedsG,
        final Map<M, Point3D> seedsM) {
      super();
      this.delta = delta;
      this.rectangle = rectangle;
      this.result = result;
      this.hint = hint;
      this.seedsG = seedsG;
      this.seedsM = seedsM;
    }

    @Override
    public final int compareTo(final Value other) {
      return (int) Math.signum(other.result - this.result);
    }
  }

  private final ExecutorService                                   tpe;
  private final QualityMeasure<G, M, Pair<Concept<G, M>, Double>> conflictDistance;
  private final AdditiveConceptLayout<G, M>                       layout;
  private final Concept<G, M>                                     concept;
  private final ConceptMovement                                   movement;
  private double                                                  minY;
  private final int                                               toSide;
  private final int                                               zoomInto;
  private final int                                               steps;
  public final DoubleProperty                                     progressProperty = new SimpleDoubleProperty(0d);
  public Value                                                    best;
  public final ObservableSet<Value>                               values           =
      FXCollections.observableSet(new HashSet<Value>());
  private final List<Value>                                       lastValues;
  private final Set<Value>                                        nextValues       = new HashSet<Value>();
  private final double                                            numPerDim;

  public LayoutEvolution(
      final AdditiveConceptLayout<G, M> layout,
      final Concept<G, M> concept,
      final ConceptMovement movement,
      final double widthFactor,
      final double heightFactor,
      final int toSide,
      final int steps,
      final int zoomInto,
      final QualityMeasure<G, M, Pair<Concept<G, M>, Double>> conflictDistance,
      final ExecutorService tpe) {
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
    final BoundingBox layoutBounds = layout.getCurrentBoundingBox(false, false);
    final double width = widthFactor * layoutBounds.getWidth();
    final double height = heightFactor * layoutBounds.getHeight();
    final Point3D origin = layout.getPosition(concept).getValue();
    final Rectangle2D rectangle =
        new Rectangle2D(origin.getX() - width / 2d, origin.getY() - height / 2d, width, height);
    final Value firstValue = new Value(
        Point3D.ZERO,
        rectangle,
        conflictDistance.apply(layout),
        //        layout.clone()._seedsG,
        //        layout.clone()._seedsM);
        layout.clone().seedsG,
        layout.clone().seedsM);
    if (concept.equals(layout.lattice.context.selection.topConcept())) {
      best = firstValue;
      return;
    }
    switch (movement) {
    case LABEL_SEED:
      try {
        minY = layout.getPosition(Iterators.getOnlyElement(layout.lattice.row(concept).iterator())).getValue().getY()
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

  public final Value calculate() {
    if (!nextValues.isEmpty())
      for (int step = 0; step < steps; step++)
        zoomIn();
    return best;
  }

  private final void zoomIn() {
    final Set<Future<?>> futures = new HashSet<Future<?>>();
    for (Value nextValue : nextValues) {
      values.remove(nextValue);
      final double tileWidth = nextValue.rectangle.getWidth() / numPerDim;
      final double tileHeight = nextValue.rectangle.getHeight() / numPerDim;
      final Value value = new Value(
          nextValue.delta,
          new Rectangle2D(
              nextValue.rectangle.getMinX() + toSide * tileWidth,
              nextValue.rectangle.getMinY() + toSide * tileHeight,
              tileWidth,
              tileHeight),
          nextValue.result,
          nextValue.hint,
          nextValue.seedsG,
          nextValue.seedsM);
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

  private final Runnable
      create(final Value nextValue, final double i, final double j, final double tileWidth, final double tileHeight) {
    return new Runnable() {

      @Override
      public void run() {
        final Point3D delta = nextValue.delta.add(i * tileWidth, j * tileHeight, 0);
        if (delta.getY() < minY)
          return;
        final AdditiveConceptLayout<G, M> movedLayout = layout.clone();
        movedLayout.move(concept, movement, delta);
        final Value value = new Value(
            delta,
            new Rectangle2D(
                nextValue.rectangle.getMinX() + (i + toSide) * tileWidth,
                nextValue.rectangle.getMinY() + (j + toSide) * tileHeight,
                tileWidth,
                tileHeight),
            conflictDistance.apply(movedLayout),
            //            movedLayout._seedsG,
            //            movedLayout._seedsM);
            movedLayout.seedsG,
            movedLayout.seedsM);
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
