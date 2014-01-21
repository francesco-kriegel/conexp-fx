package conexp.fx.core.algorithm.lattice;

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


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javafx.collections.ObservableSet;
import javafx.geometry.Point3D;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.concurrent.BlockingTask;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.layout.ConceptLayout;
import conexp.fx.core.layout.ConceptMovement;
import conexp.fx.core.math.Points;
import conexp.fx.core.quality.ConflictDistance;
import conexp.fx.core.quality.LayoutEvolution;

public final class IFox<G, M>
{
  private enum Type
  {
    EQUIVALENT,
    REDUCIBLE,
    IRREDUCIBLE;
  }

  private static final class Attribute<M>
  {
    private final Type   type;
    private final Set<M> equivalents;

    @SafeVarargs
    private Attribute(final Type type, final Collection<M>... equivalents)
    {
      this.type = type;
      if (equivalents.length == 0)
        this.equivalents = Collections.<M> emptySet();
      else
        this.equivalents = new HashSet<M>(equivalents[0]);
    }
  }

  private static final <G, M> Attribute<M> attribute(final MatrixContext<G, M> context, final M m)
  {
    final int _m = context.selection.colHeads().indexOf(m);
    for (Set<Integer> _n : context.selection._attributes)
      if (_n.contains(_m))
        if (_n.size() > 1)
          return new Attribute<M>(Type.EQUIVALENT, Collections2.transform(
              Sets.difference(_n, Collections.singleton(_m)),
              context.selection.colHeads().index().inverse()));
        else if (context.selection._irreducibleAttributes.contains(_n))
          return new Attribute<M>(Type.IRREDUCIBLE);
        else
          return new Attribute<M>(Type.REDUCIBLE);
    return null;
  }

  private static final <G, M> void adjust(
      final ConceptLayout<G, M> layout,
      final M m,
      final ConflictDistance<G, M> conflictDistance,
      final ThreadPoolExecutor tpe)
  {
    LayoutEvolution<G, M>.Value v =
        new LayoutEvolution<G, M>(
            layout,
            layout.lattice.attributeConcepts.get(m),
            ConceptMovement.INTENT_CHAIN_SEEDS,
            4d,
            4d,
            2,
            2,
            1,
            conflictDistance,
            tpe).calculate();
    layout.updateSeeds(v.seeds);
    v =
        new LayoutEvolution<G, M>(
            layout,
            v.hint,
            ConceptMovement.INTENT_CHAIN_SEEDS,
            4d,
            4d,
            2,
            2,
            1,
            conflictDistance,
            tpe).calculate();
    layout.updateSeeds(v.seeds);
  }

