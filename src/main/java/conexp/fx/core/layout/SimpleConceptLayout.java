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

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import conexp.fx.core.collections.ListIterators;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.math.Points;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;

public class SimpleConceptLayout<G, M> extends ConceptLayout<G, M, SimpleObjectProperty<Point3D>> {

  public SimpleConceptLayout(ConceptLattice<G, M> conceptLattice) {
    super(conceptLattice);
    start();
  }

  @Override
  protected SimpleObjectProperty<Point3D> newPositionBinding(Concept<G, M> concept) {
    return new SimpleObjectProperty<Point3D>(Point3D.ZERO);
  }

  @Override
  public void rotate(double angle) {
    // TODO Auto-generated method stub
  }

  @Override
  public void move(Concept<G, M> concept, ConceptMovement movement, Point3D delta) {
    // TODO Auto-generated method stub
    synchronized (moves) {
      moves.offer(Pair.of(concept, delta));
    }
//    final SimpleObjectProperty<Point3D> p = getOrAddPosition(concept);
//    double y = Double.MAX_VALUE;
//    for (Concept<G, M> upper : lattice.upperNeighbors(concept)) {
//      final SimpleObjectProperty<Point3D> q = getOrAddPosition(upper);
//      y = Math.min(y, q.getValue().subtract(p.getValue()).getY());
//    }
//    p.setValue(p.getValue().add(new Point3D(delta.getX(), Math.min(delta.getY(), -y), delta.getZ())));
//    p.setValue(p.getValue().add(delta));
//    final Set<Concept<G, M>> filter = lattice.filter(concept);
//    final double factor = 1d / (double) filter.size();
//    filter.forEach(upper -> {
//      final SimpleObjectProperty<Point3D> p = getOrAddPosition(upper);
//      p.setValue(p.getValue().add(delta.multiply(factor)));
//    });
//    invalidate();
  }

  private final void _move(final Concept<G, M> concept, final Point3D delta) {
    final SimpleObjectProperty<Point3D> p = getOrAddPosition(concept);
    p.setValue(p.getValue().add(delta));
    invalidate();
  }

  @Override
  public void deleteZ() {
    // TODO Auto-generated method stub
  }

  private final Queue<Pair<Concept<G, M>, Point3D>> moves = new ConcurrentLinkedQueue<>();
  private Thread                                    thread;
  private final long                                delay = 400l;

  public final void start() {
    synchronized (this) {
      if (thread == null) {
        thread = new Thread(() -> {
          try {
            while (true) {
//              if (ConExpFX.instance.executor.isIdleBinding.get())
              evolveLayout();
              Thread.sleep(delay);
            }
          } catch (InterruptedException e) {
            System.err.println("Force interrupted.");
          }
        }, SimpleConceptLayout.class.getName());
        thread.start();
      }
    }
  }

  public final void stop() {
    synchronized (this) {
      if (thread != null) {
        thread.interrupt();
        thread = null;
      }
    }
  }

  private final void evolveLayout() {
//    normalizeSeeds();
//    double size = getMaximalNodeDistance();
//    if (size == 0d)
//      size = 1d;
    final double k = getK();
    final Map<Concept<G, M>, Point3D> deltas = new ConcurrentHashMap<>();
    lattice.rowHeads().forEach(c -> deltas.put(c, Point3D.ZERO));
    synchronized (positionBindings) {
      computeAdjacentNodeForces(k, deltas);
      computeNodeForces(k, deltas);
//    computeNodeEdgeForces(k, deltas);
//    adjustWH(deltas);
      synchronized (moves) {
        for (Pair<Concept<G, M>, Point3D> move = moves.poll(); move != null; move = moves.poll()) {
          final Pair<Concept<G, M>, Point3D> _move = move;
          deltas.compute(_move.x(), (c, d) -> d.add(_move.y()));
        }
      }
      deltas.replaceAll((c, d) -> {
        if (Double.isFinite(d.getX()) && Double.isFinite(d.getY()) && Double.isFinite(d.getZ()))
          return d;
        else
          return new Point3D(Math.random(), Math.random(), 0d).normalize();
      });
      deltas.forEach((concept, delta) -> _move(concept, delta));
    }
  }

  private final double getMaximalNodeDistance() {
    double size = 0d;
    for (Concept<G, M> c1 : lattice.rowHeads())
      for (Concept<G, M> c2 : lattice.rowHeads()) {
        size = Math.max(size, getPosition(c1).getValue().subtract(getPosition(c2).getValue()).magnitude());
      }
    return size;
  }

  private final double getK() {
    final BoundingBox box = getCurrentBoundingBox(false, false);
    final double area = box.getWidth() * box.getHeight();
    final double n = lattice.rowHeads().size();
    return Math.sqrt(area / n);
  }

