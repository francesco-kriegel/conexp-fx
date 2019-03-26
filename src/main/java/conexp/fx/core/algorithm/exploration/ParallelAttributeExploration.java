package conexp.fx.core.algorithm.exploration;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import conexp.fx.core.algorithm.nextclosures.NextClosuresState;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.context.Context;
import conexp.fx.core.context.Implication;
import conexp.fx.core.math.SetClosureOperator;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.exploration.HumanExpertP;
import conexp.fx.gui.task.TimeTask;
import conexp.fx.gui.util.Platform2;

public class ParallelAttributeExploration {

  public static final <M> Set<Implication<String, M>> explore(
      final Context<String, M> cxt,
      final Set<Implication<String, M>> backgroundKnowledge,
      final Expert<String, M> expert,
      final ExecutorService executor,
      final Consumer<Implication<String, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Supplier<Boolean> isCancelled) {
    if (!cxt.models(backgroundKnowledge))
      throw new IllegalArgumentException("Background knowledge does not hold in formal context");
    final NextClosuresState<String, M, Set<M>> result = NextClosuresState.withHashSets(cxt.colHeads());
    final int maxCardinality = cxt.colHeads().size();
    // expert.onCancelRunnable.set(() -> result.cardinality = maxCardinality);
    final SetClosureOperator<M> clop = backgroundKnowledge == null || backgroundKnowledge.isEmpty()
        ? SetClosureOperator.fromImplications(result.implications, true, false)
        : SetClosureOperator.supremum(
            SetClosureOperator.fromImplications(result.implications, true, false),
            SetClosureOperator.fromImplications(backgroundKnowledge, false, false));
    for (; result.cardinality <= maxCardinality; result.cardinality++) {
      if (isCancelled.get())
        break;
      final double q = ((double) result.cardinality) / ((double) maxCardinality);
      final int p = (int) (100d * q);
      updateStatus.accept("current cardinality: " + result.cardinality + "/" + maxCardinality + " (" + p + "%)");
      updateProgress.accept(q);
      final Set<Future<?>> futures = Collections3.newConcurrentHashSet();
      result.candidates.keySet().parallelStream().filter(c -> c.size() == result.cardinality).forEach(candidate -> {
        futures.add(executor.submit(() -> {
          final Set<M> closure = clop.closure(candidate);
          if (closure.size() == candidate.size()) {
            final Set<String> candidateI = cxt.colAnd(candidate);
            final Set<M> candidateII = cxt.rowAnd(candidateI);
            if (candidateII.size() != candidate.size()) {
              candidateII.removeAll(candidate);
              final Implication<String, M> implication = new Implication<String, M>(candidate, candidateII, candidateI);
              try {
                Set<CounterExample<String, M>> counterExamples = Collections.emptySet();
                do {
                  counterExamples = expert.getCounterExamples(implication);
                  if (!counterExamples.isEmpty())
                    counterExamples.forEach(cex -> Platform2.runOnFXThreadAndWaitTryCatch(() -> {
                      cex.insertIn(cxt);
                      cex.addTo(implication);
                    }));
                } while (!implication.isTrivial() && !counterExamples.isEmpty());
              } catch (InterruptedException __) {
                result.cardinality = maxCardinality;
              }
              if (!implication.isTrivial()) {
                result.implications.add(implication);
                implicationConsumer.accept(implication);
              }
              final Set<M> intent = new HashSet<M>();
              intent.addAll(implication.getPremise());
              intent.addAll(implication.getConclusion());
              result.addNewCandidates(intent);
            } else
              result.addNewCandidates(candidate);
          } else
            result.candidates.put(closure, result.cardinality);
          result.candidates.remove(candidate);
        }));
      });
      for (Future<?> future : futures)
        try {
          future.get();
        } catch (InterruptedException | ExecutionException __) {}
    }
    updateStatus.accept(result.implications.size() + " implications found");
    return result.implications;
  }

  public static final <M> TimeTask<?> createExplorationTask(FCADataset<String, M> dataset) {
    return new TimeTask<Void>(dataset, "ParallelExploration") {

      @Override
      protected Void call() throws Exception {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        final HumanExpertP<M> humexp = new HumanExpertP<>(dataset.context.selection);
        Platform2.runOnFXThreadAndWaitTryCatch(() -> {
          humexp.init();
          humexp.show();
        });
        explore(
            dataset.context.getSelection(),
            Collections.emptySet(),
            humexp,
            ConExpFX.instance.executor.tpe,
            implication -> Platform2.runOnFXThread(() -> dataset.implications.add(implication)),
            // dataset.implications::add,
            status -> updateMessage(status),
            progress -> updateProgress(progress, 1d),
            () -> isCancelled());
        Platform2.runOnFXThreadAndWaitTryCatch(() -> humexp.hide());
        updateProgress(1d, 1d);
        return null;
      }
    };
  }

}