  public static final <G, M> BlockingTask select(
      final ConceptLayout<G, M> layout,
      final M m,
      final ConflictDistance<G, M> conflictDistance,
      final ThreadPoolExecutor tpe)
  {
    return new BlockingTask("iFox Select")
      {
        private MatrixContext<G, M> I;
        private MatrixContext<G, M> IuJ;
        private Set<G>              mJ;
        private Attribute<M>        attribute;

        protected void _call()
        {
          updateMessage("Calculating Incremental Update...");
          I = layout.lattice.context.selection;
          mJ = new HashSet<G>(layout.lattice.context.col(m));
          layout.lattice.context.selectAttribute(m);
          IuJ = layout.lattice.context.selection;
          attribute = attribute(layout.lattice.context, m);
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
        }

        private void equivalent()
        {
          reducible();
        }

        private void reducible()
        {
          // modify only label and intents
          for (Concept<G, M> c : layout.lattice.rowHeads())
            if (mJ.containsAll(c.extent())) {
              c.intent().add(m);
              if (mJ.size() == c.extent().size())
                synchronized (layout.lattice.attributeConcepts) {
                  layout.lattice.attributeConcepts.put(m, c);
                }
            }
        }

        @SuppressWarnings("deprecation")
        private void irreducible()
        {
          // full update
          updateMessage("Determining old, varying and generating concepts...");
          updateProgress(0.1d, 1d);
          final Set<Concept<G, M>> olds = new HashSet<Concept<G, M>>();
          final Set<Concept<G, M>> vars = new HashSet<Concept<G, M>>();
          final Set<Concept<G, M>> gens = new HashSet<Concept<G, M>>();
          final Predicate<Concept<G, M>> varPredicate = new Predicate<Concept<G, M>>()
            {
              public final boolean apply(final Concept<G, M> c)
              {
                return mJ.containsAll(c.extent());
              }
            };
          final Predicate<Concept<G, M>> genPredicate = new Predicate<Concept<G, M>>()
            {
              public final boolean apply(final Concept<G, M> c)
              {
                return I.rowAnd(Sets.intersection(c.extent(), mJ)).equals(c.intent());
              }
            };
          vars.addAll(Collections2.filter(layout.lattice.rowHeads(), varPredicate));
          olds.addAll(Sets.difference(layout.lattice.rowHeads(), vars));
          gens.addAll(Collections2.filter(olds, genPredicate));
          final Map<Concept<G, M>, Concept<G, M>> news = new HashMap<Concept<G, M>, Concept<G, M>>(gens.size());
          updateMessage("Updating varying concepts...");
          updateProgress(0.2d, 1d);
          for (Concept<G, M> v : vars) {
            v.intent().add(m);
            for (Concept<G, M> g : gens)
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
              if (v.smaller(g) && Sets.filter(Sets.union(gens, vars), new Predicate<Concept<G, M>>()
                {
                  public final boolean apply(final Concept<G, M> c)
                  {
                    return v.smaller(c) && c.smaller(g);
                  }
                }).isEmpty())
                layout.lattice.addFast(v, n);
            // layout.invalidate();
          }
          updateMessage("Creating new concepts neighborhood...");
          updateProgress(0.55d, 1d);
          i = 0d;
          for (final Entry<Concept<G, M>, Concept<G, M>> n1 : news.entrySet())
            for (final Entry<Concept<G, M>, Concept<G, M>> n2 : news.entrySet()) {
              updateProgress(0.55d + 0.25d * (i++ / Math.pow(news.size(), 2)), 1d);
              if (n1.getValue().smaller(n2.getValue()) && Sets.filter(gens, new Predicate<Concept<G, M>>()
                {
                  public final boolean apply(final Concept<G, M> c)
                  {
                    return n1.getValue().smaller(c) && c.smaller(n2.getValue());
                  }
                }).isEmpty())
                layout.lattice.addFast(n1.getKey(), n2.getKey());
            }
          // layout.invalidate();
          updateMessage("Updating seeds...");
          updateProgress(0.8d, 1d);
          synchronized (layout.seeds) {
            updateMessage("Determining new Reducibles...");
            for (M n : layout.seeds.keySet()) {
              final int _n = IuJ.colHeads().indexOf(n);
              final Set<Integer> eq = Iterables.find(IuJ._attributes, new Predicate<Collection<Integer>>()
                {
                  public final boolean apply(final Collection<Integer> c)
                  {
                    return c.contains(_n);
                  }
                });
              if (!IuJ._irreducibleAttributes.contains(eq)) {
                updateMessage("Backup seed: " + n);
                final Point3D oldSeed = layout.seeds.remove(n);
                layout.seedHistory.put(n, oldSeed);
              }
            }
            // final HashSet<M> currentIrreducibles = new HashSet<M>(layout.seeds.keySet());
            // for (M s : currentIrreducibles)
            // if (layout.lattice.row(layout.lattice.attributeConcepts.get(s)).size() != 1)
            if (layout.seedHistory.containsKey(m)) {
              updateMessage("Restoring seed: " + m);
              layout.seeds.put(m, layout.seedHistory.get(m));
            } else {
              updateMessage("Computing random seed: " + m);
              layout.seeds.put(m, new Point3D(2d * Math.random() - 1d, 0.5d + Math.random(), 0));
            }
            adjust(layout, m, conflictDistance, tpe);
          }
          layout.lattice.pushAllChangedEvent();
        }
      };
  }

