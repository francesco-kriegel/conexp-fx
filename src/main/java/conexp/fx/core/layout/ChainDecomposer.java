package conexp.fx.core.layout;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.ujmp.core.util.RandomSimple;

import com.google.common.base.Predicate;

import conexp.fx.core.collections.BitSetFX;
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
    final BitSetFX available = new BitSetFX();
    available.set(0, num);
    final Set<Set<E>> chains = new HashSet<Set<E>>();
    while (!available.isEmpty())
      chains.add(nextChain(available));
    return chains;
  }

  private final Set<E> nextChain(final BitSetFX available) {
    final Set<E> chain = new HashSet<E>();
    for (int i = nextMinimalElement(available); i != -1; i = nextChainElement(i, available))
      chain.add(neighborhood.rowHeads().get(i));
    return chain;
  }

  private final int nextMinimalElement(final BitSetFX available) {
    final int i = Collections3.random(available, new Predicate<Integer>() {

      public final boolean apply(final Integer i) {
        return neighborhood._col(i, available).isEmpty();
      }
    }, rng);
    available.remove(i);
    return i;
  }

  private final int nextChainElement(final int i, final BitSetFX available) {
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
