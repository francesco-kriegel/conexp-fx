package conexp.fx.core.layout;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.Functions;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.math.Points;

public final class ConceptLayout<G, M> implements Observable {

  private boolean                                    observe          = false;
  public final ConceptLattice<G, M>                  lattice;
  public final Map<M, Point3D>                       _seeds           = new ConcurrentHashMap<M, Point3D>();
  public final ObservableMap<M, Point3D>             seeds            = FXCollections.observableMap(_seeds);
  public final Map<M, Point3D>                       seedHistory      = new ConcurrentHashMap<M, Point3D>();
  private final Map<Concept<G, M>, Binding<Point3D>> positionBindings =
                                                                          new ConcurrentHashMap<Concept<G, M>, Binding<Point3D>>();
  public final Map<Concept<G, M>, Point3D>           positions        =
                                                                          Maps
                                                                              .transformValues(
                                                                                  positionBindings,
                                                                                  Functions
                                                                                      .<Point3D> observableValueToCurrentValueFunction());
  public final Map<Concept<G, M>, Concept<G, M>>     generators       =
                                                                          new ConcurrentHashMap<Concept<G, M>, Concept<G, M>>();
  private final Set<InvalidationListener>            listeners        = new HashSet<InvalidationListener>();

  public ConceptLayout(final ConceptLattice<G, M> conceptLattice, final Map<M, Point3D> initialSeeds) {
    super();
    this.lattice = conceptLattice;
    for (final Concept<G, M> concept : conceptLattice.rowHeads())
      putNewPositionBinding(concept);
    if (initialSeeds != null)
      seeds.putAll(initialSeeds);
  }

  public final void observe() {
    if (this.observe)
      return;
    this.observe = true;
    lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
        for (Concept<G, M> concept : event.getRows())
          synchronized (positionBindings) {
            putNewPositionBinding(concept);
          }
      }
    }, RelationEvent.ROWS_ADDED);
    lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
        for (Concept<G, M> concept : event.getRows())
          synchronized (positionBindings) {
            positionBindings.remove(concept).dispose();
          }
      }
    }, RelationEvent.ROWS_REMOVED);
