package conexp.fx.core.algorithm.nextclosures;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;

public final class NextClosures2 {

  public static final class Result<G, M> {

    public final Set<Concept<G, M>>  concepts     = Collections
                                                      .newSetFromMap(new ConcurrentHashMap<Concept<G, M>, Boolean>());
    public final Map<Set<M>, Set<M>> implications = new ConcurrentHashMap<Set<M>, Set<M>>();
    private final Set<Set<M>>        candidates   = Collections.newSetFromMap(new ConcurrentHashMap<Set<M>, Boolean>());
    private int                      cardinality  = 0;

    public Result() {
      candidates.add(new HashSet<M>());
    }

    private final boolean isClosed(final Set<M> candidate) {
      for (Entry<Set<M>, Set<M>> implication : implications.entrySet())
        if (candidate.size() > implication.getKey().size() && candidate.containsAll(implication.getKey())
            && !candidate.containsAll(implication.getValue()))
          return false;
      return true;
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

  }

  public static final <G, M> NextClosures2.Result<G, M> compute(final MatrixContext<G, M> cxt) {
    final ThreadPoolExecutor tpe =
        new ThreadPoolExecutor(
            4,
            4,
            1000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    tpe.prestartAllCoreThreads();
    final Result<G, M> result = new Result<G, M>();
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
              if (candidateII.size() == candidate.size()) {
                result.concepts.add(new Concept<G, M>(cxt.colAnd(candidate), candidate));
              } else {
                candidateII.removeAll(candidate);
                result.implications.put(candidate, candidateII);
              }
              final Map<Set<M>, Boolean> newCandidates = new HashMap<Set<M>, Boolean>();
              for (M m : Sets.difference(cxt.colHeads(), candidate)) {
                final Set<M> candidateM = new HashSet<M>(candidate);
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
