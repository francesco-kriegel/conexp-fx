package conexp.fx.core.closureoperators;

import java.util.Arrays;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.apache.commons.lang3.NotImplementedException;

import conexp.fx.core.context.Context;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.Implication;

public interface ClosureOperator<M> {

  public boolean isClosed(Set<M> set);

  public boolean close(Set<M> set);

  public Set<M> closure(Set<M> set);

  public static <T> ClosureOperator<T> definedBy(final Function<Set<T>, Set<T>> closure) {
    return new AClosureOperator<T>() {

      @Override
      public final Set<T> closure(final Set<T> set) {
        return closure.apply(set);
      }

    };
  }

  @SafeVarargs
  public static <T> ClosureOperator<T> infimum(final ClosureOperator<T>... closureOperators) {
    return infimum(Arrays.asList(closureOperators));
  }

  public static <T> ClosureOperator<T> infimum(final Iterable<ClosureOperator<T>> closureOperators) {
    return definedBy(set -> {
      final Set<T> closure = new HashSet<T>();
      final Iterator<ClosureOperator<T>> iterator = closureOperators.iterator();
      if (iterator.hasNext())
        set.addAll(iterator.next().closure(set));
      while (iterator.hasNext())
        set.retainAll(iterator.next().closure(set));
      return closure;
    });
  }

  @SafeVarargs
  public static <T> ClosureOperator<T> supremum(final ClosureOperator<T>... closureOperators) {
    return supremum(Arrays.asList(closureOperators));
  }

  public static <T> ClosureOperator<T> supremum(final Iterable<ClosureOperator<T>> closureOperators) {
    return definedBy(set -> {
      final Set<T> closure = new HashSet<T>();
      closure.addAll(set);
      boolean changed = true;
      while (changed) {
        changed = false;
        for (ClosureOperator<T> clop : closureOperators)
          changed |= closure.addAll(clop.closure(closure));
      }
      return closure;
    });
//    @Override
//    public final boolean isClosed(final Set<M> set) {
//      return closureOperators.parallelStream().allMatch(
//          clop -> clop.isClosed(set));
//    }
  }

//  /**
//   * @param implications
//   * @return a closure operator, that computes the closure w.r.t. the given implication set.
//   */
//  public static <G, M> ClosureOperator<M>
//      fromImplicationSet(final Collection<Implication<G, M>> implications, final boolean includePseudoClosures) {
//    return definedBy(
//        set -> {
//          final Set<M> closure = new HashSet<M>(set);
//          boolean changed = true;
//          while (changed) {
//            changed = false;
//            for (Implication<G, M> i : implications)
//              if ((includePseudoClosures ? closure.size() > i.getPremise().size()
//                  : closure.size() >= i.getPremise().size())
//                  && closure.containsAll(
//                      i.getPremise())
//                  && (closure.size() < i.getConclusion().size() || !closure.containsAll(
//                      i.getConclusion())))
//                changed |= closure.addAll(
//                    i.getConclusion());
//          }
//          return closure;
//        });
//  }
//
//  public static <G, M> ClosureOperator<M> fromImplicationSetOptimized(
//      final Collection<Implication<G, M>> implications,
//      final boolean includePseudoClosures) {
//    return definedBy(
//        set -> {
//          final List<Implication<G, M>> is = new ArrayList<Implication<G, M>>();
//          final Set<M> closure = new HashSet<M>(set);
//          boolean changed = true;
//          while (changed) {
//            changed = false;
//            for (Implication<G, M> i : implications)
//              if (!is.contains(
//                  i)
//                  && (includePseudoClosures ? closure.size() > i.getPremise().size()
//                      : closure.size() >= i.getPremise().size())
//                  && closure.containsAll(
//                      i.getPremise())
//                  && (closure.size() < i.getConclusion().size() || !closure.containsAll(
//                      i.getConclusion()))) {
//                changed |= closure.addAll(
//                    i.getConclusion());
//                is.add(
//                    i);
//              }
//          }
//          return closure;
//        });
//  }

