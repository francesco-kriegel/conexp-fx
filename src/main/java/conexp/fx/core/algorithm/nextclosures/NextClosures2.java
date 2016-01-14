package conexp.fx.core.algorithm.nextclosures;

import java.io.File;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import conexp.fx.core.algorithm.nextclosure.exploration.CounterExample;
import conexp.fx.core.algorithm.nextclosure.exploration.Expert;
import conexp.fx.core.closureoperators.ClosureOperator;
import conexp.fx.core.collections.BitSetSet2;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.Context;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.Implication;
import conexp.fx.core.importer.CXTImporter;
import conexp.fx.core.math.Math3;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.exploration.HumanExpertP;
import conexp.fx.gui.task.TimeTask;
import conexp.fx.gui.util.Platform2;

public final class NextClosures2 {

  public static final class Result<G, M> {

    public final Set<Concept<G, M>>     concepts     = Collections3.newConcurrentHashSet();
    public final Set<Implication<G, M>> implications = Collections3.newConcurrentHashSet();
    private final Set<Set<M>>           candidates   = Collections3.newConcurrentHashSet();
    private final Set<Set<M>>           processed    = Collections3.newConcurrentHashSet();
    private int                         cardinality  = 0;

    private Result() {
      candidates.add(new HashSet<M>());
    }

    private final boolean isNewIntent(final Set<M> s) {
      try {
        return processed.add(s);
      } catch (ConcurrentModificationException e) {
        return isNewIntent(s);
      }
    }

    private final void addNewCandidates(final Context<G, M> cxt, final Set<M> intent) {
      if (isNewIntent(intent))
        for (M m : Sets.difference(cxt.colHeads(), intent)) {
          final Set<M> candidateM = new HashSet<M>(intent);
          candidateM.add(m);
          candidates.add(candidateM);
        }
    }

  }

