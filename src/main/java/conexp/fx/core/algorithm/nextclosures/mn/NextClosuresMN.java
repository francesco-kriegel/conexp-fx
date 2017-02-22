package conexp.fx.core.algorithm.nextclosures.mn;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.context.MatrixContext;

public class NextClosuresMN<G, M> {

  public static final <G, M> ResultMN<G, M> compute(
      final MatrixContext<G, M> cxt,
      final Set<M> premises,
      final Set<M> conclusions) {
    final MatrixContextMN<G, M> cxtMN = new MatrixContextMN<G, M>(cxt, premises, conclusions);
    final ResultMN<G, M> result = new ResultMN<G, M>();
    final Set<Set<M>> candidates = new HashSet<Set<M>>();
    final Set<Set<M>> candidates2 = new HashSet<Set<M>>();
    final Set<M> p = new HashSet<M>();
    candidates.add(p);
    final Set<M> c = new HashSet<M>(cxtMN.closureMN(p));
    if (!c.isEmpty()) // i.e. p->c does not follow from the empty set of implications
      result.implications.put(
          p,
          c);
    for (int i = 1; i < premises.size(); i++) {
      System.out.println("size " + i);
      candidates2.clear();
      for (Set<M> s : candidates) {
        for (M m : Sets.difference(
            premises,
            s)) {
          Set<M> t = new HashSet<M>(s);
          t.add(m);
          candidates2.add(t);
        }
      }
      for (Set<M> s : candidates2) {
        if (s.size() != i)
          throw new RuntimeException();
        final Set<M> closureMN = cxtMN.closureMN(s);
        final Set<M> closureL = result.closure(s);
        if (!closureL.containsAll(closureMN)) // i.e. s is M-N-pseudo-closed
          result.implications.put(
              s,
              closureMN);
      }
      candidates.clear();
      candidates.addAll(candidates2);
    }
    return result;
  }
}