  public static <G, M> ClosureOperator<M> fromContext(final MatrixContext<G, M> cxt) {
    return definedBy(set -> cxt.rowAnd(cxt.colAnd(set)));
  }

  public static <G, M> ClosureOperator<M> fromImplications(
      final Collection<Implication<G, M>> implications,
      final boolean includePseudoClosures,
      final boolean parallel) {
    return definedBy(set -> {
      final Set<M> closure = new HashSet<M>(set);
      final AtomicBoolean changed = new AtomicBoolean(true);
      while (changed.get()) {
        changed.set(false);
        (parallel ? implications.parallelStream() : implications.stream())
            .filter(
                i -> (includePseudoClosures ? closure.size() > i.getPremise().size()
                    : closure.size() >= i.getPremise().size()) && closure.containsAll(i.getPremise())
                    && (closure.size() < i.getConclusion().size() || !closure.containsAll(i.getConclusion())))
            .sequential()
            .forEach(i -> changed.set(closure.addAll(i.getConclusion())));
      }
      return closure;
    });
  }

  public static <G, M> ClosureOperator<M>
      fromImplicationSetLinClosure(final Collection<Implication<G, M>> implications) {
    return definedBy(set -> {

      // Initialization
      final Map<Implication<G, M>, Integer> count = new HashMap<Implication<G, M>, Integer>();
      final Map<M, Set<Implication<G, M>>> list = new HashMap<M, Set<Implication<G, M>>>();
      for (M attribute : set)
        list.put(attribute, new HashSet<Implication<G, M>>());
      for (Implication<G, M> implication : implications) {
        for (M attribute : implication.getPremise())
          list.put(attribute, new HashSet<Implication<G, M>>());
        for (M attribute : implication.getConclusion())
          list.put(attribute, new HashSet<Implication<G, M>>());
      }
      // final Multimap<M, Implication<G, M>> list = HashMultimap.create();
      final Set<M> newdep = new HashSet<M>();
      final Set<M> update = new HashSet<M>();
      for (Implication<G, M> implication : implications) {
        count.put(implication, implication.getPremise().size());
        if (implication.getPremise().isEmpty())
          newdep.addAll(implication.getConclusion());
        for (M attribute : implication.getPremise()) {
          if (!list.containsKey(attribute))
            list.put(attribute, new HashSet<Implication<G, M>>());
          list.get(attribute).add(implication);
          // list.put(
          // attribute,
          // implication);
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
          count.put(implication, previous - 1);
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
    });
  }

  public static <G, M> ClosureOperator<M>
      fromImplicationSetDowlingGalier(final Collection<Implication<G, M>> implications) {
    throw new NotImplementedException("");
  }

//  public Set<M> closure(final Set<M> attributes) {
//    final Set<M> closure = new HashSet<M>(attributes);
//    final Map<Implication<M>, Integer> count = new HashMap<Implication<M>, Integer>();
//    final Multimap<M, Implication<M>> list = HashMultimap.<M, Implication<M>> create();
//    for (Implication<M> impl : this) {
//      final int psize = impl.getPremise().size();
//      count.put(impl, psize);
//      if (psize == 0)
//        closure.addAll(impl.getConclusion());
//      else
//        for (M im : impl.getPremise())
//          list.put(im, impl);
//    }
//    List<M> update = new LinkedList<M>(attributes);
//    while (!update.isEmpty()) {
//      final M m = update.remove(0);
//      for (Implication<M> impl : list.get(m)) {
//        final int newCount = count.get(impl) - 1;
//        count.put(impl, newCount);
//        if (newCount == 0) {
//          final Set<M> add = new HashSet<M>(Sets.difference(impl.getConclusion(), closure));
//          closure.addAll(add);
//          update.addAll(add);
//        }
//      }
//    }
//    return closure;
//  }

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
