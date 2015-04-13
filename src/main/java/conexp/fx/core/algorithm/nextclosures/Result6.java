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

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import conexp.fx.core.context.Concept;

public final class Result6<G, M> {

  public final Set<Concept<G, M>>    concepts     = Collections
                                                      .newSetFromMap(new ConcurrentHashMap<Concept<G, M>, Boolean>());
  public final Map<Set<M>, Set<M>>   implications = new ConcurrentHashMap<Set<M>, Set<M>>();
  public final Map<Set<M>, Set<G>>   supports     = new ConcurrentHashMap<Set<M>, Set<G>>();
  final Map<Set<M>, Integer> candidates   = new ConcurrentHashMap<Set<M>, Integer>();
  private final Set<Set<M>>          processed    = Collections
                                                      .newSetFromMap(new ConcurrentHashMap<Set<M>, Boolean>());
  int                        cardinality  = 0;

  public Result6() {
    candidates.put(new HashSet<M>(), 0);
  }

  private final boolean isClosed(final Set<M> candidate) {
    for (Entry<Set<M>, Set<M>> implication : implications.entrySet())
      if (candidate.size() > implication.getKey().size() && candidate.containsAll(implication.getKey())
          && !candidate.containsAll(implication.getValue()))
        return false;
    return true;
  }

  final Set<M> fastClosure(final Set<M> candidate, final int c) {
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

  final boolean addToProcessed(final Set<M> s) {
    try {
      return processed.add(s);
    } catch (ConcurrentModificationException e) {
      return addToProcessed(s);
    }
  }

}
