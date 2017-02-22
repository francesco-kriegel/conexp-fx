package conexp.fx.core.layout;

import java.util.HashMap;

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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ujmp.core.util.RandomSimple;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import conexp.fx.core.collections.BitSetFX;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.layout.AdditiveConceptLayout.Type;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.task.TimeTask;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;

public final class GeneticLayouter<G, M> {

  public static final <G, M> TimeTask<Void> initialSeeds(final FCADataset<G, M> dataset) {
    return new TimeTask<Void>(dataset, "Initial Seeds and Labels") {

      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        final Random rng = new RandomSimple();
        updateMessage("Computing Infimum Irreducibles...");
        final Set<G> supremumIrreducibles = dataset.layout.lattice.context.selection.supremumIrreducibles();
        final Set<M> infimumIrreducibles = dataset.layout.lattice.context.selection.infimumIrreducibles();
        updateProgress(0.2d, 1d);
        updateProgress(0.3d, 1d);
        updateMessage("Generating Layered Random Seeds...");
        final Map<G, Point3D> randomSeedsG = new HashMap<G, Point3D>();
        final Map<M, Point3D> randomSeedsM = new HashMap<M, Point3D>();
        for (G g : supremumIrreducibles)
          randomSeedsG.put(g, new Point3D(2d * rng.nextDouble() - 1d, 1, 0));
        for (M m : infimumIrreducibles)
          randomSeedsM.put(m, new Point3D(2d * rng.nextDouble() - 1d, 1, 0));
        updateProgress(0.4d, 1d);
        dataset.layout.updateSeeds(randomSeedsG, randomSeedsM);
        updateProgress(0.5d, 1d);
        updateMessage("Computing Attribute Labels...");
        for (Concept<G, M> c : dataset.layout.lattice.rowHeads()) {
          final Set<M> attributeLabels =
              new HashSet<M>(dataset.layout.lattice.context.selection.attributeLabels(c.extent(), c.intent()));
          synchronized (dataset.layout.lattice.attributeConcepts) {
            for (M m : attributeLabels)
              dataset.layout.lattice.attributeConcepts.put(m, c);
          }
        }
        updateProgress(0.75d, 1d);
        updateMessage("Computing Object Labels...");
        for (Concept<G, M> c : dataset.layout.lattice.rowHeads()) {
          final Set<G> objectLabels =
              new HashSet<G>(dataset.layout.lattice.context.selection.objectLabels(c.extent(), c.intent()));
          synchronized (dataset.layout.lattice.objectConcepts) {
            for (G g : objectLabels)
              dataset.layout.lattice.objectConcepts.put(g, c);
          }
        }
        updateProgress(1d, 1d);
        return null;
      }
    };
  }

  public static final <G, M> TimeTask<Void> seeds(
      final FCADataset<G, M> dataset,
      final boolean includingLayout,
      final int generationCount,
      final int populationSize) {
    return new TimeTask<Void>(dataset, "Genetic Layouter") {

      private final DoubleProperty                   evolutionaryProgressProperty = new SimpleDoubleProperty(0d);
      private final Set<AdditiveConceptLayout<G, M>> layouts                      =
          new HashSet<AdditiveConceptLayout<G, M>>(populationSize);
      private double                                 currentQuality               = -1d;
      private AdditiveConceptLayout<G, M>            currentBest;
      private ChainDecomposer<Set<Integer>>          chainDecomposerG;
      private ChainDecomposer<Set<Integer>>          chainDecomposerM;
      private Random                                 rng                          = new RandomSimple();

      @Override
      protected final void updateProgress(double workDone, double max) {
        super.updateProgress(workDone, max);
        evolutionaryProgressProperty.set(workDone / max);
      };

      @Override
      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateProgress(0.1d, 1d);
        updateMessage("Decomposing Concept Lattice...");
        final MatrixContext<Set<Integer>, Set<Integer>> cxt = dataset.layout.lattice.context.selection._reduced.clone();
        chainDecomposerG = new ChainDecomposer<Set<Integer>>(cxt.objectQuasiOrder().neighborhood());
        chainDecomposerM = new ChainDecomposer<Set<Integer>>(cxt.attributeQuasiOrder().neighborhood());
        if (includingLayout) {
          layouts.add(dataset.layout.clone());
          currentBest = dataset.layout;
        }
        updateProgress(0.2d, 1d);
        updateMessage("Setting up Evolution Engine...");
        initPopulation();
        updateProgress(0.3d, 1d);
        updateMessage("Evolving Layout...");
        evolvePopulation();
        updateProgress(1d, 1d);
        return null;
      }

      private final void initPopulation() {
        final Set<Future<?>> futures = new HashSet<Future<?>>();
        for (int i = includingLayout ? 1 : 0; i < populationSize; i++)
          futures.add(ConExpFX.instance.executor.tpe.submit(generate()));
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
            if (dataset.conceptGraph.polar()) {} else {
              final AdditiveConceptLayout<G, M> candidate =
                  new AdditiveConceptLayout<G, M>(dataset.layout.lattice, null, null, Type.HYBRID);
              final Set<Set<Set<Integer>>> chainsG = chainDecomposerG.randomChainDecomposition();
              final Set<Set<Set<Integer>>> chainsM = chainDecomposerM.randomChainDecomposition();
              final int v = chainsG.size();
              if (v == 1) {
                final Point3D seed = new Point3D(0d, 1d, 0d);
                for (G g : Collections2
                    .transform(Iterables.getOnlyElement(chainsG), candidate.lattice.context.selection._firstObject))
                  candidate.seedsG.put(g, seed);
              } else {
                final int vv = v * v;
                final BitSetFX usedX = new BitSetFX();
                for (Set<Set<Integer>> chain : chainsG) {
                  int _x;
                  for (_x = rng.nextInt(vv + 1); usedX.contains(_x) || usedX.contains(_x + 1)
                      || (_x != 0 && usedX.contains(_x - 1)); _x = rng.nextInt(vv + 1));
                  usedX.add(_x);
                  final int __x = _x - vv / 2;
                  final Point3D seed = new Point3D(
                      (double) __x,
                      (double) (rng.nextInt(Math.abs(__x + 1) + 1) + 1),
                      dataset.conceptGraph.threeDimensions() ? (double) (rng.nextInt(Math.abs(__x + 1) + 1) - __x / 2)
                          : 0d);
                  for (G g : Collections2.transform(chain, candidate.lattice.context.selection._firstObject))
                    candidate.seedsG.put(g, seed);
                }
              }
              final int w = chainsM.size();
              if (w == 1) {
                final Point3D seed = new Point3D(0d, 1d, 0d);
                for (M m : Collections2
                    .transform(Iterables.getOnlyElement(chainsM), candidate.lattice.context.selection._firstAttribute))
                  candidate.seedsM.put(m, seed);
              } else {
                final int ww = w * w;
                final BitSetFX usedX = new BitSetFX();
                for (Set<Set<Integer>> chain : chainsM) {
                  int _x;
                  for (_x = rng.nextInt(ww + 1); usedX.contains(_x) || usedX.contains(_x + 1)
                      || (_x != 0 && usedX.contains(_x - 1)); _x = rng.nextInt(ww + 1));
                  usedX.add(_x);
                  final int __x = _x - ww / 2;
                  final Point3D seed = new Point3D(
                      (double) __x,
                      (double) (rng.nextInt(Math.abs(__x + 1) + 1) + 1),
                      dataset.conceptGraph.threeDimensions() ? (double) (rng.nextInt(Math.abs(__x + 1) + 1) - __x / 2)
                          : 0d);
                  for (M m : Collections2.transform(chain, candidate.lattice.context.selection._firstAttribute))
                    candidate.seedsM.put(m, seed);
                }
              }
              synchronized (layouts) {
                layouts.add(candidate);
              }
              final double result = dataset.conflictDistance.apply(candidate).second();
              if (result > currentQuality || currentBest == null) {
                currentQuality = result;
                currentBest = candidate;
              }
            }
          }
        };
      }

      private final AdditiveConceptLayout<G, M>
          crossover(final AdditiveConceptLayout<G, M> l1, final AdditiveConceptLayout<G, M> l2) {
        final AdditiveConceptLayout<G, M> l = l1.clone();
        return null;
      }

      private final void evolvePopulation() {
        if (dataset.conceptGraph.polar()) {} else {
          for (int i = 0; i < generationCount; i++) {
//            if (currentBest != dataset.layout)
            dataset.layout.updateSeeds(currentBest.seedsG, currentBest.seedsM);
            updateProgress(0.3d + 0.7d * ((double) i) / (double) generationCount, 1d);
            updateMessage("Evolving Seeds: " + i + " of " + generationCount + " Generations...");
            evolveGeneration();
          }
//          try {
//            if (currentBest != dataset.layout)
          dataset.layout.updateSeeds(currentBest.seedsG, currentBest.seedsM);
//          } catch (Exception e) {
//            System.err.println(currentBest);
//            System.err.println(dataset.layout);
//            e.printStackTrace();
//          }
        }
      }

      private final void evolveGeneration() {
        if (dataset.conceptGraph.polar()) {} else {
          currentQuality = -1d;
          for (AdditiveConceptLayout<G, M> candidate : layouts) {
            LayoutEvolution<G, M>.Value v = new LayoutEvolution<G, M>(
                candidate,
                dataset.conflictDistance.apply(candidate).first(),
                ConceptMovement.INTENT_CHAIN_SEEDS,
                4d,
                4d,
                2,
                2,
                1,
                dataset.conflictDistance,
                ConExpFX.instance.executor.tpe).calculate();
            if (!candidate.updateSeeds(v.seedsG, v.seedsM)) {
//              if (rng.nextBoolean()) {
//                final Concept<G, M> c = candidate.seedsM
//                    .keySet()
//                    .parallelStream()
//                    .map(candidate.lattice.attributeConcepts::get)
//                    .filter(cn -> cn != null)
//                    .findAny()
//                    .get();
////                    candidate.lattice.attributeConcepts.get(Collections3.random(candidate.seeds.keySet(), rng));
//                v = new LayoutEvolution<G, M>(
//                    candidate,
//                    c,
//                    // TODO
//                    //                    ConceptMovement.LABEL_CHAIN_SEEDS,
//                    ConceptMovement.INTENT_CHAIN_SEEDS,
//                    4d,
//                    4d,
//                    2,
//                    2,
//                    1,
//                    dataset.conflictDistance,
//                    ConExpFX.instance.executor.tpe).calculate();
//              } else
              v = new LayoutEvolution<G, M>(
                  candidate,
                  Collections3.random(candidate.lattice.rowHeads(), rng),
                  ConceptMovement.INTENT_CHAIN_SEEDS,
                  4d,
                  4d,
                  2,
                  2,
                  1,
                  dataset.conflictDistance,
                  ConExpFX.instance.executor.tpe).calculate();
              candidate.updateSeeds(v.seedsG, v.seedsM);
            }
            if (v.result > currentQuality) {
              currentQuality = v.result;
              currentBest = candidate;
            }
          }
        }
      }
    };
  }
}
