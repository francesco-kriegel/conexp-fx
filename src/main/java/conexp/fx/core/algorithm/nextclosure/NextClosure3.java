package conexp.fx.core.algorithm.nextclosure;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;

public final class NextClosure3 {

  public static final class Result<G, M> {

    public final Set<Concept<G, M>>  concepts     = new HashSet<Concept<G, M>>();
    public final Map<Set<M>, Set<M>> implications = new HashMap<Set<M>, Set<M>>();

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

  public static final <G, M> Result<G, M> compute(final MatrixContext<G, M> cxt) {
    ThreadPoolExecutor tpe =
        new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            1000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    tpe.prestartAllCoreThreads();
    final Result<G, M> result = new Result<G, M>();
    Set<M> candidate = new HashSet<M>();
    while (candidate.size() < cxt.colHeads().size()) {
      final Set<M> closure = new HashSet<M>(cxt.intent(candidate));
      if (candidate.size() == closure.size()) {
        result.concepts.add(new Concept<G, M>(cxt.colAnd(candidate), candidate));
      } else {
        closure.removeAll(candidate);
        result.implications.put(candidate, closure);
      }
      final Set<M> cand = candidate;
      final Map<M, Future<Set<M>>> futures = new HashMap<M, Future<Set<M>>>();
      for (final M m : Lists.reverse(cxt.colHeads())) {
        if (!cand.contains(m)) {
          futures.put(m, tpe.submit(new Callable<Set<M>>() {

            @Override
            public Set<M> call() {
              Set<M> AplusG = new HashSet<M>();
              for (M n : cxt.colHeads()) {
                if (m.equals(n)) {
                  AplusG.add(m);
                  break;
                }
                if (cand.contains(n))
                  AplusG.add(n);
              }
              AplusG = result.closure(AplusG);
              boolean lexicSmaller = true;
              for (M l : cxt.colHeads()) {
                if (l.equals(m))
                  break;
                if (AplusG.contains(l) != cand.contains(l)) {
                  lexicSmaller = false;
                  break;
                }
              }
              if (lexicSmaller)
                return AplusG;
              return null;
            }
          }));
        }
      }
      for (final M m : Lists.reverse(cxt.colHeads())) {
        if (futures.containsKey(m)) {
          try {
            final Set<M> f = futures.get(m).get();
            if (f != null) {
              for (Future<Set<M>> ff : futures.values())
                ff.cancel(true);
              tpe.purge();
              candidate = f;
              break;
            }
          } catch (InterruptedException | ExecutionException e) {}
        }
      }
    }
    tpe.purge();
    tpe.shutdown();
    result.concepts.add(new Concept<G, M>(cxt.colAnd(cxt.colHeads()), cxt.colHeads()));
    return result;
  }

}
