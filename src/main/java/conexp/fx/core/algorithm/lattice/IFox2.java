package conexp.fx.core.algorithm.lattice;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.layout.AdditiveConceptLayout;
import conexp.fx.core.layout.ConceptMovement;
import conexp.fx.core.layout.LayoutEvolution;
import conexp.fx.core.layout.QualityMeasure;
import conexp.fx.gui.task.TimeTask;
import javafx.geometry.Point3D;

public final class IFox2<G, M> {

  private enum Type {
    EQUIVALENT,
    REDUCIBLE,
    IRREDUCIBLE;
  }

  private static final class Attribute<M> {

    private final Type   type;
    private final Set<M> equivalents;

    @SafeVarargs
    private Attribute(final Type type, final Collection<M>... equivalents) {
      this.type = type;
      if (equivalents.length == 0)
        this.equivalents = Collections.<M> emptySet();
      else
        this.equivalents = new HashSet<M>(equivalents[0]);
    }
  }

  private static final <G, M> Attribute<M> attribute(final MatrixContext<G, M> context, final M m) {
    final int _m = context.selection.colHeads().indexOf(m);
    for (Set<Integer> _n : context.selection._attributes)
      if (_n.contains(_m))
        if (_n.size() > 1)
          return new Attribute<M>(
              Type.EQUIVALENT,
              Collections2.transform(
                  Sets.difference(_n, Collections.singleton(_m)),
                  context.selection.colHeads().indexGuava().inverse()));
        else if (context.selection._irreducibleAttributes.contains(_n))
          return new Attribute<M>(Type.IRREDUCIBLE);
        else
          return new Attribute<M>(Type.REDUCIBLE);
    return null;
  }

  private static final <G, M> void adjust(
      final AdditiveConceptLayout<G, M> layout,
      final M m,
      final QualityMeasure<G, M, Pair<Concept<G, M>, Double>> conflictDistance,
      final AbstractExecutorService tpe) {
    LayoutEvolution<G, M>.Value v = new LayoutEvolution<G, M>(
        layout,
        layout.lattice.attributeConcepts.get(m),
        // TODO
        // ConceptMovement.LABEL_SEED,
        ConceptMovement.INTENT_CHAIN_SEEDS,
        2d,
        2d,
        1,
        1,
        1,
        conflictDistance,
        tpe).calculate();
    layout.updateSeeds(v.seedsG, v.seedsM);
//    v =
//        new LayoutEvolution<G, M>(
//            layout,
//            v.hint,
//            ConceptMovement.INTENT_CHAIN_SEEDS,
//            4d,
//            4d,
//            2,
//            2,
//            1,
//            conflictDistance,
//            tpe).calculate();
//    layout.updateSeeds(v.seeds);
  }

