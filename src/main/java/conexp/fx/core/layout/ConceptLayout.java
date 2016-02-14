package conexp.fx.core.layout;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;

public abstract class ConceptLayout<G, M> implements Observable {

  protected boolean                                    observe          = false;
  public final ConceptLattice<G, M>                    lattice;
  protected final Map<Concept<G, M>, Binding<Point3D>> positionBindings =
      new ConcurrentHashMap<Concept<G, M>, Binding<Point3D>>();
//  public final Map<Concept<G, M>, Point3D>          positions        =
//      Maps.transformValues(positionBindings, GuavaFunctions.<Point3D> observableValueToCurrentValueFunction());
  public final Map<Concept<G, M>, Concept<G, M>>       generators       =
      new ConcurrentHashMap<Concept<G, M>, Concept<G, M>>();

  protected ConceptLayout(final ConceptLattice<G, M> conceptLattice) {
    super();
    this.lattice = conceptLattice;
  }

  public final void observe() {
    if (true)
      return;
    if (this.observe)
      return;
    this.observe = true;
    lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
        for (Concept<G, M> concept : event.getRows())
//          synchronized (positionBindings) {
          getOrAddPosition(concept);
//          }
      }
    }, RelationEvent.ROWS_ADDED);
    lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
        for (Concept<G, M> concept : event.getRows())
          disposePosition(concept);
//          synchronized (positionBindings) {
//            final Binding<Point3D> posb = positionBindings.remove(concept);
//            try {
//              posb.dispose();
//            } catch (NullPointerException e) {
//              System.err.println("position binding not found: " + concept);
//            }
//          }
      }
    }, RelationEvent.ROWS_REMOVED);
    // seeds.addListener(new MapChangeListener<M, Point3D>() {
    //
    // @Override
    // public void onChanged(MapChangeListener.Change<? extends M, ? extends Point3D> change) {
    // System.out.println(seeds.keySet());
    // }
    // });
  }

  public final Binding<Point3D> getOrAddPosition(final Concept<G, M> concept) {
    synchronized (positionBindings) {
      return positionBindings.computeIfAbsent(concept, this::newPositionBinding);
    }
  }

  protected abstract Binding<Point3D> newPositionBinding(Concept<G, M> concept);

  public final Binding<Point3D> getPosition(final Concept<G, M> c) {
    synchronized (positionBindings) {
      // could possibly removed but then the position binding for bottom concept in interordinal scales could not be
      // found.
//      if (!positionBindings.containsKey(c))
//        putNewPositionBinding(c);
      return positionBindings.get(c);
    }
  }

  public final void disposePosition(final Concept<G, M> concept) {
    synchronized (positionBindings) {
      positionBindings.remove(concept).dispose();
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
      for (Entry<Concept<G, M>, Binding<Point3D>> e : positionBindings.entrySet())
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
