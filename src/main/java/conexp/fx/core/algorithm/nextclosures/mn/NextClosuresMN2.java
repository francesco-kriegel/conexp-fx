package conexp.fx.core.algorithm.nextclosures.mn;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.context.MatrixContext;

public class NextClosuresMN2<G, M> {

  public static final <G, M> ResultMN<G, M> compute(
      final MatrixContext<G, M> cxt,
      final Set<M> premises,
      final Set<M> conclusions) {
    final MatrixContextMN<G, M> cxtMN = new MatrixContextMN<G, M>(cxt, premises, conclusions);
    final ResultMN<G, M> result = new ResultMN<G, M>();
    final Map<Integer, Set<Set<M>>> candidates = new HashMap<Integer, Set<Set<M>>>();
    for (int i = 0; i <= premises.size(); i++)
      candidates.put(
          i,
          new HashSet<Set<M>>());
    final Set<M> p = new HashSet<M>();
    candidates.get(
        0).add(
        p);
    final Set<M> c = new HashSet<M>(cxtMN.closureMN(p));
    if (!c.isEmpty()) // i.e. p->c does not follow from the empty set of implications
      result.implications.put(
          p,
          c);
    final Set<M> pmm = cxtMN.intentM(p);
    for (M m : Sets.difference(
        premises,
        pmm)) {
      Set<M> nextCandidate = new HashSet<M>(pmm);
      nextCandidate.add(m);
      candidates.get(
          pmm.size() + 1).add(
          nextCandidate);
    }
    for (int i = 1; i < premises.size(); i++) {
//      System.out.println("size " + i);
      for (Set<M> s : candidates.get(i)) {
//        System.out.println(s);
        if (s.size() != i)
          throw new RuntimeException();
        final Set<M> closureMN = cxtMN.closureMN(s);
        final Set<M> closureL = result.closure(s);
        if (!closureL.containsAll(closureMN)) { // i.e. s is M-N-pseudo-closed
          result.implications.put(
              s,
              closureMN);
        } else {
//          for (M m : Sets.difference(premises, s)) {
//            Set<M> nextCandidate = new HashSet<M>(s);
//            nextCandidate.add(m);
//            candidates.get(s.size() + 1).add(nextCandidate);
//          }
        }
        final Set<M> smm = cxtMN.intentM(s);
        for (M m : Sets.difference(
            premises,
            smm)) {
          Set<M> nextCandidate = new HashSet<M>(smm);
          nextCandidate.add(m);
          candidates.get(
              smm.size() + 1).add(
              nextCandidate);
        }
      }
    }
    return result;
  }
}