  public static final <G, M> TimeTask<Void> select(
      final String id,
      final AdditiveConceptLayout<G, M> layout,
      final M m,
      final QualityMeasure<G, M, Pair<Concept<G, M>, Double>> conflictDistance,
      final ExecutorService tpe) {
    return new TimeTask<Void>(id + " - iFox Select") {

      private MatrixContext<G, M> I;
      private MatrixContext<G, M> IuJ;
      private Set<G>              mJ;
      private Attribute<M>        attribute;

      protected Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateMessage("Calculating Incremental Update...");
        I = layout.lattice.context.selection;
        mJ = new HashSet<G>(layout.lattice.context.col(m));
        layout.lattice.context.selectAttribute(m);
        IuJ = layout.lattice.context.selection;
        attribute = attribute(layout.lattice.context.selection, m);
        switch (attribute.type) {
        case EQUIVALENT:
          equivalent();
          break;
        case REDUCIBLE:
          reducible();
          break;
        case IRREDUCIBLE:
          irreducible();
          break;
        }
        layout.invalidate();
        layout.lattice.pushAllChangedEvent();
        updateProgress(1d, 1d);
        return null;
      }

      private void equivalent() {
        reducible();
      }

      private void reducible() {
        // modify only label and intents
        for (final Concept<G, M> c : layout.lattice.rowHeads())
          if (mJ.containsAll(c.extent())) {
            c.intent().add(m);
//            Platform2.runOnFXThread(() -> c.intent().add(m));
            if (mJ.size() == c.extent().size())
              synchronized (layout.lattice.attributeConcepts) {
                layout.lattice.attributeConcepts.put(m, c);
              }
          }
      }

      private void irreducible() {
        // full update
        updateMessage("Determining old, varying and generating concepts...");
        updateProgress(0.1d, 1d);
        final Set<Concept<G, M>> vars =
            layout.lattice.rowHeads().stream().filter(c -> mJ.containsAll(c.extent())).collect(Collectors.toSet());
        final Set<Concept<G, M>> olds =
            layout.lattice.rowHeads().stream().filter(c -> !vars.contains(c)).collect(Collectors.toSet());
        final Set<Concept<G, M>> gens =
            olds.stream().filter(c -> I.rowAnd(Sets.intersection(c.extent(), mJ)).equals(c.intent())).collect(
                Collectors.toSet());
        final Map<Concept<G, M>, Concept<G, M>> news = new HashMap<Concept<G, M>, Concept<G, M>>(gens.size());
        updateMessage("Updating varying concepts...");
        updateProgress(0.2d, 1d);
        for (final Concept<G, M> v : vars) {
          v.intent().add(m);
//          Platform2.runOnFXThread(() -> v.intent().add(m));
          for (final Concept<G, M> g : gens)
            layout.lattice.remove(v, g);
        }
        updateMessage("Creating new concepts...");
        updateProgress(0.3d, 1d);
        double i = 0d;
        for (final Concept<G, M> g : gens) {
          updateProgress(0.3d + 0.25d * (i++ / (double) gens.size()), 1d);
          final HashSet<G> gol = new HashSet<G>(layout.lattice.objectLabels(g));
          final Concept<G, M> n = g.clone();
          n.extent().retainAll(mJ);
          n.intent().add(m);
          news.put(n, g);
          synchronized (layout.generators) {
            layout.generators.put(n, g);
          }
          layout.lattice.rowHeads().add(n);
          layout.lattice.addFast(n, g);
          if (g.extent().containsAll(mJ))
            synchronized (layout.lattice.attributeConcepts) {
              layout.lattice.attributeConcepts.put(m, n);
            }
          for (G ol : Sets.intersection(gol, mJ))
            synchronized (layout.lattice.objectConcepts) {
              layout.lattice.objectConcepts.put(ol, n);
            }
          for (final Concept<G, M> v : vars)
            if (v.smaller(g)
                && Stream.concat(gens.stream(), vars.stream()).noneMatch(c -> v.smaller(c) && c.smaller(g)))
              layout.lattice.addFast(v, n);
          // layout.invalidate();
        }
        updateMessage("Creating new concepts neighborhood...");
        updateProgress(0.55d, 1d);
        i = 0d;
        for (final Entry<Concept<G, M>, Concept<G, M>> n1 : news.entrySet())
          for (final Entry<Concept<G, M>, Concept<G, M>> n2 : news.entrySet()) {
            updateProgress(0.55d + 0.25d * (i++ / Math.pow(news.size(), 2)), 1d);
            if (n1.getValue().smaller(n2.getValue())
                && gens.stream().noneMatch(c -> n1.getValue().smaller(c) && c.smaller(n2.getValue())))
              layout.lattice.addFast(n1.getKey(), n2.getKey());
          }
        // layout.invalidate();
        updateMessage("Updating seeds...");
        updateProgress(0.8d, 1d);
        synchronized (layout.seedsM) {
          updateMessage("Determining new Reducibles...");
          for (M n : layout.seedsM.keySet()) {
            final int _n = IuJ.colHeads().indexOf(n);
            final Set<Integer> eq = IuJ._attributes.stream().filter(c -> c.contains(_n)).findAny().get();
            if (!IuJ._irreducibleAttributes.contains(eq)) {
              updateMessage("Backup seed: " + n);
              final Point3D oldSeed = layout.seedsM.remove(n);
              layout.seedHistoryM.put(n, oldSeed);
            }
          }
          // final HashSet<M> currentIrreducibles = new HashSet<M>(layout.seeds.keySet());
          // for (M s : currentIrreducibles)
          // if (layout.lattice.row(layout.lattice.attributeConcepts.get(s)).size() != 1)
          if (layout.seedHistoryM.containsKey(m) && !layout.seedsM.values().contains(layout.seedHistoryM.get(m))) {
            updateMessage("Restoring seed: " + m);
            layout.seedsM.put(m, layout.seedHistoryM.get(m));
          } else {
            updateMessage("Computing random seed: " + m);
            layout.seedsM.put(m, new Point3D(1d + Math.random(), 0.5d + Math.random(), 0));
          }
        }
        synchronized (layout.seedsG) {
          IuJ._irreducibleObjects
              .stream()
              .filter(
                  eq -> eq
                      .stream()
                      .map(IuJ.rowHeads()::get)
                      .filter(((Predicate<G>) layout.seedsG::containsKey).negate())
                      .findAny()
                      .isPresent())
              .map(IuJ._firstObject::apply)
              .forEach(g -> layout.seedsG.put(g, new Point3D(1d + Math.random(), 0.5d + Math.random(), 0)));
          layout.seedsG.keySet().removeIf(
              g -> !IuJ._irreducibleObjects
                  .contains(IuJ._objects.stream().filter(c -> c.contains(IuJ.rowHeads().indexOf(g))).findAny().get()));
        }
        updateProgress(0.9d, 1d);
        updateMessage("Adjusting Layout...");
//    adjust(layout, m, conflictDistance, tpe);
      }
    };
  }

  public static final <G, M> TimeTask<Void> ignore(
      final String id,
      final AdditiveConceptLayout<G, M> layout,
      final M m,
      final QualityMeasure<G, M, Pair<Concept<G, M>, Double>> conflictDistance,
      final ExecutorService tpe) {
    return new TimeTask<Void>(id + " - iFox Ignore") {

      private MatrixContext<G, M> I;
      private Attribute<M>        attribute;

      protected Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateMessage("Calculating Incremental Update...");
        attribute = attribute(layout.lattice.context.selection, m);
        layout.lattice.context.deselectAttribute(m);
        I = layout.lattice.context.selection;
        switch (attribute.type) {
        case EQUIVALENT:
          equivalent();
          break;
        case REDUCIBLE:
          reducible();
          break;
        case IRREDUCIBLE:
          irreducible();
          break;
        }
        layout.lattice.pushAllChangedEvent();
        layout.invalidate();
        updateProgress(1d, 1d);
        return null;
      }

      private void equivalent() {
        // modify only label and intents
        synchronized (layout.lattice.attributeConcepts) {
          layout.lattice.attributeConcepts.remove(m);
        }
        for (Concept<G, M> c : layout.lattice.rowHeads())
          c.intent().remove(m);
//          Platform2.runOnFXThread(() -> c.intent().remove(m));
        synchronized (layout.seedsM) {
          final Point3D seed = layout.seedsM.remove(m);
          if (seed != null) {
            layout.seedHistoryM.put(m, seed);
            final M n = attribute.equivalents.iterator().next();
            layout.seedsM.put(n, seed);
          }
        }
      }

      private void reducible() {
        // modify only label and intents
        synchronized (layout.lattice.attributeConcepts) {
          layout.lattice.attributeConcepts.remove(m);
        }
        for (Concept<G, M> c : layout.lattice.rowHeads())
          c.intent().remove(m);
//          Platform2.runOnFXThread(() -> c.intent().remove(m));
      }

      private void irreducible() {
        // full update
        updateMessage("Determining old, varying and generating concepts...");
        updateProgress(0.1d, 1d);
        final Set<Concept<G, M>> olds =
            layout.lattice.rowHeads().stream().filter(c -> !c.intent().contains(m)).collect(Collectors.toSet());
        final Set<Concept<G, M>> vars = layout.lattice
            .rowHeads()
            .stream()
            .filter(
                c -> !olds.contains(c)
                    && c.extent().equals(I.colAnd(Sets.difference(c.intent(), Collections.singleton(m)))))
            .collect(Collectors.toSet());
        final Set<Concept<G, M>> _news = layout.lattice
            .rowHeads()
            .stream()
            .filter(c -> !olds.contains(c) && !vars.contains(c))
            .collect(Collectors.toSet());
        updateMessage("Computing generating concepts...");
        updateProgress(0.2d, 1d);
        synchronized (layout.lattice.attributeConcepts) {
          layout.lattice.attributeConcepts.remove(m);
        }
        final Map<Concept<G, M>, Concept<G, M>> news = new HashMap<Concept<G, M>, Concept<G, M>>();
        for (Concept<G, M> n : _news) {
          final Concept<G, M> g = olds
              .stream()
              // .filter(c -> c.intent().containsAll(n.intent()) && c.intent().size() == n.intent().size() - 1)
              .filter(c -> c.intent().equals(Sets.difference(n.intent(), Collections.singleton(m))))
              .findAny()
              .get();
          synchronized (layout.lattice.objectConcepts) {
            final Set<G> nol = new HashSet<G>(layout.lattice.objectLabels(n));
            for (G ol : nol)
              layout.lattice.objectConcepts.put(ol, g);
          }
          news.put(n, g);
          synchronized (layout.generators) {
            layout.generators.put(n, g);
          }
        }
        final Set<Concept<G, M>> oldNonGens =
            olds.stream().filter(c -> !news.values().contains(c)).collect(Collectors.toSet());
        // layout.invalidate();
        updateMessage("Updating varying concepts...");
        updateProgress(0.3d, 1d);
        for (Concept<G, M> v : vars)
          v.intent().remove(m);
//          Platform2.runOnFXThread(() -> v.intent().remove(m));
        for (final Entry<Concept<G, M>, Concept<G, M>> n : news.entrySet())
          for (final Concept<G, M> v : vars)
            if (layout.lattice.contains(v, n.getKey())
                && oldNonGens.stream().noneMatch(c -> v.smaller(c) && c.smaller(n.getValue())))
              layout.lattice.addFast(v, n.getValue());
        // layout.invalidate();
        updateMessage("Removing concepts...");
        // layout.lattice.rowHeads().removeAll(news.keySet());
        for (Concept<G, M> y : news.keySet()) {
          for (Concept<G, M> z : layout.lattice.row(y))
            layout.lattice.remove(y, z);
          for (Concept<G, M> z : layout.lattice.col(y))
            layout.lattice.remove(z, y);
          layout.lattice.rowHeads().remove(y);
        }
        updateMessage("Updating seeds...");
        updateProgress(0.6d, 1d);
        synchronized (layout.seedsM) {
          updateMessage("Backup seed: " + m);
          // TODO: seed is sometimes not available in the map. check for a solution!
          final Point3D oldSeed = layout.seedsM.remove(m);
          layout.seedHistoryM.put(m, oldSeed);
          Set<Integer> _seeds =
              new HashSet<Integer>(Collections2.transform(layout.seedsM.keySet(), I.colHeads().indexGuava()));
          for (Set<Integer> eq : I._irreducibleAttributes)
            if (!Iterables.any(_seeds, Predicates.in(eq))) {
              // introduce new seed
              // interate over all possible attributes in the equivalence class!
//              final M n = Collections2.transform(eq, I.colHeads().index().inverse()).iterator().next();
              for (M n : Collections2.transform(eq, I.colHeads().indexGuava().inverse())) {
                // if (true) {
                // read seed vector from current layout
                final Concept<G, M> nc = I.attributeConcept(n);
                if (layout.getPosition(nc) != null) {
                  final Point3D np = layout.getPosition(nc).getValue();// .add(oldSeed);
                  final Point3D nnp = layout.getPosition(Iterables.getOnlyElement(layout.lattice.row(nc))).getValue();
                  final Point3D seed = np.subtract(nnp);
                  layout.seedsM.put(n, seed);
                  updateProgress(0.8d, 1d);
                  updateMessage("Adjusting Layout...");
//                adjust(layout, n, conflictDistance, tpe);
                  // }
                  // if (layout.seedHistory.containsKey(n)) {
                  // updateMessage("Restoring seed: " + n);
                  // final Point3D seedBackup = layout.seedHistory.get(n);
                  // layout.seeds.put(n, seedBackup);
                  // } else {
                  // updateMessage("Computing random seed: " + n);
                  // layout.seeds.put(n, new Point3D(2d * Math.random() - 1d, 0.5d + Math.random(), 0));
                  // adjust(layout, n);
                  // }
                  break;
                }
              }
            }
          // final HashSet<M> currentReducibles =
          // new HashSet<M>(Sets.difference(I.colHeads(), layout.seeds.keySet()));
          // for (M s : currentReducibles)
          // if (Sets.difference(layout.lattice.row(layout.lattice.attributeConcepts.get(s)), news.keySet()).size() ==
          // 1)
        }
        synchronized (layout.seedsG) {
          I._irreducibleObjects
              .stream()
              .filter(
                  eq -> eq
                      .stream()
                      .map(I.rowHeads()::get)
                      .filter(((Predicate<G>) layout.seedsG::containsKey).negate())
                      .findAny()
                      .isPresent())
              .map(I._firstObject::apply)
              .forEach(g -> layout.seedsG.put(g, new Point3D(1d + Math.random(), 0.5d + Math.random(), 0)));
          layout.seedsG.keySet().removeIf(
              g -> !I._irreducibleObjects
                  .contains(I._objects.stream().filter(c -> c.contains(I.rowHeads().indexOf(g))).findAny().get()));
        }
        updateProgress(0.9d, 1d);
      }
    };
  }
}
