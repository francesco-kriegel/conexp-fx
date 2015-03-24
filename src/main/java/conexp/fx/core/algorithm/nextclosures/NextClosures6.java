package conexp.fx.core.algorithm.nextclosures;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.Context;

public final class NextClosures6 {

  public static final class Result<G, M> {

    public final Set<Concept<G, M>>    concepts     = Collections
                                                        .newSetFromMap(new ConcurrentHashMap<Concept<G, M>, Boolean>());
    public final Map<Set<M>, Set<M>>   implications = new ConcurrentHashMap<Set<M>, Set<M>>();
    public final Map<Set<M>, Set<G>>   supports     = new ConcurrentHashMap<Set<M>, Set<G>>();
    private final Map<Set<M>, Integer> candidates   = new ConcurrentHashMap<Set<M>, Integer>();
    private final Set<Set<M>>          processed    = Collections
                                                        .newSetFromMap(new ConcurrentHashMap<Set<M>, Boolean>());
    private int                        cardinality  = 0;

    public Result() {
      candidates.put(new HashSet<M>(), 0);
    }

    private final boolean isClosed(final Set<M> candidate) {
      for (Entry<Set<M>, Set<M>> implication : implications.entrySet())
        if (candidate.size() > implication.getKey().size() && candidate.containsAll(implication.getKey())
            && !candidate.containsAll(implication.getValue()))
          return false;
      return true;
    }

    private final Set<M> fastClosure(final Set<M> candidate, final int c) {
      final Set<M> closure = new HashSet<M>(candidate);
      boolean changed = false;
      for (Entry<Set<M>, Set<M>> implication : implications.entrySet())
        if (implication.getKey().size() >= c && closure.size() > implication.getKey().size()
            && closure.containsAll(implication.getKey()) && !closure.containsAll(implication.getValue())) {
          closure.addAll(implication.getValue());
          changed = true;
        }
      while (changed) {
        changed = false;
        for (Entry<Set<M>, Set<M>> implication : implications.entrySet())
          if (closure.size() > implication.getKey().size() && closure.containsAll(implication.getKey())
              && !closure.containsAll(implication.getValue())) {
            closure.addAll(implication.getValue());
            changed = true;
          }
      }
      return closure;
    }

    private final Set<M> closure(final Set<M> candidate) {
      final Set<M> closure = new HashSet<M>(candidate);
      boolean changed = true;
      while (changed) {
        changed = false;
        for (Entry<Set<M>, Set<M>> implication : implications.entrySet())
          if (closure.size() > implication.getKey().size() && closure.containsAll(implication.getKey())
              && !closure.containsAll(implication.getValue())) {
            closure.addAll(implication.getValue());
            changed = true;
          }
      }
      return closure;
    }

    private final boolean addToProcessed(final Set<M> s) {
      try {
        return processed.add(s);
      } catch (ConcurrentModificationException e) {
        return addToProcessed(s);
      }
    }

  }

  public static final <G, M> NextClosures6.Result<G, M> compute(
      final Context<G, M> cxt,
      final boolean verbose,
      final ThreadPoolExecutor tpe) {
    if (verbose)
      System.out.println("NextClosures running on " + tpe.getCorePoolSize() + " - " + tpe.getMaximumPoolSize()
          + " cores...");
    final Result<G, M> result = new Result<G, M>();
    final int maxCardinality = cxt.colHeads().size();
    for (; result.cardinality <= maxCardinality; result.cardinality++) {
      if (verbose) {
        final int p = (int) ((100f * (float) result.cardinality) / ((float) maxCardinality));
        System.out.print("current cardinality: " + result.cardinality + "/" + maxCardinality + " (" + p + "%)");
      }
      final Collection<Set<M>> candidatesN =
          new HashSet<Set<M>>(Collections2.filter(
              result.candidates.keySet(),
              candidate -> candidate.size() == result.cardinality));
//          new Predicate<Set<M>>() {
//
//            @Override
//            public final boolean apply(final Set<M> candidate) {
//              return candidate.size() == result.cardinality;
//            }
//          }));
      if (verbose)
        System.out.println("     " + candidatesN.size() + " candidates will be processed...");
      final Set<Future<?>> futures = new HashSet<Future<?>>();
      for (final Set<M> candidate : candidatesN) {
        futures.add(tpe.submit(new Runnable() {

          @Override
          public final void run() {
            final Set<M> closure = result.fastClosure(candidate, result.candidates.get(candidate));
            if (closure.equals(candidate)) {
              final Set<G> candidateI = new HashSet<G>(cxt.colAnd(candidate));
              final Set<M> candidateII = new HashSet<M>(cxt.rowAnd(candidateI));
              if (result.addToProcessed(candidateII)) {
                for (M m : Sets.difference(cxt.colHeads(), candidateII)) {
                  final Set<M> candidateM = new HashSet<M>(candidateII);
                  candidateM.add(m);
                  result.candidates.put(candidateM, 0);
                }
              }
              if (candidateII.size() == candidate.size()) {
                result.concepts.add(new Concept<G, M>(candidateI, candidate));
              } else {
                result.concepts.add(new Concept<G, M>(candidateI, candidateII));
                candidateII.removeAll(candidate);
                result.implications.put(candidate, candidateII);
                result.supports.put(candidate, candidateI);
              }
            } else {
              result.candidates.put(closure, result.cardinality);
            }
          }
        }));
      }
      for (Future<?> future : futures)
        try {
          future.get();
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
      result.candidates.keySet().removeAll(candidatesN);
    }
    if (verbose) {
      System.out.println(result.concepts.size() + " concepts found");
      System.out.println(result.implications.size() + " implications found");
    }
    return result;
  }

  public static final <G, M> NextClosures6.Result<G, M> compute(
      final Context<G, M> cxt,
      final boolean verbose,
      final int cores) {
    if (cores > Runtime.getRuntime().availableProcessors())
      throw new IllegalArgumentException("Requested pool size is too large. VM has only "
          + Runtime.getRuntime().availableProcessors() + " available cpus, thus a thread pool with " + cores
          + " cores cannot be used here.");
    final ThreadPoolExecutor tpe =
        new ThreadPoolExecutor(cores, cores, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    tpe.prestartAllCoreThreads();
    final Result<G, M> result = compute(cxt, verbose, tpe);
    tpe.purge();
    tpe.shutdown();
    return result;
  }

  public static final <G, M> NextClosures6.Result<G, M> compute(final Context<G, M> cxt, final boolean verbose) {
    final int maxc = Runtime.getRuntime().availableProcessors();
    final int cores = maxc < 9 ? maxc : (maxc * 3) / 4;
    return compute(cxt, verbose, cores);
  }

}
