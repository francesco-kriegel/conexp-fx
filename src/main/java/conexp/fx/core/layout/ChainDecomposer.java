package conexp.fx.core.layout;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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


import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.ujmp.core.collections.BitSetSet;
import org.ujmp.core.util.RandomSimple;

import com.google.common.base.Predicate;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.relation.MatrixRelation;

public final class ChainDecomposer<E>
{
  private final MatrixRelation<E, E> neighborhood;
  private final int                  num;
  private final Random               rng = new RandomSimple();

  public ChainDecomposer(final MatrixRelation<E, E> neighborhood)
  {
    this.neighborhood = neighborhood;
    this.num = neighborhood.rowHeads().size();
  }

  public final Set<Set<E>> randomChainDecomposition()
  {
    final BitSetSet available = new BitSetSet();
    available.set(0, num);
    final Set<Set<E>> chains = new HashSet<Set<E>>();
    while (!available.isEmpty())
      chains.add(nextChain(available));
    return chains;
  }

  private final Set<E> nextChain(final BitSetSet available)
  {
    final Set<E> chain = new HashSet<E>();
    for (int i = nextMinimalElement(available); i != -1; i = nextChainElement(i, available))
      chain.add(neighborhood.rowHeads().get(i));
    return chain;
  }

  private final int nextMinimalElement(final BitSetSet available)
  {
    final int i = Collections3.random(available, new Predicate<Integer>()
    {
      public final boolean apply(final Integer i)
      {
        return neighborhood._col(i, available).isEmpty();
      }
    }, rng);
    available.remove(i);
    return i;
  }

  private final int nextChainElement(final int i, final BitSetSet available)
  {
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
