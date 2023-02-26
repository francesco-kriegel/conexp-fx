package conexp.fx.core.layout;
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

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Maps;

import conexp.fx.core.collections.ListIterators;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.math.Points;
import javafx.beans.binding.Binding;
import javafx.geometry.Point3D;

public abstract class QualityMeasure<G, M, V> implements Function<AdditiveConceptLayout<G, M>, V> {

//    implements com.google.common.base.Function<AdditiveConceptLayout<G, M>, V>,
//    java.util.function.Function<AdditiveConceptLayout<G, M>, V> {
//
//  private final Map<Pair<Map<G, Point3D>, Map<M, Point3D>>, V> cache = new ConcurrentHashMap<>();
//
//  public final V apply(final AdditiveConceptLayout<G, M> layout) {
//    return cache.computeIfAbsent(Pair.of(layout._seedsG, layout._seedsM), __ -> compute(layout));
//  }
//
  @Override
  public V apply(AdditiveConceptLayout<G, M> t) {
    return compute(t);
  }

  protected abstract V compute(AdditiveConceptLayout<G, M> layout);

  public static final <G, M, V> QualityMeasure<G, M, V>
      of(final java.util.function.Function<AdditiveConceptLayout<G, M>, V> f) {
    return new QualityMeasure<G, M, V>() {

      @Override
      protected V compute(AdditiveConceptLayout<G, M> layout) {
        return f.apply(layout);
      }
    };
  }

  @SafeVarargs
  public static final <G, M> QualityMeasure<G, M, Double>
      linearCombination(final Pair<Double, QualityMeasure<G, M, Double>>... weightedMeasures) {
    return of(
        layout -> Stream
            .of(weightedMeasures)
            .map(p -> p.first() * p.second().apply(layout))
            .reduce(Double::sum)
            .orElse(0d));
  }

  public static final <G, M> QualityMeasure<G, M, Double> topBottomXDistance() {
    return of(
        layout -> 1d - (Math.abs(
            layout.getOrAddPosition(layout.lattice.context.topConcept()).getValue().getX()
                - layout.getOrAddPosition(layout.lattice.context.bottomConcept()).getValue().getX())
            / layout.getCurrentBoundingBox(false, false).getWidth()));
  }

  public static final <G, M> QualityMeasure<G, M, Double> heightWidthRatio() {
    return of(layout -> {
      final double h = layout.getCurrentBoundingBox(false, false).getHeight();
      final double w = layout.getCurrentBoundingBox(false, false).getWidth();
      final double min = Math.min(h, w);
      final double max = Math.max(h, w);
      return min / max;
    });
  }

  public static final <G, M> QualityMeasure<G, M, Integer> edgeIntersections() {
    return of(layout -> {
      int intersections = 0;
      for (Pair<Pair<Concept<G, M>, Concept<G, M>>, Pair<Concept<G, M>, Concept<G, M>>> e : ListIterators
          .upperCartesianDiagonalStrict(layout.lattice))
        if (!e.first().x().equals(e.second().y()) && !e.first().y().equals(e.second().x()))
          if (Points.intersectingLineSegments(
              layout.getOrAddPosition(e.first().x()).getValue(),
              layout.getOrAddPosition(e.first().y()).getValue(),
              layout.getOrAddPosition(e.second().x()).getValue(),
              layout.getOrAddPosition(e.second().y()).getValue()))
            intersections++;
      return intersections;
    });
  }

  public static final <G, M> QualityMeasure<G, M, Pair<Concept<G, M>, Double>> conflictDistance() {
    return of(layout -> {
      double num = 0d;
      double minC = 1d;
      double sumC = 0d;
      double minL = Double.MAX_VALUE;
      double sumL = 0d;
      final Concept<G, M> bottom = layout.lattice.context.selection.bottomConcept();
      Concept<G, M> faulty = bottom;
      for (Entry<Concept<G, M>, Binding<Point3D>> entry : Maps
          .asMap(layout.lattice.rowHeads(), layout::getOrAddPosition)
          .entrySet()) {
        // layout.positionBindings.entrySet())
        // //
        // {
        if (!entry.getKey().equals(bottom)) {
          final Point3D p = entry.getValue().getValue();
          final Concept<G, M> c = entry.getKey();
          for (Pair<Concept<G, M>, Concept<G, M>> edge : layout.lattice)
            if (!edge.x().equals(bottom) && !c.equals(edge.x()) && !c.equals(edge.y())) {
              final Point3D q1 = layout.getOrAddPosition(edge.x()).getValue();
              final Point3D q2 = layout.getOrAddPosition(edge.y()).getValue();
              final double length = q1.distance(q2);
              final double conflictDistance = Math.min(Points.pointSegmentDistance(p, q1, q2) / length, 0.5d) * 2d;
              num++;
              minL = Math.min(minL, length);
              sumL += length;
              sumC += conflictDistance;
              if (conflictDistance < minC) {
                minC = conflictDistance;
                faulty = c;
              }
            }
        }
      }
      final double avgC = num == 0d ? 1d : (sumC / num);
      final double avgL = num == 0d ? 1d : (sumL / num);
      return Pair.of(faulty, Math.pow(minC * Math.pow(minC / avgC, 0.5d) * Math.pow(minL / avgL, 0.25d), 0.125d));
    });
  }

  public static final <G, M> QualityMeasure<G, M, Integer> parallelEdges() {
    return of(layout -> {
      return 0;
    });
  }

  public static final <G, M> QualityMeasure<G, M, Integer> distinctDirections() {
    return of(layout -> {
      return 0;
    });
  }

  public static final <G, M> QualityMeasure<G, M, Integer> distinctAngles() {
    return of(layout -> {
      return 0;
    });
  }
}
