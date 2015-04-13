package conexp.fx.core.algorithm.nextclosure;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;

public final class NextClosure2 {

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
      for (M m : Lists.reverse(cxt.colHeads()))
        if (!candidate.contains(m)) {
          Set<M> AplusG = new HashSet<M>();
          for (M n : cxt.colHeads()) {
            if (m.equals(n)) {
              AplusG.add(m);
              break;
            }
            if (candidate.contains(n))
              AplusG.add(n);
          }
          AplusG = result.closure(AplusG);
          boolean lexicSmaller = true;
          for (M l : cxt.colHeads()) {
            if (l.equals(m))
              break;
            if (AplusG.contains(l) != candidate.contains(l)) {
              lexicSmaller = false;
              break;
            }
          }
          if (lexicSmaller) {
            candidate = AplusG;
            break;
          }
        }
    }
    result.concepts.add(new Concept<G, M>(cxt.colAnd(cxt.colHeads()), cxt.colHeads()));
    return result;
  }

}
