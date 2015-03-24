package conexp.fx.core.closureoperators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import conexp.fx.core.context.Context;
import conexp.fx.core.implication.Implication;

public interface ClosureOperator<M> {

  public boolean isClosed(Set<M> set);

  public boolean close(Set<M> set);

  public Set<M> closure(Set<M> set);

  /**
   * @param implications
   * @return a closure operator, that computes the closure w.r.t. the given implication set.
   */
  public static <G, M> ClosureOperator<M> fromImplicationSet(final Collection<Implication<G, M>> implications) {
    return new ClosureOperator<M>() {

      @Override
      public boolean close(final Set<M> set) {
        Set<M> closure = closure(set);
        boolean closed = set.equals(closure);
        set.addAll(closure);
        return closed;
      }

      @Override
      public Set<M> closure(final Set<M> set) {
        final Set<M> closure = new HashSet<M>(set);
        boolean changed = true;
        while (changed) {
          changed = false;
          for (Implication<G, M> i : implications)
            if (set.containsAll(i.getPremise()))
              changed |= closure.addAll(i.getConclusion());
        }
        return closure;
      }

      @Override
      public boolean isClosed(final Set<M> set) {
        return set.equals(closure(set));
      }

    };
  }

  /**
   * @param maxCard
   * @param baseSet
   * @return a closure operator, whose closed sets are those that do not exceed the given maximal cardinality, or
   *         contains all elements from the base set.
   */
  public static <G, M> ClosureOperator<M> byMaximalCardinality(final int maxCard, final Collection<M> baseSet) {
    return new ClosureOperator<M>() {

      @Override
      public boolean isClosed(final Set<M> set) {
        return set.size() <= maxCard || set.containsAll(baseSet);
      }

      @Override
      public boolean close(final Set<M> set) {
        if (isClosed(set))
          return true;
        set.addAll(baseSet);
        return false;
      }

      @Override
      public Set<M> closure(final Set<M> set) {
        if (isClosed(set))
          return new HashSet<M>(set);
        return new HashSet<M>(baseSet);
      }

    };
  }

  /**
   * @param minSupp
   * @param cxt
   * @return a closure operator, whose closed sets are those that have an extent with at least minSupp elements in the
   *         given formal context cxt, or contain all elements from the context's codomain.
   */
  public static <G, M> ClosureOperator<M> byMinimalSupport(final int minSupp, final Context<G, M> cxt) {
    return new ClosureOperator<M>() {

      @Override
      public boolean isClosed(final Set<M> set) {
        return cxt.colAnd(set).size() >= minSupp || set.containsAll(cxt.colHeads());
      }

      @Override
      public boolean close(final Set<M> set) {
        if (isClosed(set))
          return true;
        set.addAll(cxt.colHeads());
        return false;
      }

      @Override
      public Set<M> closure(final Set<M> set) {
        if (isClosed(set))
          return new HashSet<M>(set);
        return new HashSet<M>(cxt.colHeads());
      }

    };
  }

  /**
   * @param elements
   * @param baseSet
   * @return a closure operator, whose closed sets are those that contain all given elements.
   */
  public static <G, M> ClosureOperator<M> containsAllFrom(final Collection<M> elements, final Set<M> baseSet) {
    return new ClosureOperator<M>() {

      @Override
      public boolean isClosed(final Set<M> set) {
        return set.containsAll(elements);
      }

      @Override
      public boolean close(final Set<M> set) {
        if (isClosed(set))
          return true;
        set.addAll(elements);
        return false;
      }

      @Override
      public Set<M> closure(final Set<M> set) {
        if (isClosed(set))
          return new HashSet<M>(set);
        Set<M> result = new HashSet<M>(set);
        result.addAll(elements);
        return result;
      }

    };
  }

  /**
   * @param elements
   * @param baseSet
   * @return a closure operator, whose closed sets are those, that are subsets of elements, or contain all elements from
   *         the baseSet.
   */
  public static <G, M> ClosureOperator<M> isSubsetOf(final Collection<M> elements, final Set<M> baseSet) {
    return new ClosureOperator<M>() {

      @Override
      public boolean isClosed(final Set<M> set) {
        return elements.containsAll(set);
      }

      @Override
      public boolean close(final Set<M> set) {
        if (isClosed(set))
          return true;
        set.addAll(baseSet);
        return false;
      }

      @Override
      public Set<M> closure(final Set<M> set) {
        if (isClosed(set))
          return new HashSet<M>(set);
        return new HashSet<M>(baseSet);
      }

    };
  }

}