//    seeds.addListener(new MapChangeListener<M, Point3D>() {
//
//      @Override
//      public void onChanged(MapChangeListener.Change<? extends M, ? extends Point3D> change) {
//        System.out.println(seeds.keySet());
//      }
//    });
  }

  public final Binding<Point3D> positionBinding(final Concept<G, M> c) {
    synchronized (positionBindings) {
//      if (!positionBindings.containsKey(c))
//        putNewPositionBinding(c);
      return positionBindings.get(c);
    }
  }

  private final void putNewPositionBinding(final Concept<G, M> concept) {
    positionBindings.put(concept, new ObjectBinding<Point3D>() {

      {
        if (observe)
          bind(seeds, concept.intent());
        else
          bind(seeds);
      }

      public void dispose() {
        if (observe)
          unbind(seeds, concept.intent());
        else
          unbind(seeds);
        super.dispose();
      }

      protected final Point3D computeValue() {
        double x = 0d;
        double y = 0d;
        double z = 0d;
        synchronized (concept.intent()) {
          synchronized (seeds) {
            for (M m : Sets.intersection(seeds.keySet(), concept.intent())) {
              final Point3D seed = seeds.get(m);
              x += seed.getX();
              y += seed.getY();
              z += seed.getZ();
            }
          }
        }
        return new Point3D(x, y, z);
      }
    });
  }

  public final boolean updateSeeds(final Map<M, Point3D> seedUpdates) {
    if (_seeds.equals(seedUpdates))
      return false;
    synchronized (seeds) {
      seeds.clear();
      seeds.putAll(seedUpdates);
    }
    invalidate();
    return true;
  }

  public final void rotate(final double angle) {
    synchronized (seeds) {
      for (Entry<M, Point3D> seed : seeds.entrySet())
        seeds.put(seed.getKey(), Points.rotate(seed.getValue(), angle));
    }
    invalidate();
  }

  @SuppressWarnings("incomplete-switch")
  public final void move(final Concept<G, M> concept, final ConceptMovement movement, final Point3D delta) {
    synchronized (seeds) {
      final double dx = delta.getX();
      final double dy = delta.getY();
      final double dz = delta.getZ();
      switch (movement) {
      case LABEL_SEED:
        try {
          final M affectedSeed =
              Iterators.getOnlyElement(Sets.intersection(lattice.attributeLabels(concept), seeds.keySet()).iterator());
          final Point3D seed = seeds.get(affectedSeed);
          seeds.put(affectedSeed, new Point3D(seed.getX() + dx, Math.max(0.001d, seed.getY() + dy), seed.getZ() + dz));
        } catch (NoSuchElementException | IllegalArgumentException e) {
          System.err.println(e.getStackTrace()[0]
              + " Moving only label seeds, but there was none or more than one! Check this.");
          System.err.println("\t" + Sets.intersection(lattice.attributeLabels(concept), seeds.keySet()));
        }
        break;
      case LABEL_CHAIN_SEEDS:
        try {
          final M m =
              Iterators.getOnlyElement(Sets.intersection(lattice.attributeLabels(concept), seeds.keySet()).iterator());
          final Point3D s = seeds.get(m);
          final HashSet<M> eq = new HashSet<M>(Maps.filterValues(seeds, new Predicate<Point3D>() {

            public final boolean apply(final Point3D p) {
              return s.equals(p);// || s.distance(p) < s.distance(0, 0, 0) / 16d;
            }
          }).keySet());
          final double f = 1d / (double) Sets.intersection(eq, concept.intent()).size();
          final Point3D t = new Point3D(s.getX() + f * dx, Math.max(0.1d, s.getY() + f * dy), s.getZ() + f * dz);
          for (M n : eq)
            seeds.put(n, t);
        } catch (NoSuchElementException | IllegalArgumentException e) {
          System.err.println(e.getStackTrace()[0]
              + " Moving only label seeds, but there was none or more than one! Check this.");
          System.err.println("\t" + Sets.intersection(lattice.attributeLabels(concept), seeds.keySet()));
        }
        break;
      case INTENT_SEEDS:
      case INTENT_CHAIN_SEEDS:
        final Set<M> affectedSeeds = new HashSet<M>(Sets.intersection(concept.intent(), seeds.keySet()));
        final Point3D a = Points.absoluteSum(Collections2.transform(affectedSeeds, new Function<M, Point3D>() {

          public final Point3D apply(final M m) {
            return seeds.get(m);
          }
        }));
        switch (movement) {
        case INTENT_SEEDS:
          for (M m : affectedSeeds) {
            final Point3D s = seeds.get(m);
            final double fx = a.getX() == 0 ? 1d / (double) affectedSeeds.size() : Math.abs(s.getX()) / a.getX();
            final double fy = a.getY() == 0 ? 1d / (double) affectedSeeds.size() : Math.abs(s.getY()) / a.getY();
            final double fz = a.getZ() == 0 ? 1d / (double) affectedSeeds.size() : Math.abs(s.getZ()) / a.getZ();
            final Point3D t = new Point3D(s.getX() + fx * dx, Math.max(0.1d, s.getY() + fy * dy), s.getZ() + fz * dz);
            seeds.put(m, t);
          }
          break;
        case INTENT_CHAIN_SEEDS:
          final double size = affectedSeeds.size();
          while (!affectedSeeds.isEmpty()) {
            final M m = Iterables.getFirst(affectedSeeds, null);
            final Point3D s = seeds.get(m);
            final HashSet<M> eq = new HashSet<M>(Maps.filterValues(seeds, new Predicate<Point3D>() {

              public final boolean apply(final Point3D p) {
                return s.equals(p);// || s.distance(p) < s.distance(0, 0, 0) / 16d;
              }
            }).keySet());
            affectedSeeds.removeAll(eq);
            final double fx = a.getX() == 0 ? 1d / size : Math.abs(s.getX()) / a.getX();
            final double fy = a.getY() == 0 ? 1d / size : Math.abs(s.getY()) / a.getY();
            final double fz = a.getZ() == 0 ? 1d / size : Math.abs(s.getZ()) / a.getZ();
            final Point3D t = new Point3D(s.getX() + fx * dx, Math.max(0.1d, s.getY() + fy * dy), s.getZ() + fz * dz);
            for (M n : eq)
              seeds.put(n, t);
          }
          break;
        }
        break;
      }
    }
    invalidate();
  }

  public synchronized final BoundingBox getCurrentBoundingBox(final boolean hideBottom, final boolean hideTop) {
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
    for (Entry<Concept<G, M>, Point3D> e : positions.entrySet())
      if (!hideBottom || !e.getKey().getIntent().containsAll(lattice.context.colHeads()))
        if (!hideTop || !e.getKey().getExtent().containsAll(lattice.context.rowHeads())) {
          final Point3D p = e.getValue();
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

  public void deleteZ() {
    synchronized (seeds) {
      seeds.putAll(new HashMap<M, Point3D>(Maps.transformValues(seeds, Points.XY_PROJECTION)));
    }
    invalidate();
  }

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

  public final ConceptLayout<G, M> clone() {
    return new ConceptLayout<G, M>(lattice, seeds);
  }

  public boolean equals(Object o) {
    return o != null && o instanceof ConceptLayout && ((ConceptLayout<?, ?>) o).lattice.equals(lattice)
        && ((ConceptLayout<?, ?>) o).seeds.equals(seeds);
  }

  public int hashCode() {
    return 7 * lattice.hashCode() + 13 * seeds.hashCode();
  }
}
