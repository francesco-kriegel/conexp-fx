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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValue;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;

public abstract class ConceptLayout<G, M, P extends ObservableValue<Point3D>> implements Observable {

  protected boolean                              observe          = false;
  public final ConceptLattice<G, M>              lattice;
  protected final Map<Concept<G, M>, P>          positionBindings = new ConcurrentHashMap<Concept<G, M>, P>();
  public final Map<Concept<G, M>, Concept<G, M>> generators       =
      new ConcurrentHashMap<Concept<G, M>, Concept<G, M>>();

  protected ConceptLayout(final ConceptLattice<G, M> conceptLattice) {
    super();
    this.lattice = conceptLattice;
  }

  public final void observe() {
    if (this.observe)
      return;
    this.observe = true;
    // TODO synchronized(positionBindings)
    lattice.addEventHandler(e -> e.getRows().forEach(this::getOrAddPosition), RelationEvent.ROWS_ADDED);
    lattice.addEventHandler(e -> e.getRows().forEach(this::disposePosition), RelationEvent.ROWS_REMOVED);
  }

  public final P getOrAddPosition(final Concept<G, M> concept) {
    synchronized (positionBindings) {
      return positionBindings.computeIfAbsent(concept, this::newPositionBinding);
    }
  }

  protected abstract P newPositionBinding(Concept<G, M> concept);

  public final P getPosition(final Concept<G, M> c) {
    synchronized (positionBindings) {
      return positionBindings.get(c);
    }
  }

  public final void disposePosition(final Concept<G, M> concept) {
    synchronized (positionBindings) {
      final P p = positionBindings.remove(concept);
      if (p instanceof Binding)
        ((Binding<Point3D>) p).dispose();
    }
  }

  protected final void initializePositionBindings() {
    for (final Concept<G, M> concept : lattice.rowHeads())
      getOrAddPosition(concept);
  }

  public abstract void rotate(double angle);

  public abstract void move(Concept<G, M> concept, ConceptMovement movement, Point3D delta);

  public abstract void deleteZ();

  public final BoundingBox getCurrentBoundingBox(final boolean hideBottom, final boolean hideTop) {
    synchronized (positionBindings) {
      double xmin = Double.MAX_VALUE;
      double xmax = Double.MIN_VALUE;
      double ymin = Double.MAX_VALUE;
      double ymax = Double.MIN_VALUE;
      double zmin = Double.MAX_VALUE;
      double zmax = Double.MIN_VALUE;
//    final java.util.function.Predicate<Entry<Concept<G, M>, Point3D>> pred;
//    if (hideBottom && hideTop)
//      pred =
//          e -> !e.getKey().getExtent().containsAll(lattice.context.rowHeads())
//              && !e.getKey().getIntent().containsAll(lattice.context.colHeads());
//    else if (hideBottom)
//      pred =
//          e -> !e.getKey().getExtent().containsAll(lattice.context.rowHeads())
//              && !e.getKey().getIntent().containsAll(lattice.context.colHeads());
//    else if (hideTop)
//      pred =
//          e -> !e.getKey().getExtent().containsAll(lattice.context.rowHeads())
//              && !e.getKey().getIntent().containsAll(lattice.context.colHeads());
//    else
//      pred = e -> true;
//    positions
//        .entrySet()
//        .parallelStream()
//        .filter(pred)
//        .map(e -> e.getValue())
//        .reduce(
//            Pair.of(new double[] { Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE }, new double[] {
//                Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE }),
//            (a, p) -> Pair.of(new double[] {}, new double[] {}),
//            (a, b) -> Pair.of(null, null));
      for (Entry<Concept<G, M>, P> e : positionBindings.entrySet())
        if (!hideBottom || !e.getKey().getIntent().containsAll(lattice.context.colHeads()))
          if (!hideTop || !e.getKey().getExtent().containsAll(lattice.context.rowHeads())) {
            final Point3D p = e.getValue().getValue();
            xmin = Math.min(xmin, p.getX());
            xmax = Math.max(xmax, p.getX());
            ymin = Math.min(ymin, p.getY());
            ymax = Math.max(ymax, p.getY());
            zmin = Math.min(zmin, p.getZ());
            zmax = Math.max(zmax, p.getZ());
          }
      final double dx = xmax - xmin;
      final double dy = ymax - ymin;
      final double dz = zmax - zmin;
      return new BoundingBox(xmin, ymin, zmin, dx, dy, dz);
    }
  }

  private final Set<InvalidationListener> listeners = new HashSet<InvalidationListener>();

  public final void invalidate() {
    synchronized (listeners) {
      for (InvalidationListener listener : listeners)
        listener.invalidated(this);
    }
  }

  public final void addListener(final InvalidationListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  public final void removeListener(final InvalidationListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

}
