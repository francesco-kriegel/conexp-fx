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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
        final Set<M> closure = closure(set);
        final boolean closed = set.equals(closure);
        if (!closed)
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
            if (closure.containsAll(i.getPremise()))
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

  public static <G, M> ClosureOperator<M>
      fromImplicationSetLinClosure(final Collection<Implication<G, M>> implications) {
    return new ClosureOperator<M>() {

      @Override
      public boolean isClosed(Set<M> set) {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean close(Set<M> set) {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public Set<M> closure(Set<M> set) {
        // TODO Auto-generated method stub
        return null;
      }

    };
  }

  public static <G, M> Set<M> linClosure(final Set<M> set, final Collection<Implication<G, M>> implications) {

    // Initialization
    final Map<Implication<G, M>, Integer> count = new HashMap<Implication<G, M>, Integer>();
    final Map<M, Set<Implication<G, M>>> list = new HashMap<M, Set<Implication<G, M>>>();
    for (M attribute : set)
      list.put(
          attribute,
          new HashSet<Implication<G, M>>());
    for (Implication<G, M> implication : implications) {
      for (M attribute : implication.getPremise())
        list.put(
            attribute,
            new HashSet<Implication<G, M>>());
      for (M attribute : implication.getConclusion())
        list.put(
            attribute,
            new HashSet<Implication<G, M>>());
    }
//    final Multimap<M, Implication<G, M>> list = HashMultimap.create();
    final Set<M> newdep = new HashSet<M>();
    final Set<M> update = new HashSet<M>();
    for (Implication<G, M> implication : implications) {
      count.put(
          implication,
          implication.getPremise().size());
      if (implication.getPremise().isEmpty())
        newdep.addAll(implication.getConclusion());
      for (M attribute : implication.getPremise()) {
        if (!list.containsKey(attribute))
          list.put(
              attribute,
              new HashSet<Implication<G, M>>());
        list.get(
            attribute).add(
            implication);
//        list.put(
//            attribute,
//            implication);
      }
    }
    newdep.addAll(set);
    update.addAll(set);

    // Computation
    while (!update.isEmpty()) {
      final M attribute = update.iterator().next();
      update.remove(attribute);
      for (Implication<G, M> implication : list.get(attribute)) {
        final Integer previous = count.get(implication);
        count.put(
            implication,
            previous - 1);
        if (count.get(implication) == 0) {
          final Set<M> add = new HashSet<M>();
          add.addAll(implication.getConclusion());
          add.removeAll(newdep);
          newdep.addAll(add);
          update.addAll(add);
        }
      }
    }

    return newdep;
  }

  public static <G, M> Set<M> naiveClosure(final Set<M> set, final Collection<Implication<G, M>> implications) {
    final Set<M> closure = new HashSet<M>();
    closure.addAll(set);
    boolean changed = true;
    while (changed) {
      changed = false;
      for (Implication<G, M> implication : implications)
        if (closure.containsAll(implication.getPremise()))
          changed |= closure.addAll(implication.getConclusion());
    }
    return closure;
  }

  public static <G, M> Set<M> naiveClosure2(final Set<M> set, final Collection<Implication<G, M>> implications) {
    return fromImplicationSet(
        implications).closure(
        set);
  }

  public static <G, M> Set<M> optimizedClosure(final Set<M> set, final Collection<Implication<G, M>> implications) {
    final Set<M> closure = new HashSet<M>();
    closure.addAll(set);
    final Set<Implication<G, M>> impls = new HashSet<Implication<G, M>>();
    impls.addAll(implications);
    boolean changed = true;
    while (changed) {
      changed = false;
      final Iterator<Implication<G, M>> it = impls.iterator();
      while (it.hasNext()) {
        final Implication<G, M> impl = it.next();
        if (closure.containsAll(impl.getPremise())) {
          changed |= closure.addAll(impl.getConclusion());
          it.remove();
        }
      }
    }
    return closure;
  }

  public class BooleanProperty {

    private boolean value;

    private BooleanProperty(final boolean initialValue) {
      super();
      this.value = initialValue;
    }

    private void setValue(final boolean value) {
      this.value = value;
    }

    private boolean getValue() {
      return this.value;
    }

  }

  public static <G, M> Set<M> naiveClosureP(final Set<M> set, final Collection<Implication<G, M>> implications) {
    final Set<M> closure = new HashSet<M>();
    closure.addAll(set);
    final Set<M> closureP = Collections.synchronizedSet(closure);
    final BooleanProperty changed = new BooleanProperty(true);
    while (changed.getValue()) {
      changed.setValue(false);
      implications.parallelStream().forEach(
          implication -> {
            if (closureP.containsAll(implication.getPremise()))
              if (closureP.addAll(implication.getConclusion()))
                if (changed.getValue() == false)
                  changed.setValue(true);
          });
    }
    return closure;
  }

  public static <G, M> Set<M> optimizedClosureP(final Set<M> set, final Collection<Implication<G, M>> implications) {
    final Set<M> closure = new HashSet<M>();
    closure.addAll(set);
    final Set<M> closureP = Collections.synchronizedSet(closure);
    final Set<Implication<G, M>> impls = new HashSet<Implication<G, M>>();
    impls.addAll(implications);
    final BooleanProperty changed = new BooleanProperty(true);
    while (changed.getValue()) {
      changed.setValue(false);
      final Set<Implication<G, M>> toBeRemoved = Collections.synchronizedSet(new HashSet<Implication<G, M>>());
      impls.parallelStream().forEach(
          implication -> {
            if (closureP.containsAll(implication.getPremise()))
              if (closureP.addAll(implication.getConclusion())) {
                toBeRemoved.add(implication);
                if (changed.getValue() == false)
                  changed.setValue(true);
              }
          });
      impls.removeAll(toBeRemoved);
    }
    return closure;
  }

  public static <G, M> ClosureOperator<M> fromImplicationSetDowlingGalier(
      final Collection<Implication<G, M>> implications) {
    return new ClosureOperator<M>() {

      @Override
      public boolean isClosed(Set<M> set) {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean close(Set<M> set) {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public Set<M> closure(Set<M> set) {
        // TODO Auto-generated method stub
        return null;
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
        return cxt.colAnd(
            set).size() >= minSupp || set.containsAll(cxt.colHeads());
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