  public static final <G, M> BlockingTask ignore(
      final ConceptLayout<G, M> layout,
      final M m,
      final ConflictDistance<G, M> conflictDistance,
      final ThreadPoolExecutor tpe)
  {
    return new BlockingTask("iFox Ignore")
      {
        private MatrixContext<G, M> I;
        private Attribute<M>        attribute;

        protected void _call()
        {
          updateMessage("Calculating Incremental Update...");
          attribute = attribute(layout.lattice.context, m);
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
          layout.invalidate();
        }

        private void equivalent()
        {
          // modify only label and intents
          synchronized (layout.lattice.attributeConcepts) {
            layout.lattice.attributeConcepts.remove(m);
          }
          for (Concept<G, M> c : layout.lattice.rowHeads())
            c.intent().remove(m);
          synchronized (layout.seeds) {
            final Point3D seed = layout.seeds.remove(m);
            if (seed != null) {
              layout.seedHistory.put(m, seed);
              final M n = attribute.equivalents.iterator().next();
              layout.seeds.put(n, seed);
            }
          }
        }

        private void reducible()
        {
          // modify only label and intents
          synchronized (layout.lattice.attributeConcepts) {
            layout.lattice.attributeConcepts.remove(m);
          }
          for (Concept<G, M> c : layout.lattice.rowHeads())
            c.intent().remove(m);
        }

        private void irreducible()
        {
          // full update
          updateMessage("Determining old, varying and generating concepts...");
          updateProgress(0.1d, 1d);
          final Set<Concept<G, M>> olds = new HashSet<Concept<G, M>>();
          final Set<Concept<G, M>> vars = new HashSet<Concept<G, M>>();
          final Set<Concept<G, M>> _news = new HashSet<Concept<G, M>>();
          final Map<Concept<G, M>, Concept<G, M>> news = new HashMap<Concept<G, M>, Concept<G, M>>();
          final Set<Concept<G, M>> oldNonGens = new HashSet<Concept<G, M>>();
          final Predicate<Concept<G, M>> oldPredicate = new Predicate<Concept<G, M>>()
            {
              public final boolean apply(final Concept<G, M> c)
              {
                return !c.intent().contains(m);
              }
            };
          final Predicate<Concept<G, M>> varPredicate = new Predicate<Concept<G, M>>()
            {
              public final boolean apply(final Concept<G, M> c)
              {
                return !olds.contains(c)
                    && c.extent().equals(I.colAnd(Sets.difference(c.intent(), Collections.singleton(m))));
              }
            };
          olds.addAll(Collections2.filter(layout.lattice.rowHeads(), oldPredicate));
          vars.addAll(Collections2.filter(layout.lattice.rowHeads(), varPredicate));
          _news.addAll(Sets.difference(layout.lattice.rowHeads(), Sets.union(olds, vars)));
          updateMessage("Computing generating concepts...");
          updateProgress(0.2d, 1d);
          synchronized (layout.lattice.attributeConcepts) {
            layout.lattice.attributeConcepts.remove(m);
          }
          for (Concept<G, M> n : _news) {
            final ObservableSet<M> intent = n.intent();
            final Predicate<Concept<G, M>> genPredicate = new Predicate<Concept<G, M>>()
              {
                public final boolean apply(final Concept<G, M> c)
                {
                  return c.intent().equals(Sets.difference(intent, Collections.singleton(m)));
                }
              };
            final Concept<G, M> g = Iterators.find(olds.iterator(), genPredicate);
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
          oldNonGens.addAll(Collections3.difference(olds, news.values()));
          // layout.invalidate();
          updateMessage("Updating varying concepts...");
          updateProgress(0.3d, 1d);
          for (Concept<G, M> v : vars)
            v.intent().remove(m);
          for (final Entry<Concept<G, M>, Concept<G, M>> n : news.entrySet())
            for (final Concept<G, M> v : vars) {
              final Predicate<Concept<G, M>> vnIntervalPredicate = new Predicate<Concept<G, M>>()
                {
                  public final boolean apply(final Concept<G, M> c)
                  {
                    return v.smaller(c) && c.smaller(n.getValue());
                  }
                };
              if (layout.lattice.contains(v, n.getKey()) && Sets.filter(oldNonGens, vnIntervalPredicate).isEmpty())
                layout.lattice.addFast(v, n.getValue());
            }
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
          synchronized (layout.seeds) {
            updateMessage("Backup seed: " + m);
            final Point3D oldSeed = layout.seeds.remove(m);
            layout.seedHistory.put(m, oldSeed);
            Set<Integer> _seeds =
                new HashSet<Integer>(Collections2.transform(layout.seeds.keySet(), I.colHeads().index()));
            for (Set<Integer> eq : I._irreducibleAttributes)
              if (!Iterables.any(_seeds, Predicates.in(eq))) {
                // introduce new seed
                final M n = Collections2.transform(eq, I.colHeads().index().inverse()).iterator().next();
                // if (true) {
                // read seed vector from current layout
                final Concept<G, M> nc = I.attributeConcept(n);
                final Point3D np = Points.plus(layout.positions.get(nc), oldSeed);
                final Point3D nnp = layout.positions.get(Iterables.getOnlyElement(layout.lattice.row(nc)));
                final Point3D seed = Points.minus(np, nnp);
                layout.seeds.put(n, seed);
                adjust(layout, n, conflictDistance, tpe);
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
              }
            // final HashSet<M> currentReducibles =
            // new HashSet<M>(Sets.difference(I.colHeads(), layout.seeds.keySet()));
            // for (M s : currentReducibles)
            // if (Sets.difference(layout.lattice.row(layout.lattice.attributeConcepts.get(s)), news.keySet()).size() ==
            // 1)
          }
          updateProgress(0.9d, 1d);
          layout.lattice.pushAllChangedEvent();
        }
      };
  }
}
