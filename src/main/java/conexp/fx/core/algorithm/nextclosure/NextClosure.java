package conexp.fx.core.algorithm.nextclosure;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import conexp.fx.core.algorithm.nextclosure.exploration.LexicalOrder;
import conexp.fx.core.closureoperators.ClosureOperator;
import conexp.fx.core.collections.either.Either;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.Implication;

public class NextClosure {

  public static <G, M> Iterable<Concept<G, M>> intents(final MatrixContext<G, M> cxt) {
    final Function<Set<M>, Concept<G, M>> clop = set -> {
      final Set<G> extent = cxt.colAnd(
          set);
      final Set<M> intent = cxt.rowAnd(
          extent);
      return new Concept<G, M>(extent, intent);
    };
    return enumerate(
        cxt.colHeads(),
        clop.apply(
            new HashSet<M>()),
        clop,
        concept -> concept.getIntent());
  }

  public static <G, M> Iterable<Either<Concept<G, M>, Implication<G, M>>> implications(final MatrixContext<G, M> cxt) {
    final Set<Implication<G, M>> implications = Sets.newHashSet();
    final ClosureOperator<M> clop = ClosureOperator.fromImplications(
        implications,
        true,
        true);
    final Function<Set<M>, Either<Concept<G, M>, Implication<G, M>>> clop2 = set -> {
      final Set<M> quasiIntent = clop.closure(
          set);
      final Set<G> extent = cxt.colAnd(
          quasiIntent);
      final Set<M> intent = cxt.rowAnd(
          extent);
      if (quasiIntent.size() != intent.size()) {
        intent.removeAll(
            quasiIntent);
        final Implication<G, M> implication = new Implication<G, M>(quasiIntent, intent, extent);
        implications.add(
            implication);
        return Either.<Concept<G, M>, Implication<G, M>> ofRight(
            implication);
      } else
        return Either.<Concept<G, M>, Implication<G, M>> ofLeft(
            new Concept<G, M>(extent, intent));
    };
    return enumerate(
        cxt.colHeads(),
        clop2.apply(
            new HashSet<M>()),
        clop2,
        e -> e.getLeft().isPresent() ? e.getLeft().get().getIntent() : e.getRight().get().getPremise());
  }

  public static <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>>
      conceptsAndImplications(final MatrixContext<G, M> cxt) {
    final Set<Concept<G, M>> concepts = Sets.newHashSet();
    final Set<Implication<G, M>> implications = Sets.newHashSet();
    enumerate(
        cxt.colHeads(),
        ClosureOperator.fromImplications(
            implications,
            true,
            true)).forEach(
                quasiIntent -> {
                  final Set<G> extent = cxt.colAnd(
                      quasiIntent);
                  final Set<M> intent = cxt.rowAnd(
                      extent);
                  if (quasiIntent.size() != intent.size()) {
                    intent.removeAll(
                        quasiIntent);
                    implications.add(
                        new Implication<G, M>(quasiIntent, intent, extent));
                  } else
                    concepts.add(
                        new Concept<G, M>(extent, intent));
                });
    return Pair.of(
        concepts,
        implications);
  }

  public static <T> Iterable<Set<T>> enumerate(final SetList<T> base, final ClosureOperator<T> clop) {
    return enumerate(
        base,
        new HashSet<T>(),
        clop::closure,
        t -> t);
  }

  public static <T, U> Iterable<U> enumerate(
      final SetList<T> base,
      final U first,
      final Function<Set<T>, U> clop,
      final Function<U, Set<T>> inverse) {
    return new Iterable<U>() {

      public final Iterator<U> iterator() {

        return new UnmodifiableIterator<U>() {

          private U nextClosure = first;
          private boolean isFirst = true;

          public final boolean hasNext() {
            return isFirst || inverse.apply(
                nextClosure).size() < base.size();
          }

          public final U next() {
            if (isFirst)
              isFirst = false;
            else if (inverse.apply(
                nextClosure).size() < base.size()) {
              for (T m : Lists.reverse(
                  base)) {
                final U s = clop.apply(
                    LexicalOrder.oplus(
                        base,
                        inverse.apply(
                            nextClosure),
                        m));
                if (LexicalOrder.isSmaller(
                    base,
                    inverse.apply(
                        nextClosure),
                    inverse.apply(
                        s),
                    m)) {
                  nextClosure = s;
                  return nextClosure;
                }
              }
              throw new RuntimeException();
            }
            return nextClosure;
          }
        };
      }
    };
  }
}
