package conexp.fx.core.algorithm.nextclosures;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;

public final class NextClosures4 {

  public static final <G, M> Result4<G, M> compute(final MatrixContext<G, M> cxt) {
    final ThreadPoolExecutor tpe =
        new ThreadPoolExecutor(
            4,
            4,
            1000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    tpe.prestartAllCoreThreads();
    final Result4<G, M> result = new Result4<G, M>();
    final int maxCardinality = cxt.colHeads().size();
    for (; result.cardinality <= maxCardinality; result.cardinality++) {
//      System.out.println("current cardinality: " + result.cardinality);
      final Collection<Set<M>> candidatesN =
          new HashSet<Set<M>>(Collections2.filter(result.candidates, new Predicate<Set<M>>() {

            @Override
            public final boolean apply(final Set<M> candidate) {
              return candidate.size() == result.cardinality;
            }
          }));
//      System.out.println(candidatesN.size() + " candidates will be processed...");
      result.candidates.removeAll(candidatesN);
      final Set<Future<?>> futures = new HashSet<Future<?>>();
      for (final Set<M> candidate : candidatesN) {
        futures.add(tpe.submit(new Runnable() {

          @Override
          public final void run() {
            final Set<M> closure = result.closure(candidate);
            if (closure.equals(candidate)) {
              final Set<M> candidateII = new HashSet<M>(cxt.intent(candidate));
              if (result.addToProcessed(candidateII)) {
                final Map<Set<M>, Boolean> newCandidates = new HashMap<Set<M>, Boolean>();
                for (M m : Sets.difference(cxt.colHeads(), candidateII)) {
                  final Set<M> candidateM = new HashSet<M>(candidateII);
                  candidateM.add(m);
                  newCandidates.put(result.closure(candidateM), true);
                }
                // find minimal ones among new candidates
                for (Set<M> c2 : newCandidates.keySet())
                  for (Set<M> c1 : newCandidates.keySet())
                    if (c2.size() > c1.size() && c2.containsAll(c1)) {
                      newCandidates.put(c2, false);
                      break;
                    }
                for (Entry<Set<M>, Boolean> e : newCandidates.entrySet())
                  if (e.getValue())
                    result.candidates.add(e.getKey());
              }
              if (candidateII.size() == candidate.size()) {
                result.concepts.add(new Concept<G, M>(cxt.colAnd(candidate), candidate));
              } else {
                candidateII.removeAll(candidate);
                result.implications.put(candidate, candidateII);
              }
            } else {
              result.candidates.add(closure);
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
    }
    tpe.purge();
    tpe.shutdown();
    return result;
  }

}
