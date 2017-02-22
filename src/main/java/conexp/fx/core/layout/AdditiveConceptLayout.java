package conexp.fx.core.layout;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.math.Points;
import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Point3D;

public final class AdditiveConceptLayout<G, M> extends ConceptLayout<G, M, Binding<Point3D>> {

  public enum Type {
    ATTRIBUTE("Attribute-Additive"),
    OBJECT("Object-Additive"),
    HYBRID("Hybrid-Additive");

    private final String title;

    private Type(final String title) {
      this.title = title;
    }

    @Override
    public final String toString() {
      return title;
    }
  }

  public final ObservableMap<G, Point3D> seedsG       = FXCollections.observableMap(new ConcurrentHashMap<>());
  public final Map<G, Point3D>           seedHistoryG = new ConcurrentHashMap<G, Point3D>();

  public final ObservableMap<M, Point3D> seedsM       = FXCollections.observableMap(new ConcurrentHashMap<>());
  public final Map<M, Point3D>           seedHistoryM = new ConcurrentHashMap<M, Point3D>();

  private final Property<Type>           type;

  public AdditiveConceptLayout(
      final ConceptLattice<G, M> conceptLattice,
      final @Nullable Map<G, Point3D> initialSeedsG,
      final @Nullable Map<M, Point3D> initialSeedsM,
      final Type type) {
    super(conceptLattice);
    this.type = new SimpleObjectProperty<>(type);
    initializePositionBindings();
    if (initialSeedsG != null)
      seedsG.putAll(initialSeedsG);
    if (initialSeedsM != null)
      seedsM.putAll(initialSeedsM);
  }

  public final void setType(final Type type) {
    this.type.setValue(type);
  }

  public final void bindType(final ObservableValue<Type> observable) {
    this.type.bind(observable);
  }

  protected final Binding<Point3D> newPositionBinding(final Concept<G, M> concept) {
//    synchronized (positionBindings) {
//      positionBindings.put(concept, 
    return new ObjectBinding<Point3D>() {

      {
        if (observe)
          bind(seedsG, concept.extent(), seedsM, concept.intent(), type);
        else
          bind(seedsG, seedsM, type);
      }

      public final void dispose() {
        if (observe)
          unbind(seedsG, concept.extent(), seedsM, concept.intent(), type);
        else
          unbind(seedsG, seedsM, type);
        super.dispose();
      }

      protected final Point3D computeValue() {
        double x = 0d;
        double y = 0d;
        double z = 0d;
        if (type.getValue().equals(Type.HYBRID) || type.getValue().equals(Type.OBJECT))
          synchronized (concept.extent()) {
            synchronized (seedsG) {
              for (G g : Sets.difference(seedsG.keySet(), concept.extent())) {
                final Point3D seed = seedsG.get(g);
                x += seed.getX();
                y += seed.getY();
                z += seed.getZ();
              }
            }
          }
        if (type.getValue().equals(Type.HYBRID) || type.getValue().equals(Type.ATTRIBUTE))
          synchronized (concept.intent()) {
            synchronized (seedsM) {
              for (M m : Sets.intersection(seedsM.keySet(), concept.intent())) {
                final Point3D seed = seedsM.get(m);
                x += seed.getX();
                y += seed.getY();
                z += seed.getZ();
              }
            }
          }
        return new Point3D(x, y, z);
      }
//      })
    };
  }

  public final boolean
      updateSeeds(final @Nullable Map<G, Point3D> seedUpdatesG, final @Nullable Map<M, Point3D> seedUpdatesM) {
    boolean ret = false;
    if (!seedsG.equals(seedUpdatesG))
      synchronized (seedsG) {
        ret = true;
        seedsG.clear();
        if (seedUpdatesG != null)
          seedsG.putAll(seedUpdatesG);
      }
    if (!seedsM.equals(seedUpdatesM))
      synchronized (seedsM) {
        ret = true;
        seedsM.clear();
        if (seedUpdatesM != null)
          seedsM.putAll(seedUpdatesM);
      }
    invalidate();
    return ret;
  }

  public final void normalizeSeeds() {
    final double l = 1d / Stream
        .concat(seedsG.values().stream(), seedsM.values().stream())
        .map(Point3D::magnitude)
        .reduce(Math::min)
        .orElse(1d);
    seedsG.replaceAll((g, seed) -> seed.multiply(l));
    seedsM.replaceAll((m, seed) -> seed.multiply(l));
  }

