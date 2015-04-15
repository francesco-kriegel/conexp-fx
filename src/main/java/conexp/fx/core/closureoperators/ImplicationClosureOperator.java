package conexp.fx.core.closureoperators;

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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import conexp.fx.core.implication.Implication;

public class ImplicationClosureOperator<G, M> implements ClosureOperator<M> {

  private final Set<Implication<G, M>> implications;
  private final boolean                includePseudoClosures;

  public ImplicationClosureOperator(final boolean concurrent, final boolean includePseudoClosures) {
    this(concurrent ? Collections.newSetFromMap(new ConcurrentHashMap<Implication<G, M>, Boolean>())
        : new HashSet<Implication<G, M>>(), includePseudoClosures);
  }

  public ImplicationClosureOperator(final Set<Implication<G, M>> implications, final boolean includePseudoClosures) {
    super();
    this.implications = implications;
    this.includePseudoClosures = includePseudoClosures;
  }

  public ImplicationClosureOperator(final boolean concurrent) {
    this(concurrent, false);
  }

  public ImplicationClosureOperator(final Set<Implication<G, M>> implications) {
    this(implications, false);
  }

  public final Set<Implication<G, M>> getImplications() {
    return this.implications;
  }

  @Override
  public boolean isClosed(Set<M> set) {
    return set.containsAll(closure(set));
  }

  @Override
  public boolean close(Set<M> set) {
    final Set<M> closure = closure(set);
    final boolean closed = set.equals(closure);
    if (!closed)
      set.addAll(closure);
    return closed;
  }

  @Override
  public Set<M> closure(Set<M> set) {
    final Set<M> closure = new HashSet<M>(set);
    boolean changed = true;
    while (changed) {
      changed = false;
      for (Implication<G, M> i : implications)
        if (closure.containsAll(i.getPremise()) && (!includePseudoClosures || closure.size() > i.getPremise().size()))
          changed |= closure.addAll(i.getConclusion());
    }
    return closure;
  }

}
