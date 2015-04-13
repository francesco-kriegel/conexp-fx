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

public final class Result4<G, M> {

  public final Set<Concept<G, M>>  concepts     = Collections
                                                    .newSetFromMap(new ConcurrentHashMap<Concept<G, M>, Boolean>());
  public final Map<Set<M>, Set<M>> implications = new ConcurrentHashMap<Set<M>, Set<M>>();
  final Set<Set<M>>        candidates   = Collections.newSetFromMap(new ConcurrentHashMap<Set<M>, Boolean>());
  private final Set<Set<M>>        processed    = Collections.newSetFromMap(new ConcurrentHashMap<Set<M>, Boolean>());
  int                      cardinality  = 0;

  public Result4() {
    candidates.add(new HashSet<M>());
  }

  private final boolean isClosed(final Set<M> candidate) {
    for (Entry<Set<M>, Set<M>> implication : implications.entrySet())
      if (candidate.size() > implication.getKey().size() && candidate.containsAll(implication.getKey())
          && !candidate.containsAll(implication.getValue()))
        return false;
    return true;
  }

  final Set<M> closure(final Set<M> candidate) {
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

  protected final boolean addToProcessed(final Set<M> s) {
    try {
      return processed.add(s);
    } catch (ConcurrentModificationException e) {
      return addToProcessed(s);
    }
  }

}
