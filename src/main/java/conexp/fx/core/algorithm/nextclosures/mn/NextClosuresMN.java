package conexp.fx.core.algorithm.nextclosures.mn;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
