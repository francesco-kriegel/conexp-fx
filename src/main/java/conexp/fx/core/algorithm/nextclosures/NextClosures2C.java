package conexp.fx.core.algorithm.nextclosures;

/*-
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.Context;
import conexp.fx.core.context.Implication;
import conexp.fx.core.math.ClosureOperator;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.task.TimeTask;
import conexp.fx.gui.util.Platform2;

public final class NextClosures2C {

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> compute(
      final Context<G, M> cxt,
      final ExecutorService executor,
      final Consumer<Concept<G, M>> conceptConsumer,
      final Consumer<Implication<G, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Supplier<Boolean> isCancelled,
      final ClosureOperator<M> constraint) {
    final NextClosuresState<G, M, Set<M>> result = NextClosuresState.withHashSets(cxt.colHeads());
    result.candidates.clear();
    final HashSet<M> firstCandidate = new HashSet<M>();
    constraint.close(firstCandidate);
    final int firstCardinality = firstCandidate.size();
    result.cardinality = firstCardinality;
    result.candidates.put(firstCandidate, firstCardinality);
//    final ClosureOperator<M> clop = ClosureOperator.fromImplications(result.implications, true, true);
    final ClosureOperator<M> sup = ClosureOperator.supremum(ClosureOperator.fromContext(cxt), constraint);
    final int maxCardinality = cxt.colHeads().size();
    for (; result.cardinality <= maxCardinality; result.cardinality++) {
      try {
        if (isCancelled.get())
          break;
      } catch (Exception __) {}
      final double q = ((double) result.cardinality) / ((double) maxCardinality);
      final int p = (int) (100d * q);
      updateStatus.accept("current cardinality: " + result.cardinality + "/" + maxCardinality + " (" + p + "%)");
      updateProgress.accept(q);
      final Set<Future<?>> futures = Collections3.newConcurrentHashSet();
      result.candidates.keySet().parallelStream().filter(c -> c.size() == result.cardinality).forEach(candidate -> {
        futures.add(executor.submit(() -> {
          final Set<M> closure =
              ClosureOperator
                  .supremum(
                      ClosureOperator
                          .fromImplications(result.implications, result.candidates.get(candidate), true, true),
                      constraint)
                  .closure(candidate);
          if (closure.size() == candidate.size()) {
            final Set<M> candidateII = sup.closure(candidate);
            final Set<G> candidateI = cxt.colAnd(candidate);
            if (result.isNewIntent(candidateII)) {
              final Concept<G, M> concept = new Concept<G, M>(candidateI, new HashSet<M>(candidateII));
              result.concepts.add(concept);
              conceptConsumer.accept(concept);
              result.addNewCandidates(candidateII);
            }
            if (candidateII.size() != candidate.size()) {
              candidateII.removeAll(candidate);
              final Implication<G, M> implication = new Implication<G, M>(candidate, candidateII, candidateI);
              result.implications.add(implication);
              implicationConsumer.accept(implication);
            }
          } else {
            result.candidates.put(closure, result.cardinality);
          }
          result.candidates.remove(candidate);
        }));
      });
      for (Future<?> future : futures)
        try {
          future.get();
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
    }
    updateStatus
        .accept(result.concepts.size() + " concepts, and " + result.implications.size() + " implications found");
    return result.getResultAndDispose();
  }

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>>
      compute(final Context<G, M> cxt, final ExecutorService executor, final ClosureOperator<M> constraint) {
    return compute(
        cxt,
        executor,
        __ -> {},
        __ -> {},
        System.out::println,
        System.out::println,
        () -> false,
        constraint);
  }

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>>
      compute(final Context<G, M> cxt, final int cores, final ClosureOperator<M> constraint) {
    if (cores > Runtime.getRuntime().availableProcessors())
      throw new IllegalArgumentException(
          "Requested pool size is too large. VM has only " + Runtime.getRuntime().availableProcessors()
              + " available cpus, thus a thread pool with " + cores + " cores cannot be used here.");
//    final ThreadPoolExecutor tpe =
//        new ThreadPoolExecutor(cores, cores, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
//    tpe.prestartAllCoreThreads();
    final ExecutorService tpe = Executors.newWorkStealingPool(cores);
    final Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> result = compute(cxt, tpe, constraint);
//    tpe.purge();
    tpe.shutdown();
    return result;
  }

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>>
      compute(final Context<G, M> cxt, final ClosureOperator<M> constraint) {
    return compute(cxt, Runtime.getRuntime().availableProcessors() - 1, constraint);
  }

  public static final <G, M> TimeTask<?> createTask(FCADataset<G, M> dataset, final ClosureOperator<M> constraint) {
    return new TimeTask<Void>(dataset, "NextClosures") {

      @Override
      protected Void call() throws Exception {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        compute(
            dataset.context.getSelection(),
            ConExpFX.instance.executor.tpe,
            concept -> Platform2.runOnFXThread(() -> dataset.concepts.add(concept)),
            implication -> Platform2.runOnFXThread(() -> dataset.implications.add(implication)),
            // dataset.concepts::add,
            // dataset.implications::add,
            status -> updateMessage(status),
            progress -> updateProgress(progress, 1d),
            () -> isCancelled(),
            constraint);
        updateProgress(1d, 1d);
        return null;
      }
    };
  }

}
