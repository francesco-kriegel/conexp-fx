package conexp.fx.core.algorithm.nextclosures;

/*-
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

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> compute(
      final Set<M> baseSet,
      final ClosureOperator<M> clop,
      final Function<Set<M>, Set<G>> extension,
      final ExecutorService executor,
      final Consumer<Concept<G, M>> conceptConsumer,
      final Consumer<Implication<G, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Supplier<Boolean> isCancelled) {
    final int max = baseSet.size();
    final NextClosuresState<G, M, Set<M>> state = NextClosuresState.withHashSets(baseSet);
//    final Function<BitSetFX, BitSetFX> cl = bitClosure(b.implications);
    for (; state.cardinality <= max; state.cardinality++) {
      if (isCancelled.get())
        break;
      final double q = ((double) state.cardinality) / ((double) max);
      final int p = (int) (100d * q);
      updateStatus.accept("current cardinality: " + state.cardinality + "/" + max + " (" + p + "%)");
      updateProgress.accept(q);
      final Set<Future<?>> fs = Collections3.newConcurrentHashSet();
      final Set<Set<M>> cc = state.getActualCandidates();
      cc.forEach(c -> fs.add(executor.submit(() -> {
        final Set<M> d = ClosureOperator
            .implicativeClosure(state.implications, state.getFirstPremiseSize(c), true, true, true, HashSet::new, c);
        if (c.containsAll(d)) {
//        if (c.containsAll(d)) {
//          final BitSetFX c1 = mcxt._colAnd(c);
//          final BitSetFX c2 = mcxt._rowAnd(c1);
          final Set<M> c2 = clop.closure(c);
          if (state.isNewIntent(c2)) {
            state.concepts.add(new Concept<G, M>(extension.apply(c2), c2));
            state.addNewCandidates(c2);
          }
          if (!c.containsAll(c2)) {
//          if (!c.containsAll(c2)) {
            c2.removeAll(c);
            state.implications.add(new Implication<G, M>(c, c2));
          }
        } else
          state.addCandidate(d);
//        state.candidates.remove(c);
      })));
      for (Future<?> f : fs)
        try {
          f.get();
        } catch (InterruptedException | ExecutionException __) {}
      state.candidates.keySet().removeAll(cc);
    }
    updateStatus.accept(state.concepts.size() + " concepts, and " + state.implications.size() + " implications found");
//    final NextClosuresState<G, M, Set<M>> r = NextClosuresState.withHashSets(cxt.colHeads());
//    state.concepts
//        .parallelStream()
//        .map(
//            c -> new Concept<G, M>(
//                mcxt.rowHeads().getAll(c.getExtent(), true),
//                mcxt.colHeads().getAll(c.getIntent(), true)))
//        .forEach(conceptConsumer.andThen(r.concepts::add));
//    state.implications
//        .parallelStream()
//        .map(
//            i -> new Implication<G, M>(
//                mcxt.colHeads().getAll(i.getPremise(), true),
//                mcxt.colHeads().getAll(i.getConclusion(), true)))
//        .forEach(implicationConsumer.andThen(r.implications::add));
//    return r.getResultAndDispose();
    return state.getResultAndDispose();
  }

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>>
      compute(final Set<M> baseSet, final ClosureOperator<M> clop, final Function<Set<M>, Set<G>> extension) {
    return compute(
        baseSet,
        clop,
        extension,
        Executors.newWorkStealingPool(),
        __ -> {},
        __ -> {},
        __ -> {},
        __ -> {},
        () -> false);
  }

  public static <G, M> Set<Implication<G, M>> transformToJoiningImplications(
      final Context<G, M> cxt,
      final Set<M> premises,
      final Set<M> conclusions,
      final Set<Implication<G, M>> implications) {
    final Set<Implication<G, M>> joiningImplications = new HashSet<>();
    for (Implication<G, M> implication : implications) {
      final Set<M> premise = new HashSet<>(implication.getPremise());
      premise.retainAll(premises);
      final Set<M> conclusion = cxt.rowAnd(cxt.colAnd(premise));
      conclusion.retainAll(conclusions);
      joiningImplications.add(new Implication<G, M>(premise, conclusion));
    }
    return joiningImplications;
  }

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
      clop = i -> ClosureOperator
          .supremum(
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
