package conexp.fx.core.layout;

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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.ujmp.core.collections.set.BitSetSet;
import org.ujmp.core.util.RandomSimple;

import com.google.common.base.Predicate;

import conexp.fx.core.collections.BitSetSet2;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.relation.MatrixRelation;

public final class ChainDecomposer<E> {

  private final MatrixRelation<E, E> neighborhood;
  private final int                  num;
  private final Random               rng = new RandomSimple();

  public ChainDecomposer(final MatrixRelation<E, E> neighborhood) {
    this.neighborhood = neighborhood;
    this.num = neighborhood.rowHeads().size();
  }

  public final Set<Set<E>> randomChainDecomposition() {
    final BitSetSet2 available = new BitSetSet2();
    available.getBitSet().set(0, num);
    final Set<Set<E>> chains = new HashSet<Set<E>>();
    while (!available.isEmpty())
      chains.add(nextChain(available));
    return chains;
  }

  private final Set<E> nextChain(final BitSetSet available) {
    final Set<E> chain = new HashSet<E>();
    for (int i = nextMinimalElement(available); i != -1; i = nextChainElement(i, available))
      chain.add(neighborhood.rowHeads().get(i));
    return chain;
  }

  private final int nextMinimalElement(final BitSetSet available) {
    final int i = Collections3.random(available, new Predicate<Integer>() {

      public final boolean apply(final Integer i) {
        return neighborhood._col(i, available).isEmpty();
      }
    }, rng);
    available.remove(i);
    return i;
  }

  private final int nextChainElement(final int i, final BitSetSet available) {
    final Collection<Integer> upper = neighborhood._row(i, available);
    if (upper.isEmpty())
      return -1;
    else {
      final int j = Collections3.random(upper, rng);
      available.remove(j);
      return j;
    }
  }
}