  public static final <T> Set<Set<T>>
      compute(final Set<T> baseSet, final ClosureOperator<T> clop, final boolean verbose, final ExecutorService tpe) {
    final Map<Set<T>, Set<T>> closures = new ConcurrentHashMap<>();
    final Set<Set<T>> candidates = Collections3.newConcurrentHashSet();
    candidates.add(new HashSet<T>());
    for (final AtomicInteger cardinality = new AtomicInteger(0); cardinality.get() < baseSet.size(); cardinality
        .incrementAndGet()) {
      System.out.println("current cardinality: " + cardinality.get());
      final long total = candidates.parallelStream().filter(c -> c.size() == cardinality.get()).count();
      final AtomicInteger current = new AtomicInteger(0);
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

  public static final <M> Result<String, M> explore(
      final Context<String, M> cxt,
      final Set<Implication<String, M>> backgroundKnowledge,
      final Expert<String, M> expert,
      final ExecutorService executor,
      final Consumer<Implication<String, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Callable<Boolean> isCancelled) {
    if (!cxt.models(backgroundKnowledge))
      throw new IllegalArgumentException("Background knowledge does not hold in formal context");
    final Result<String, M> result = new Result<String, M>();
    final int maxCardinality = cxt.colHeads().size();
//    expert.onCancelRunnable.set(() -> result.cardinality = maxCardinality);
    final ClosureOperator<M> clop = backgroundKnowledge == null || backgroundKnowledge.isEmpty()
        ? ClosureOperator.fromImplications(result.implications, true, false)
        : ClosureOperator.supremum(
            ClosureOperator.fromImplications(result.implications, true, false),
            ClosureOperator.fromImplications(backgroundKnowledge, false, false));
    for (; result.cardinality <= maxCardinality; result.cardinality++) {
      try {
        if (isCancelled.call())
          break;
      } catch (Exception __) {}
      final double q = ((double) result.cardinality) / ((double) maxCardinality);
      final int p = (int) (100d * q);
      updateStatus.accept("current cardinality: " + result.cardinality + "/" + maxCardinality + " (" + p + "%)");
      updateProgress.accept(q);
      final Set<Future<?>> futures = Collections3.newConcurrentHashSet();
      result.candidates.parallelStream().filter(c -> c.size() == result.cardinality).forEach(candidate -> {
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
                  counterExamples = expert.askForCounterExample(implication);
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
            result.candidates.add(closure);
          result.candidates.remove(candidate);
        }));
      });
      for (Future<?> future : futures)
        try {
          future.get();
        } catch (InterruptedException | ExecutionException __) {}
    }
    updateStatus.accept(result.implications.size() + " implications found");
    return result;
  }

  public static final <G, M> Result<G, M> compute(
      final Context<G, M> cxt,
      final ExecutorService executor,
      final Consumer<Concept<G, M>> conceptConsumer,
      final Consumer<Implication<G, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Callable<Boolean> isCancelled) {
    final Result<G, M> result = new Result<G, M>();
    final ClosureOperator<M> clop = ClosureOperator.fromImplications(result.implications, true, false);
    final int maxCardinality = cxt.colHeads().size();
    for (; result.cardinality <= maxCardinality; result.cardinality++) {
      try {
        if (isCancelled.call())
          break;
      } catch (Exception __) {}
      final double q = ((double) result.cardinality) / ((double) maxCardinality);
      final int p = (int) (100d * q);
      updateStatus.accept("current cardinality: " + result.cardinality + "/" + maxCardinality + " (" + p + "%)");
      updateProgress.accept(q);
      final Set<Future<?>> futures = Collections3.newConcurrentHashSet();
      result.candidates.parallelStream().filter(c -> c.size() == result.cardinality).forEach(candidate -> {
        futures.add(executor.submit(() -> {
          final Set<M> closure = clop.closure(candidate);
          if (closure.size() == candidate.size()) {
            final Set<G> candidateI = cxt.colAnd(candidate);
            final Set<M> candidateII = cxt.rowAnd(candidateI);
            if (result.isNewIntent(candidateII)) {
              final Concept<G, M> concept = new Concept<G, M>(candidateI, new HashSet<M>(candidateII));
              result.concepts.add(concept);
              conceptConsumer.accept(concept);
              for (M m : Sets.difference(cxt.colHeads(), candidateII)) {
                final Set<M> candidateM = new HashSet<M>(candidateII);
                candidateM.add(m);
                result.candidates.add(candidateM);
              }
            }
            if (candidateII.size() != candidate.size()) {
              candidateII.removeAll(candidate);
              final Implication<G, M> implication = new Implication<G, M>(candidate, candidateII, candidateI);
              result.implications.add(implication);
              implicationConsumer.accept(implication);
            }
          } else {
            result.candidates.add(closure);
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
    return result;
  }

  public static final <G, M> Result<G, M> compute(final Context<G, M> cxt, final ExecutorService executor) {
    return compute(cxt, executor, __ -> {} , __ -> {} , System.out::println, System.out::println, () -> false);
  }

  public static final <G, M> Result<G, M> compute(final Context<G, M> cxt, final int cores) {
    if (cores > Runtime.getRuntime().availableProcessors())
      throw new IllegalArgumentException(
          "Requested pool size is too large. VM has only " + Runtime.getRuntime().availableProcessors()
              + " available cpus, thus a thread pool with " + cores + " cores cannot be used here.");
//    final ThreadPoolExecutor tpe =
//        new ThreadPoolExecutor(cores, cores, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
//    tpe.prestartAllCoreThreads();
    final ExecutorService tpe = Executors.newWorkStealingPool(cores);
    final Result<G, M> result = compute(cxt, tpe);
//    tpe.purge();
    tpe.shutdown();
    return result;
  }

  public static final <G, M> Result<G, M> compute(final Context<G, M> cxt) {
    return compute(cxt, Runtime.getRuntime().availableProcessors() - 1);
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

  private static final class BitResult {

    private final Set<BitConcept>     concepts     = Collections3.newConcurrentHashSet();
    private final Set<BitImplication> implications = Collections3.newConcurrentHashSet();
    private final Set<BitSetSet2>     candidates   = Collections3.newConcurrentHashSet();
    private final Set<BitSetSet2>     processed    = Collections3.newConcurrentHashSet();
    private int                       cardinality  = 0;

    private BitResult() {
      candidates.add(new BitSetSet2());
    }

    private final boolean isNewIntent(final BitSetSet2 s) {
      try {
        return processed.add(s);
      } catch (ConcurrentModificationException __) {
        return isNewIntent(s);
      }
    }

    private final void addNewCandidates(final MatrixContext<?, ?> cxt, final BitSetSet2 intent) {
//      if (isNewIntent(intent))
      for (int m = 0; m < cxt.colHeads().size(); m++)
        if (!intent.contains(m)) {
          final BitSetSet2 candidateM = new BitSetSet2(intent);
          candidateM.add(m);
          candidates.add(candidateM);
        }
    }
  }

  private static final class BitImplication extends Pair<BitSetSet2, BitSetSet2> {

    public BitImplication(final BitSetSet2 premise, final BitSetSet2 conclusion) {
      super(premise, conclusion);
    }
  }

  private static final class BitConcept extends Pair<BitSetSet2, BitSetSet2> {

    public BitConcept(final BitSetSet2 premise, final BitSetSet2 conclusion) {
      super(premise, conclusion);
    }
  }

  private static final Function<BitSetSet2, BitSetSet2> bitClosure(final Set<BitImplication> implications) {
    return set -> {
      final BitSetSet2 x = new BitSetSet2(set);
      // if (set instanceof BitSetSet2)
      // x = (BitSetSet2) set;
      // else {
      // x = new BitSetSet2();
      // x.addAll(set);
      // }
      boolean changed = false;
      do {
        final BitSetSet2 y = x.clone();
//        implications.stream().forEach(i -> {
//          if (x.size() > i.x().size() && x.containsAll(i.x()))
//            x.addAll(i.y());
//        });
//        for (BitImplication i : implications)
//          if (x.size() > i.x().size() && x.containsAll(i.x()))
//            x.addAll(i.y());
        implications
            .parallelStream()
            .filter(i -> x.containsAll(i.x()) && x.size() != i.x().size())
            // .filter(i -> x.containsAll(i.x()) && !i.x().containsAll(x))
            // && (x.size() < i.y().size() || !x.containsAll(i.y())))
            .map(i -> i.y())
            .sequential()
            .forEach(x::addAll);
        changed = !x.containsAll(y);
      } while (changed);
      return x;
    };
  }

  public static final <G, M> Result<G, M> bitCompute(
      final Context<G, M> cxt,
      final ExecutorService executor,
      final Consumer<Concept<G, M>> conceptConsumer,
      final Consumer<Implication<G, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Callable<Boolean> isCancelled) {
    final MatrixContext<G, M> mcxt = cxt instanceof MatrixContext ? (MatrixContext<G, M>) cxt : cxt.clone();
    final BitResult b = new BitResult();
    final Function<BitSetSet2, BitSetSet2> cl = bitClosure(b.implications);
    final int max = mcxt.colHeads().size();
    for (; b.cardinality < max; b.cardinality++) {
      try {
        if (isCancelled.call())
          break;
      } catch (Exception __) {}
      final double q = ((double) b.cardinality) / ((double) max);
      final int p = (int) (100d * q);
      updateStatus.accept("current cardinality: " + b.cardinality + "/" + max + " (" + p + "%)");
      updateProgress.accept(q);
      final Set<Future<?>> fs = Collections3.newConcurrentHashSet();
      b.candidates.parallelStream().filter(c -> c.size() == b.cardinality).forEach(c -> fs.add(executor.submit(() -> {
        final BitSetSet2 d = cl.apply(c);
//        if (c.size() == d.size()) {
        if (c.containsAll(d)) {
          final BitSetSet2 c1 = (BitSetSet2) mcxt._colAnd(c);
          final BitSetSet2 c2 = (BitSetSet2) mcxt._rowAnd(c1);
          if (b.isNewIntent(c2)) {
            b.concepts.add(new BitConcept(c1, new BitSetSet2(c2)));
            b.addNewCandidates(mcxt, c2);
          }
//          if (c.size() != c2.size()) {
          if (!c.containsAll(c2)) {
            c2.removeAll(c);
            b.implications.add(new BitImplication(c, c2));
          }
        } else
          b.candidates.add(d);
        b.candidates.remove(c);
      })));
      for (Future<?> f : fs)
        try {
          f.get();
        } catch (InterruptedException | ExecutionException __) {}
    }
    updateStatus.accept(b.concepts.size() + " concepts, and " + b.implications.size() + " implications found");
    final Result<G, M> r = new Result<>();
    b.concepts
        .parallelStream()
        .map(c -> new Concept<G, M>(mcxt.rowHeads().getAll(c.x(), true), mcxt.colHeads().getAll(c.y(), true)))
        .forEach(conceptConsumer.andThen(r.concepts::add));
    b.implications
        .parallelStream()
        .map(i -> new Implication<G, M>(mcxt.colHeads().getAll(i.x(), true), mcxt.colHeads().getAll(i.y(), true)))
        .forEach(implicationConsumer.andThen(r.implications::add));
    return r;
  }

  public static final <G, M> Result<G, M> bitCompute(final Context<G, M> cxt, final ExecutorService executor) {
    return bitCompute(cxt, executor, __ -> {} , __ -> {} , System.out::println, System.out::println, () -> false);
  }

  public static final void main(String[] args) throws Exception {
//    final ExecutorService exe = new ForkJoinPool();
    final ExecutorService exe = Executors.newWorkStealingPool();
    final MatrixContext<String, String> cxt =
        CXTImporter.read(new File("/Volumes/Internal HD/Data/Contexts/huge/algorithms.cxt"));
    final int cycles = 1;
    long time = Long.MAX_VALUE, bittime = Long.MAX_VALUE;
    Result<String, String> result = null, bitresult = null;
    for (int i = 0; i < cycles; i++) {
      final long s = System.currentTimeMillis();
      bitresult = bitCompute(cxt, exe, __ -> {} , __ -> {} , __ -> {} , __ -> {} , () -> false);
      bittime = Math.min(bittime, System.currentTimeMillis() - s);
    }
    System.out.println("bittime = " + Math3.formatTime(bittime));
    for (int i = 0; i < cycles; i++) {
      final long s = System.currentTimeMillis();
      result = compute(cxt, exe, __ -> {} , __ -> {} , __ -> {} , __ -> {} , () -> false);
      time = Math.min(time, System.currentTimeMillis() - s);
    }
    System.out.println("time = " + Math3.formatTime(time));
    System.out.println(result.implications.size() + " : " + bitresult.implications.size());
    System.out.println(result.concepts.size() + " : " + bitresult.concepts.size());
    boolean equalSizes = result.concepts.size() == bitresult.concepts.size()
        && result.implications.size() == bitresult.implications.size();
    System.out.println(equalSizes ? "equals sizes of results" : "error");
    boolean equals =
        result.concepts.containsAll(bitresult.concepts) && result.implications.containsAll(bitresult.implications);
    System.out.println(equals ? "equal results" : "error");
    System.out.println(result.concepts.containsAll(bitresult.concepts));
    System.out.println(result.implications.containsAll(bitresult.implications));
    boolean equal = true;
    for (Implication<String, String> i : result.implications) {
      boolean found = false;
      for (Implication<String, String> j : bitresult.implications) {
//        found |= i.equals(j);
        found |= i.hashCode() == j.hashCode();
      }
      equal &= found;
    }
    System.out.println(equal);
    exe.shutdown();
  }

}