  private final void adjustWH(final Map<Concept<G, M>, Point3D> deltas) {
    final BoundingBox box = getCurrentBoundingBox(false, false);
    final double wh = box.getWidth() / box.getHeight();
    deltas.replaceAll((__, delta) -> new Point3D(delta.getX(), delta.getY() * wh, delta.getZ()));
  }

  private final void computeAdjacentNodeForces(final double k, final Map<Concept<G, M>, Point3D> deltas) {
    for (Pair<Concept<G, M>, Concept<G, M>> edge : lattice)
//      if (!edge.x().extent().containsAll(context.rowHeads())
//          && !edge.x().intent().containsAll(context.colHeads())
//          && !edge.y().extent().containsAll(context.rowHeads())
//          && !edge.y().intent().containsAll(context.colHeads())) 
    {
      Point3D delta1 = deltas.get(edge.x());
      Point3D delta2 = deltas.get(edge.y());
      final Point3D q1 = getOrAddPosition(edge.x()).getValue();
      final Point3D q2 = getOrAddPosition(edge.y()).getValue();
      final Point3D q = q2.subtract(q1);
      final double x = Math.log1p(q.magnitude()) / k;
//      final double x = Math.pow(q.magnitude(), 2d) / k;
      final Point3D q0 = q.normalize().multiply(x);
      delta1 = delta1.add(q0);
      delta2 = delta2.subtract(q0);
      if (q.getY() > 0) {
        final double dx = -q.getY();
        delta2.subtract(0d, dx, 0d);
        delta1.add(0d, dx, 0d);
      }
      deltas.put(edge.x(), delta1);
      deltas.put(edge.y(), delta2);
    }
  }

  private final void computeNodeForces(final double k, final Map<Concept<G, M>, Point3D> deltas) {
    for (Pair<Concept<G, M>, Concept<G, M>> pair : ListIterators.upperCartesianDiagonalStrict(lattice.rowHeads())) {
      Point3D delta1 = deltas.get(pair.x());
      Point3D delta2 = deltas.get(pair.y());
      final Point3D q1 = getOrAddPosition(pair.x()).getValue();
      final Point3D q2 = getOrAddPosition(pair.y()).getValue();
      final Point3D q = q2.subtract(q1);
//    if pair.x is a subconcept of pair.y, then q1 must be below q2, i.e., q1.y>q2.y
//      if this is not the case, we decrease q2.y and increase q1.y
//      if () {// || q.getY() > 0.8d * q.magnitude()) {
//      final int c = lattice.contains(pair.x(), pair.y()) ? -1 : lattice.contains(pair.y(), pair.x()) ? 1 : 0;
//      if (c != 0&&q.getY()<0) {
//      final int sx = pair.x().extent().size();
//      final int sy = pair.y().extent().size();
//      final double df = sx == sy || q.getY() == 0d ? 0d : sx < sy == q.getY() < 0d ? 0d : 1d;
//      final double dy = df * Math.abs(q.getY());
////      final double dy = df * Math.log1p(1d + Math.abs(q.getY()));
//      delta1 = delta1.add(0d, dy, 0d);
//      delta2 = delta2.subtract(0d, dy, 0d);
//      }
//      }
      final double x = Math.pow(k, 2d) / q.magnitude();
      final Point3D q0 = q.normalize().multiply(x);
      delta1 = delta1.subtract(q0);
      delta2 = delta2.add(q0);
      deltas.put(pair.x(), delta1);
      deltas.put(pair.y(), delta2);
    }
  }

  private final void computeNodeEdgeForces(final double k, final Map<Concept<G, M>, Point3D> deltas) {
    for (Concept<G, M> concept : lattice.rowHeads())
    // if (!concept.extent().containsAll(context.rowHeads())
    // && !concept.intent().containsAll(context.colHeads()))
    {
      Point3D delta = deltas.get(concept);
      final Point3D p = getOrAddPosition(concept).getValue();
      for (Pair<Concept<G, M>, Concept<G, M>> edge : lattice)
        if (!edge.x().equals(concept) && !edge.y().equals(concept)) {
          Point3D delta1 = deltas.get(edge.x());
          Point3D delta2 = deltas.get(edge.y());
          final Point3D q1 = getOrAddPosition(edge.x()).getValue();
          final Point3D q2 = getOrAddPosition(edge.y()).getValue();
          final Point3D q = Points.shortestVectorFromLineSegment(p, q1, q2);
          final double x = Math.pow(k, 2d) / Math.pow(q.magnitude(), 2d);
          final Point3D q0 = q.normalize().multiply(x);
          delta1 = delta1.add(q0.multiply(-0.5d));
          delta2 = delta2.add(q0.multiply(-0.5d));
          delta = delta.add(q0.multiply(1d));
          deltas.put(edge.x(), delta1);
          deltas.put(edge.y(), delta2);
        }
      deltas.put(concept, delta);
    }
  }

}
