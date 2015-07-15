package conexp.fx.core.algorithm.nextclosures;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import conexp.fx.core.closureoperators.ClosureOperator;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.Context;
import conexp.fx.core.implication.Implication;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.FCADataset;
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

  }

  public static final <T> Set<Set<T>> compute(
      final Set<T> baseSet,
      final ClosureOperator<T> clop,
      final boolean verbose,
      final AbstractExecutorService tpe) {
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

  public static final <G, M> Result<G, M> compute(
      final Context<G, M> cxt,
      final AbstractExecutorService executor,
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
      } catch (Exception _) {}
      final double q = ((double) result.cardinality) / ((double) maxCardinality);
      final int p = (int) (100d * q);
      updateStatus.accept("current cardinality: " + result.cardinality + "/" + maxCardinality + " (" + p + "%)");
      updateProgress.accept(q);
      final Set<Future<?>> futures = Collections3.newConcurrentHashSet();
      result.candidates.parallelStream().filter(c -> c.size() == result.cardinality).forEach(candidate -> {
        futures.add(executor.submit(() -> {
          final Set<M> closure = clop.closure(candidate);
          if (closure.size() == candidate.size()) {
            final Set<G> candidateI = new HashSet<G>(cxt.colAnd(candidate));
            final Set<M> candidateII = new HashSet<M>(cxt.rowAnd(candidateI));
            if (result.isNewIntent(candidateII)) {
              final Concept<G, M> concept = new Concept<G, M>(candidateI, candidateII);
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

  public static final <G, M> Result<G, M> compute(final Context<G, M> cxt, final AbstractExecutorService executor) {
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
    final ForkJoinPool tpe = new ForkJoinPool(cores);
    final Result<G, M> result = compute(cxt, tpe);
//    tpe.purge();
    tpe.shutdown();
    return result;
  }

  public static final <G, M> Result<G, M> compute(final Context<G, M> cxt) {
    return compute(cxt, Runtime.getRuntime().availableProcessors() - 1);
  }

  public static final <G, M> TimeTask<?> createTask(FCADataset<G, M> dataset) {
    return new TimeTask<Void>(dataset, "NextClosures") {

      @Override
      protected Void call() throws Exception {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        compute(
            dataset.context,
            ConExpFX.instance.executor.tpe,
            concept -> Platform2.runOnFXThread(() -> dataset.concepts.add(concept)),
            implication -> Platform2.runOnFXThread(() -> dataset.implications.add(implication)),
            status -> updateMessage(status),
            progress -> updateProgress(progress, 1d),
            () -> isCancelled());
        updateProgress(1d, 1d);
        return null;
      }
    };
  }

}
