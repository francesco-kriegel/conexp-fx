package conexp.fx.core.algorithm.nextclosures;

/*-
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

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

public final class NextClosures2 {

  public static final <T> Set<Set<T>>
      compute(final Set<T> baseSet, final ClosureOperator<T> clop, final boolean verbose, final ExecutorService tpe) {
    final Map<Set<T>, Set<T>> closures = new ConcurrentHashMap<>();
    final Set<Set<T>> candidates = Collections3.newConcurrentHashSet();
    candidates.add(new HashSet<T>());
    for (final AtomicInteger cardinality = new AtomicInteger(0); cardinality.get() < baseSet.size(); cardinality
        .incrementAndGet()) {
      System.out.println("current cardinality: " + cardinality.get());
//      final long total = candidates.parallelStream().filter(c -> c.size() == cardinality.get()).count();
//      final AtomicInteger current = new AtomicInteger(0);
      while (!Collections2.filter(candidates, c -> c.size() == cardinality.get()).isEmpty()) {
        final Set<Future<?>> futures = Collections3.newConcurrentHashSet();
        final long count = candidates.parallelStream().filter(c -> c.size() == cardinality.get()).count();
        System.out.println("processing " + count + " candidates");
        for (Set<T> candidate : candidates)
          if (candidate.size() == cardinality.get()) {
            // candidates.parallelStream().filter(c -> c.size() == cardinality.get()).forEach(candidate -> {
            futures.add(tpe.submit(() -> {
              final Optional<Entry<Set<T>, Set<T>>> optional = closures
                  .entrySet()
                  .parallelStream()
                  .filter(e -> candidate.containsAll(e.getKey()) && e.getValue().containsAll(candidate))
                  .findAny();
              final Set<T> closure;
              if (optional.isPresent())
                closure = optional.get().getValue();
              else {
                closure = clop.closure(candidate);
//              System.out.println(cardinality.get() + " : " + current.incrementAndGet() + "/" + total + " : " + closure);
                closures.put(candidate, closure);
                Sets.difference(baseSet, closure).parallelStream().forEach(individual -> {
                  final Set<T> nextCandidate = Sets.newHashSet(closure);
                  nextCandidate.add(individual);
                  candidates.add(nextCandidate);
                });
              }
            }));
          }
        // });
        candidates.removeIf(c -> c.size() == cardinality.get());
        futures.forEach(f -> {
          try {
            f.get();
          } catch (Exception e) {}
        });
      }
    }
    return closures.values().parallelStream().collect(Collectors.toSet());
  }

  @SafeVarargs
  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> compute(
      final Context<G, M> cxt,
      final ExecutorService executor,
      final Consumer<Concept<G, M>> conceptConsumer,
      final Consumer<Implication<G, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Supplier<Boolean> isCancelled,
      final Collection<Implication<G, M>>... backgroundKnowledge) {
//    System.out.println("invalid background implications:");
//    Collections3.union(backgroundKnowledge).stream().filter(i -> !cxt.models(i)).forEach(System.out::println);;
//    System.out.println();
    if (!cxt.models(Collections3.union(backgroundKnowledge)))
      throw new RuntimeException("The background implications are not valid in the formal context.");
    final NextClosuresState<G, M, Set<M>> result = NextClosuresState.withHashSets(cxt.colHeads());
    final Function<Integer, ClosureOperator<M>> clop;
    if (backgroundKnowledge.length == 0)
      clop = i -> ClosureOperator.fromImplications(result.implications, i, true, true);
    else
      clop = i -> ClosureOperator.supremum(
          ClosureOperator.fromImplications(result.implications, i, true, true),
          ClosureOperator.fromImplications(Collections3.union(backgroundKnowledge), false, true));
//    final ClosureOperator<M> clop = ClosureOperator.fromImplications(result.implications, true, true);
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
      final Set<Set<M>> cc = result.getActualCandidates();
      cc.forEach(candidate -> {
        futures.add(executor.submit(() -> {
          final Set<M> closure = clop.apply(result.candidates.get(candidate)).closure(candidate);
//          if (closure.size() == candidate.size()) {
          if (closure.equals(candidate)) {
            final Set<G> candidateI = cxt.colAnd(candidate);
            final Set<M> candidateII = cxt.rowAnd(candidateI);
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
//          result.candidates.remove(candidate);
        }));
      });
      for (Future<?> future : futures)
        try {
          future.get();
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
      result.candidates.keySet().removeAll(cc);
    }
    updateStatus
        .accept(result.concepts.size() + " concepts, and " + result.implications.size() + " implications found");
    return result.getResultAndDispose();
  }

  @SafeVarargs
  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> compute(
      final Context<G, M> cxt,
      final ExecutorService executor,
      final Collection<Implication<G, M>>... backgroundKnowledge) {
    return compute(cxt, executor, __ -> {}, __ -> {}, __ -> {}, __ -> {}, () -> false, backgroundKnowledge);
  }

  @SafeVarargs
  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>>
      compute(final Context<G, M> cxt, final int cores, final Collection<Implication<G, M>>... backgroundKnowledge) {
    if (cores > Runtime.getRuntime().availableProcessors())
      throw new IllegalArgumentException(
          "Requested pool size is too large. VM has only " + Runtime.getRuntime().availableProcessors()
              + " available cpus, thus a thread pool with " + cores + " cores cannot be used here.");
//    final ThreadPoolExecutor tpe =
//        new ThreadPoolExecutor(cores, cores, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
//    tpe.prestartAllCoreThreads();
    final ExecutorService tpe = Executors.newWorkStealingPool(cores);
    final Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> result = compute(cxt, tpe, backgroundKnowledge);
//    tpe.purge();
    tpe.shutdown();
    return result;
  }

  @SafeVarargs
  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>>
      compute(final Context<G, M> cxt, final Collection<Implication<G, M>>... backgroundKnowledge) {
    return compute(cxt, Runtime.getRuntime().availableProcessors(), backgroundKnowledge);
  }

  public static final <G, M> TimeTask<?> createTask(FCADataset<G, M> dataset) {
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
            () -> isCancelled());
        updateProgress(1d, 1d);
        return null;
      }
    };
  }

}
