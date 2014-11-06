package conexp.fx.core.layout;

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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;

import org.ujmp.core.collections.BitSetSet;
import org.ujmp.core.util.RandomSimple;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.quality.ConflictDistance;
import conexp.fx.core.quality.LayoutEvolution;
import conexp.fx.gui.task.BlockingTask;

public final class GeneticLayouter<G, M> {

  public static final <G, M> BlockingTask seeds(
      final ConceptLayout<G, M> layout,
      final boolean includingLayout,
      final int generationCount,
      final int populationSize,
      final boolean threeDimensions,
      final ConflictDistance<G, M> conflictDistance,
      final ThreadPoolExecutor tpe) {
    return new BlockingTask("Genetic Layouter") {

      private final DoubleProperty           evolutionaryProgressProperty = new SimpleDoubleProperty(0d);
      private final Set<ConceptLayout<G, M>> layouts                      = new HashSet<ConceptLayout<G, M>>(
                                                                              populationSize);
      private double                         currentQuality               = -1d;
      private ConceptLayout<G, M>            currentBest;
      private ChainDecomposer<Set<Integer>>  chainDecomposer;
      private Random                         rng                          = new RandomSimple();

      @Override
      protected final void updateProgress(double workDone, double max) {
        super.updateProgress(workDone, max);
        evolutionaryProgressProperty.set(workDone / max);
      };

      @Override
      protected final void _call() {
        updateProgress(0.1d, 1d);
        updateMessage("Decomposing Concept Lattice...");
        chainDecomposer =
            new ChainDecomposer<Set<Integer>>(layout.lattice.context.selection._reduced
                .clone()
                .attributeQuasiOrder()
                .neighborhood());
        if (includingLayout) {
          layouts.add(layout.clone());
          currentBest = layout;
        }
        updateProgress(0.2d, 1d);
        updateMessage("Setting up Evolution Engine...");
        initPopulation();
        updateProgress(0.3d, 1d);
        updateMessage("Evolving Layout...");
        evolvePopulation();
      }

      private final void initPopulation() {
        final Set<Future<?>> futures = new HashSet<Future<?>>();
        for (int i = includingLayout ? 1 : 0; i < populationSize; i++)
          futures.add(tpe.submit(generate()));
        for (Future<?> f : futures)
          try {
            f.get();
          } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
          }
      }

      private final Runnable generate() {
        return new Runnable() {

          @Override
          public void run() {
            final ConceptLayout<G, M> candidate = new ConceptLayout<G, M>(layout.lattice, null);
            final Set<Set<Set<Integer>>> chains = chainDecomposer.randomChainDecomposition();
            final int w = chains.size();
            if (w == 1) {
              final Point3D seed = new Point3D(0d, 1d, 0d);
              for (M m : Collections2.transform(
                  Iterables.getOnlyElement(chains),
                  candidate.lattice.context.selection._firstAttribute))
                candidate.seeds.put(m, seed);
            } else {
              final int ww = w * w;
              final BitSetSet usedX = new BitSetSet();
              for (Set<Set<Integer>> chain : chains) {
                int _x;
                for (_x = rng.nextInt(ww + 1); usedX.contains(_x) || usedX.contains(_x + 1)
                    || (_x != 0 && usedX.contains(_x - 1)); _x = rng.nextInt(ww + 1));
                usedX.add(_x);
                final int __x = _x - ww / 2;
                final Point3D seed =
                    new Point3D((double) __x, (double) (rng.nextInt(Math.abs(__x + 1) + 1) + 1), threeDimensions
                        ? (double) (rng.nextInt(Math.abs(__x + 1) + 1) - __x / 2) : 0d);
                for (M m : Collections2.transform(chain, candidate.lattice.context.selection._firstAttribute))
                  candidate.seeds.put(m, seed);
              }
            }
            synchronized (layouts) {
              layouts.add(candidate);
            }
            final double result = conflictDistance.apply(candidate).second();
            if (result > currentQuality) {
              currentQuality = result;
              currentBest = candidate;
            }
          }
        };
      }

      private final void evolvePopulation() {
        for (int i = 0; i < generationCount; i++) {
          if (currentBest != layout)
            layout.updateSeeds(currentBest.seeds);
          updateProgress(0.3d + 0.7d * ((double) i) / (double) generationCount, 1d);
          updateMessage("Evolving Seeds: " + i + " of " + generationCount + " Generations...");
          evolveGeneration();
        }
        try {
          if (currentBest != layout)
            layout.updateSeeds(currentBest.seeds);
        } catch (Exception e) {
          System.err.println(currentBest);
          System.err.println(layout);
          e.printStackTrace();
        }
      }

      private final void evolveGeneration() {
        currentQuality = -1d;
        for (ConceptLayout<G, M> candidate : layouts) {
          LayoutEvolution<G, M>.Value v =
              new LayoutEvolution<G, M>(
                  candidate,
                  conflictDistance.apply(candidate).first(),
                  ConceptMovement.INTENT_CHAIN_SEEDS,
                  4d,
                  4d,
                  2,
                  2,
                  1,
                  conflictDistance,
                  tpe).calculate();
          if (!candidate.updateSeeds(v.seeds)) {
            if (rng.nextBoolean())
              v =
                  new LayoutEvolution<G, M>(candidate, candidate.lattice.attributeConcepts.get(Collections3.random(
                      candidate.seeds.keySet(),
                      rng)), ConceptMovement.LABEL_CHAIN_SEEDS, 4d, 4d, 2, 2, 1, conflictDistance, tpe).calculate();
            else
              v =
                  new LayoutEvolution<G, M>(
                      candidate,
                      Collections3.random(candidate.lattice.rowHeads(), rng),
                      ConceptMovement.INTENT_CHAIN_SEEDS,
                      4d,
                      4d,
                      2,
                      2,
                      1,
                      conflictDistance,
                      tpe).calculate();
            candidate.updateSeeds(v.seeds);
          }
          if (v.result > currentQuality) {
            currentQuality = v.result;
            currentBest = candidate;
          }
        }
      }
    };
  }
}
