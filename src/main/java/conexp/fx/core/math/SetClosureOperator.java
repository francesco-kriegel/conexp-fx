package conexp.fx.core.math;

import java.util.Arrays;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2023 Francesco Kriegel
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.NotImplementedException;

import conexp.fx.core.context.Context;
import conexp.fx.core.context.Implication;

@FunctionalInterface
public interface SetClosureOperator<M> extends Function<Set<M>, Set<M>> {

  @Override
  public default Set<M> apply(final Set<M> set) {
    return closure(set);
  }

  public Set<M> closure(Set<M> set);

  public default boolean close(final Set<M> set) {
    return !set.addAll(closure(set));
  }

  public default boolean isClosed(final Set<M> set) {
//    return set.containsAll(closure(set));
    return set.size() == closure(set).size();
  }

  @SafeVarargs
  public static <T> SetClosureOperator<T> infimum(final SetClosureOperator<T>... closureOperators) {
    return infimum(Arrays.asList(closureOperators));
  }

  public static <T> SetClosureOperator<T> infimum(final Iterable<SetClosureOperator<T>> closureOperators) {
    return set -> {
      final Set<T> closure = new HashSet<T>();
      final Iterator<SetClosureOperator<T>> iterator = closureOperators.iterator();
      if (iterator.hasNext())
        closure.addAll(iterator.next().closure(set));
      while (iterator.hasNext())
        closure.retainAll(iterator.next().closure(set));
      return closure;
    };
  }

  @SafeVarargs
  public static <T> SetClosureOperator<T> supremum(final SetClosureOperator<T>... closureOperators) {
    return supremum(Arrays.asList(closureOperators));
  }

  public static <T> SetClosureOperator<T> supremum(final Iterable<SetClosureOperator<T>> closureOperators) {
    return set -> {
      final Set<T> closure = new HashSet<T>();
      closure.addAll(set);
      boolean changed = true;
      while (changed) {
        changed = false;
        for (SetClosureOperator<T> clop : closureOperators)
          changed |= closure.addAll(clop.closure(closure));
      }
      return closure;
    };
//    @Override
//    public final boolean isClosed(final Set<M> set) {
//      return closureOperators.parallelStream().allMatch(
//          clop -> clop.isClosed(set));
//    }
  }

  public static <G, M> SetClosureOperator<M> fromContext(final Context<G, M> cxt) {
    return set -> cxt.rowAnd(cxt.colAnd(set));
  }

  public static <G, M> SetClosureOperator<M>
      joiningImplications(final Context<G, M> cxt, final Set<M> premises, final Set<M> conclusions) {
    return set -> {
      final HashSet<M> pintersection = new HashSet<>(set);
      pintersection.retainAll(premises);
      final Set<M> cattributes = cxt.rowAnd(cxt.colAnd(pintersection));
      cattributes.retainAll(conclusions);
      final Set<M> closure = new HashSet<>(cattributes);
      closure.addAll(set);
      return closure;
    };
  }

  public static <G, M, C extends Set<M>> C implicativeClosure(
      final Collection<Implication<G, M>> implications,
      final int firstPremiseSize,
      final boolean includePseudoClosures,
      final boolean parallel,
      final boolean bySize,
      final Function<Set<M>, C> supplier,
      final Set<M> set) {
    final C closure = supplier.apply(set);// new HashSet<M>(set);
    final AtomicBoolean changed = new AtomicBoolean(true);
    final Predicate<Implication<G, M>> p;
    if (includePseudoClosures)
      p = i -> closure.size() > i.getPremise().size() && closure.containsAll(i.getPremise());
    else
      p = i -> closure.size() >= i.getPremise().size() && closure.containsAll(i.getPremise());
    // && (closure.size() < i.getConclusion().size() || !closure.containsAll(i.getConclusion())))
    if (bySize) {
      int size;
      if (firstPremiseSize > 0) {
        size = closure.size();
        (parallel ? implications.parallelStream() : implications.stream())
            .filter(i -> firstPremiseSize <= i.getPremise().size())
            .filter(p)
            .sequential()
            .forEach(i -> closure.addAll(i.getConclusion()));
        changed.set(closure.size() != size);
      }
      while (changed.get()) {
        size = closure.size();
        changed.set(false);
        (parallel ? implications.parallelStream() : implications.stream())
            .filter(p)
            .sequential()
            .forEach(i -> closure.addAll(i.getConclusion()));
        changed.set(closure.size() != size);
      }
    } else {
      if (firstPremiseSize > 0) {
        (parallel ? implications.parallelStream() : implications.stream())
            .filter(i -> firstPremiseSize <= i.getPremise().size())
            .filter(p)
            .sequential()
            .forEach(i -> changed.set(closure.addAll(i.getConclusion()) || changed.get()));
      }
      while (changed.get()) {
        changed.set(false);
        (parallel ? implications.parallelStream() : implications.stream())
            .filter(p)
            .sequential()
            .forEach(i -> changed.set(closure.addAll(i.getConclusion()) || changed.get()));
      }
    }
    return closure;
  }

