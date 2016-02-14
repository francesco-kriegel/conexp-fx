package conexp.fx.core.algorithm.exploration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import conexp.fx.core.algorithm.nextclosures.NextClosures2.Result;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.context.Context;
import conexp.fx.core.context.Implication;
import conexp.fx.core.math.ClosureOperator;
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
    final Result<String, M> result = new Result<String, M>();
    final int maxCardinality = cxt.colHeads().size();
    // expert.onCancelRunnable.set(() -> result.cardinality = maxCardinality);
    final ClosureOperator<M> clop = backgroundKnowledge == null || backgroundKnowledge.isEmpty()
        ? ClosureOperator.fromImplications(result.implications, true, false)
        : ClosureOperator.supremum(
            ClosureOperator.fromImplications(result.implications, true, false),
            ClosureOperator.fromImplications(backgroundKnowledge, false, false));
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
              result.addNewCandidates(cxt, intent);
            } else
              result.addNewCandidates(cxt, candidate);
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