  public final void rotate(final double angle) {
    synchronized (seedsG) {
      seedsG.replaceAll((__, seed) -> Points.rotate(seed, angle));
//      for (Entry<G, Point3D> seed : seedsG.entrySet())
//        seedsG.put(seed.getKey(), Points.rotate(seed.getValue(), angle));
    }
    synchronized (seedsM) {
      seedsM.replaceAll((__, seed) -> Points.rotate(seed, angle));
//      for (Entry<M, Point3D> seed : seedsM.entrySet())
//        seedsM.put(seed.getKey(), Points.rotate(seed.getValue(), angle));
    }
    invalidate();
  }

  @SuppressWarnings("incomplete-switch")
  public final void move(final Concept<G, M> concept, final ConceptMovement movement, final Point3D delta) {
    synchronized (seedsG) {
      synchronized (seedsM) {
        final double dx = delta.getX();
        final double dy = delta.getY();
        final double dz = delta.getZ();
        switch (movement) {
        case LABEL_SEED:
          try {
            final M affectedSeed = Iterators
                .getOnlyElement(Sets.intersection(lattice.attributeLabels(concept), seedsM.keySet()).iterator());
            final Point3D seed = seedsM.get(affectedSeed);
            seedsM
                .put(affectedSeed, new Point3D(seed.getX() + dx, Math.max(0.001d, seed.getY() + dy), seed.getZ() + dz));
          } catch (NoSuchElementException | IllegalArgumentException e) {
            System.err.println(
                e.getStackTrace()[0] + " Moving only label seeds, but there was none or more than one! Check this.");
            System.err.println("\t" + Sets.intersection(lattice.attributeLabels(concept), seedsM.keySet()));
          }
          break;
        case LABEL_CHAIN_SEEDS:
          try {
            final M m = Iterators
                .getOnlyElement(Sets.intersection(lattice.attributeLabels(concept), seedsM.keySet()).iterator());
            final Point3D s = seedsM.get(m);
            final HashSet<M> eq = new HashSet<M>(Maps.filterValues(seedsM, new Predicate<Point3D>() {

              public final boolean apply(final Point3D p) {
                return s.equals(p);// || s.distance(p) < s.distance(0, 0, 0) / 16d;
              }
            }).keySet());
            final double f = 1d / (double) Sets.intersection(eq, concept.intent()).size();
            final Point3D t = new Point3D(s.getX() + f * dx, Math.max(0.1d, s.getY() + f * dy), s.getZ() + f * dz);
            for (M n : eq)
              seedsM.put(n, t);
          } catch (NoSuchElementException | IllegalArgumentException e) {
            System.err.println(
                e.getStackTrace()[0] + " Moving only label seeds, but there was none or more than one! Check this.");
            System.err.println("\t" + Sets.intersection(lattice.attributeLabels(concept), seedsM.keySet()));
          }
          break;
        case INTENT_SEEDS:
        case INTENT_CHAIN_SEEDS:
          final Set<M> affectedSeedsM = new HashSet<M>(Sets.intersection(seedsM.keySet(), concept.intent()));
          final Set<G> affectedSeedsG = new HashSet<G>(Sets.difference(seedsG.keySet(), concept.extent()));
          final Point3D a = Points.absoluteSum(Collections2.transform(affectedSeedsG, seedsG::get)).add(
              Points.absoluteSum(Collections2.transform(affectedSeedsM, seedsM::get)));
          switch (movement) {
          case INTENT_SEEDS:
            if (type.getValue().equals(Type.HYBRID) || type.getValue().equals(Type.OBJECT))
              for (G g : affectedSeedsG) {
                final Point3D s = seedsG.get(g);
                final double fx = a.getX() == 0 ? 1d / (double) affectedSeedsG.size() : Math.abs(s.getX()) / a.getX();
                final double fy = a.getY() == 0 ? 1d / (double) affectedSeedsG.size() : Math.abs(s.getY()) / a.getY();
                final double fz = a.getZ() == 0 ? 1d / (double) affectedSeedsG.size() : Math.abs(s.getZ()) / a.getZ();
                final Point3D t =
                    new Point3D(s.getX() + fx * dx, Math.max(0.1d, s.getY() + fy * dy), s.getZ() + fz * dz);
                seedsG.put(g, t);
              }
            if (type.getValue().equals(Type.HYBRID) || type.getValue().equals(Type.ATTRIBUTE))
              for (M m : affectedSeedsM) {
                final Point3D s = seedsM.get(m);
                final double fx = a.getX() == 0 ? 1d / (double) affectedSeedsM.size() : Math.abs(s.getX()) / a.getX();
                final double fy = a.getY() == 0 ? 1d / (double) affectedSeedsM.size() : Math.abs(s.getY()) / a.getY();
                final double fz = a.getZ() == 0 ? 1d / (double) affectedSeedsM.size() : Math.abs(s.getZ()) / a.getZ();
                final Point3D t =
                    new Point3D(s.getX() + fx * dx, Math.max(0.1d, s.getY() + fy * dy), s.getZ() + fz * dz);
                seedsM.put(m, t);
              }
            break;
          case INTENT_CHAIN_SEEDS:
            final double size;
            switch (type.getValue()) {
            case HYBRID:
              size = affectedSeedsG.size() + affectedSeedsM.size();
              break;
            case OBJECT:
              size = affectedSeedsG.size();
              break;
            case ATTRIBUTE:
              size = affectedSeedsM.size();
              break;
            default:
              size = 1d;
              break;
            }

            if (type.getValue().equals(Type.HYBRID) || type.getValue().equals(Type.OBJECT))
              while (!affectedSeedsG.isEmpty()) {
                final G g = Iterables.getFirst(affectedSeedsG, null);
                final Point3D s = seedsG.get(g);
                final HashSet<G> eq = new HashSet<G>(Maps.filterValues(seedsG, new Predicate<Point3D>() {

                  public final boolean apply(final Point3D p) {
                    return s.equals(p);// || s.distance(p) < s.distance(0, 0, 0) / 16d;
                  }
                }).keySet());
                affectedSeedsG.removeAll(eq);
                final double fx = a.getX() == 0 ? 1d / size : Math.abs(s.getX()) / a.getX();
                final double fy = a.getY() == 0 ? 1d / size : Math.abs(s.getY()) / a.getY();
                final double fz = a.getZ() == 0 ? 1d / size : Math.abs(s.getZ()) / a.getZ();
                final Point3D t =
                    new Point3D(s.getX() + fx * dx, Math.max(0.1d, s.getY() + fy * dy), s.getZ() + fz * dz);
                for (G h : eq)
                  seedsG.put(h, t);
              }
            if (type.getValue().equals(Type.HYBRID) || type.getValue().equals(Type.ATTRIBUTE))
              while (!affectedSeedsM.isEmpty()) {
                final M m = Iterables.getFirst(affectedSeedsM, null);
                final Point3D s = seedsM.get(m);
                final HashSet<M> eq = new HashSet<M>(Maps.filterValues(seedsM, new Predicate<Point3D>() {

                  public final boolean apply(final Point3D p) {
                    return s.equals(p);// || s.distance(p) < s.distance(0, 0, 0) / 16d;
                  }
                }).keySet());
                affectedSeedsM.removeAll(eq);
                final double fx = a.getX() == 0 ? 1d / size : Math.abs(s.getX()) / a.getX();
                final double fy = a.getY() == 0 ? 1d / size : Math.abs(s.getY()) / a.getY();
                final double fz = a.getZ() == 0 ? 1d / size : Math.abs(s.getZ()) / a.getZ();
                final Point3D t =
                    new Point3D(s.getX() + fx * dx, Math.max(0.1d, s.getY() + fy * dy), s.getZ() + fz * dz);
                for (M n : eq)
                  seedsM.put(n, t);
              }
            break;
          }
          break;
        }
      }
    }
    invalidate();
  }

  public final void deleteZ() {
    synchronized (seedsG) {
      seedsG.replaceAll((g, seed) -> seed.subtract(0, 0, seed.getZ()));
    }
    synchronized (seedsM) {
//      seedsM.putAll(new HashMap<M, Point3D>(Maps.transformValues(seedsM, Points.XY_PROJECTION)));
      seedsM.replaceAll((m, seed) -> seed.subtract(0, 0, seed.getZ()));
    }
    invalidate();
  }

  public final AdditiveConceptLayout<G, M> clone() {
    return new AdditiveConceptLayout<G, M>(lattice, seedsG, seedsM, type.getValue());
  }

  public final boolean equals(final Object o) {
    return o != null && o instanceof AdditiveConceptLayout && ((AdditiveConceptLayout<?, ?>) o).lattice.equals(lattice)
        && ((AdditiveConceptLayout<?, ?>) o).seedsG.equals(seedsG)
        && ((AdditiveConceptLayout<?, ?>) o).seedsM.equals(seedsM);
  }

  public final int hashCode() {
    return 7 * lattice.hashCode() + 13 * seedsG.hashCode() + 23 * seedsM.hashCode();
  }
}