  public static <G, M, C extends Set<M>> SetClosureOperator<M> fromImplications(
      final Collection<Implication<G, M>> implications,
      final int firstPremiseSize,
      final boolean includePseudoClosures,
      final boolean parallel,
      final boolean bySize,
      final Function<Set<M>, C> supplier) {
    return set -> implicativeClosure(
        implications,
        firstPremiseSize,
        includePseudoClosures,
        parallel,
        bySize,
        supplier,
        set);
  }

  public static <G, M> SetClosureOperator<M> fromImplications(
      final Collection<Implication<G, M>> implications,
      final int firstPremiseSize,
      final boolean includePseudoClosures,
      final boolean parallel) {
    return fromImplications(implications, firstPremiseSize, includePseudoClosures, parallel, false, HashSet<M>::new);
  }

  public static <G, M> SetClosureOperator<M> fromImplications(
      final Collection<Implication<G, M>> implications,
      final boolean includePseudoClosures,
      final boolean parallel) {
    return fromImplications(implications, 0, includePseudoClosures, parallel);
  }

  public static <G, M> SetClosureOperator<M> fromImplications(final Collection<Implication<G, M>> implications) {
    return fromImplications(implications, false, true);
  }

  public static <G, M> SetClosureOperator<M>
      fromImplicationSetLinClosure(final Collection<Implication<G, M>> implications) {
    return set -> {

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
    };
  }

  public static <G, M> SetClosureOperator<M>
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
  public static <G, M> SetClosureOperator<M> byMaximalCardinality(final int maxCard, final Collection<M> baseSet) {
    return new SetClosureOperator<M>() {

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
  public static <G, M> SetClosureOperator<M> byMinimalSupport(final int minSupp, final Context<G, M> cxt) {
    return new SetClosureOperator<M>() {

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
  public static <G, M> SetClosureOperator<M> containsAllFrom(final Collection<M> elements, final Set<M> baseSet) {
    return new SetClosureOperator<M>() {

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
  public static <G, M> SetClosureOperator<M> isSubsetOf(final Collection<M> elements, final Set<M> baseSet) {
    return new SetClosureOperator<M>() {

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

  public static <M> SetClosureOperator<M> bottom() {
    return new SetClosureOperator<M>() {

      @Override
      public final boolean isClosed(final Set<M> set) {
        return true;
      }

      @Override
      public final boolean close(final Set<M> set) {
        return true;
      }

      @Override
      public final Set<M> closure(final Set<M> set) {
        return new HashSet<M>(set);
      }
    };
  }

  public static <M> SetClosureOperator<M> top(final Set<M> baseSet) {
    return new SetClosureOperator<M>() {

      @Override
      public final boolean isClosed(final Set<M> set) {
        return set.containsAll(baseSet) && baseSet.containsAll(set);
      }

      @Override
      public final boolean close(final Set<M> set) {
        set.retainAll(baseSet);
        return !set.addAll(baseSet);
      }

      @Override
      public final Set<M> closure(final Set<M> set) {
        return new HashSet<M>(baseSet);
      }
    };
  }

}
