package conexp.fx.core.algorithm.nextclosures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.context.MatrixContext;

public class NextClosuresMN<G, M> {

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
//      System.out.println(cxtM);
//      System.out.println(cxtN);
    }

    public final Set<M> closureMN(final Set<M> set) {
      if (!premises.containsAll(set))
        throw new RuntimeException();
      return new HashSet<M>(cxtN.rowAnd(new HashSet<G>(cxtM.colAnd(set))));
    }

  }

  public static final <G, M> NextClosuresMN.Result<G, M> compute(
      final MatrixContext<G, M> cxt,
      final Set<M> premises,
      final Set<M> conclusions) {
    final MatrixContextMN<G, M> cxtMN = new MatrixContextMN<G, M>(cxt, premises, conclusions);
    final Result<G, M> result = new Result<G, M>();
    final Set<Set<M>> candidates = new HashSet<Set<M>>();
    final Set<Set<M>> candidates2 = new HashSet<Set<M>>();
    final Set<M> p = new HashSet<M>();
    candidates.add(p);
    final Set<M> c = new HashSet<M>(cxtMN.closureMN(p));
    if (!c.isEmpty()) // i.e. p->c does not follow from the empty set of implications
      result.implications.put(p, c);
    for (int i = 1; i < premises.size(); i++) {
      System.out.println("size " + i);
      candidates2.clear();
      for (Set<M> s : candidates) {
        for (M m : Sets.difference(premises, s)) {
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
          result.implications.put(s, closureMN);
      }
      candidates.clear();
      candidates.addAll(candidates2);
    }
    return result;
  }
}
