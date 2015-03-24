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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.context.MatrixContext;

public class NextClosuresMN2<G, M> {

  public static final class Result<G, M> {

    private final Map<Set<M>, Set<M>> implications;

    public Result() {
      this.implications = new HashMap<Set<M>, Set<M>>();
    }

    public final Set<M> closure(final Set<M> set) {
      final Set<M> cl = new HashSet<M>();
      for (Entry<Set<M>, Set<M>> e : implications.entrySet())
        if (set.containsAll(e.getKey()))
          cl.addAll(e.getValue());
      return cl;
    }

    public final Map<Set<M>, Set<M>> getImplications() {
      return implications;
    }

  }

  public static final class MatrixContextMN<G, M> {

    private final MatrixContext<G, M> cxt;
    private final Set<M>              premises;
    private final Set<M>              conclusions;
    private final MatrixContext<G, M> cxtM;
    private final MatrixContext<G, M> cxtN;

    public MatrixContextMN(final MatrixContext<G, M> cxt, final Set<M> premises, final Set<M> conclusions) {
      if (!Sets.intersection(premises, conclusions).isEmpty())
        throw new RuntimeException();
      this.cxt = cxt;
      this.premises = premises;
      this.conclusions = conclusions;
      this.cxtM = cxt.subRelation(cxt.rowHeads(), premises).clone();
      this.cxtN = cxt.subRelation(cxt.rowHeads(), conclusions).clone();
    }

    public final Set<M> closureMN(final Set<M> set) {
      if (!premises.containsAll(set))
        throw new RuntimeException();
      return new HashSet<M>(cxtN.rowAnd(new HashSet<G>(cxtM.colAnd(set))));
    }

    public final Set<M> intentM(final Set<M> set) {
      if (!premises.containsAll(set))
        throw new RuntimeException();
      return new HashSet<M>(cxtM.intent(set));
    }

  }

  public static final <G, M> NextClosuresMN2.Result<G, M> compute(
      final MatrixContext<G, M> cxt,
      final Set<M> premises,
      final Set<M> conclusions) {
    final MatrixContextMN<G, M> cxtMN = new MatrixContextMN<G, M>(cxt, premises, conclusions);
    final Result<G, M> result = new Result<G, M>();
    final Map<Integer, Set<Set<M>>> candidates = new HashMap<Integer, Set<Set<M>>>();
    for (int i = 0; i <= premises.size(); i++)
      candidates.put(i, new HashSet<Set<M>>());
    final Set<M> p = new HashSet<M>();
    candidates.get(0).add(p);
    final Set<M> c = new HashSet<M>(cxtMN.closureMN(p));
    if (!c.isEmpty()) // i.e. p->c does not follow from the empty set of implications
      result.implications.put(p, c);
    final Set<M> pmm = cxtMN.intentM(p);
    for (M m : Sets.difference(premises, pmm)) {
      Set<M> nextCandidate = new HashSet<M>(pmm);
      nextCandidate.add(m);
      candidates.get(pmm.size() + 1).add(nextCandidate);
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
          result.implications.put(s, closureMN);
        } else {
//          for (M m : Sets.difference(premises, s)) {
//            Set<M> nextCandidate = new HashSet<M>(s);
//            nextCandidate.add(m);
//            candidates.get(s.size() + 1).add(nextCandidate);
//          }
        }
        final Set<M> smm = cxtMN.intentM(s);
        for (M m : Sets.difference(premises, smm)) {
          Set<M> nextCandidate = new HashSet<M>(smm);
          nextCandidate.add(m);
          candidates.get(smm.size() + 1).add(nextCandidate);
        }
      }
    }
    return result;
  }
}
