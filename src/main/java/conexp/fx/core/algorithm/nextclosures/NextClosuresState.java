package conexp.fx.core.algorithm.nextclosures;

/*-
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

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import conexp.fx.core.collections.BitSetFX;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.Implication;

public final class NextClosuresState<G, M, C extends Set<M>> {

  public static final <G, M> NextClosuresState<G, M, Set<M>> withHashSets(final Set<M> domain) {
    return new NextClosuresState<>(domain, HashSet<M>::new);
  }

  public static final NextClosuresState<Integer, Integer, BitSetFX> withBitSets(final int max) {
    return new NextClosuresState<>(Collections3.integers(max), BitSetFX::new);
  }

  public final Set<Concept<G, M>>     concepts     = Collections3.newConcurrentHashSet();
  public final Set<Implication<G, M>> implications = Collections3.newConcurrentHashSet();
  public final Map<C, Integer>        candidates   = new ConcurrentHashMap<>();
  private final Set<C>                processed    = Collections3.newConcurrentHashSet();
  public int                          cardinality  = 0;

  private final Set<M>                domain;
  private final Function<Set<M>, C>   supplier;

  private NextClosuresState(final Set<M> domain, final Function<Set<M>, C> supplier) {
    this.domain = domain;
    this.supplier = supplier;
    candidates.put(supplier.apply(Collections.emptySet()), 0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see conexp.fx.core.algorithm.nextclosures.INextClosuresState#getActualCandidateStream()
   */

  public final Set<C> getActualCandidates() {
//    return new HashSet<>(Sets.filter(candidates.keySet(), c -> c.size() == cardinality));
    return candidates.keySet().parallelStream().filter(c -> c.size() == cardinality).collect(Collectors.toSet());
  }

  /*
   * (non-Javadoc)
   * 
   * @see conexp.fx.core.algorithm.nextclosures.INextClosuresState#getFirstPremiseSize(C)
   */

  public final int getFirstPremiseSize(final C candidate) {
    return candidates.get(candidate);
  }

  /*
   * (non-Javadoc)
   * 
   * @see conexp.fx.core.algorithm.nextclosures.INextClosuresState#isNewIntent(C)
   */

  public final boolean isNewIntent(final C s) {
    try {
      return processed.add(s);
    } catch (ConcurrentModificationException e) {
      return isNewIntent(s);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see conexp.fx.core.algorithm.nextclosures.INextClosuresState#addNewCandidates(C)
   */

  public final void addNewCandidates(final C intent) {
    for (M m : Sets.difference(domain, intent)) {
      final C candidateM = supplier.apply(intent);
      candidateM.add(m);
      candidates.put(candidateM, 0);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see conexp.fx.core.algorithm.nextclosures.INextClosuresState#addCandidate(C)
   */

  public final void addCandidate(final C candidate) {
    candidates.put(candidate, cardinality);

  }

  /*
   * (non-Javadoc)
   * 
   * @see conexp.fx.core.algorithm.nextclosures.INextClosuresState#getResultAndDispose()
   */

  public final Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> getResultAndDispose() {
    candidates.clear();
    processed.clear();
    return Pair.of(concepts, implications);
  }

  public int getActualCardinality() {
    return cardinality;
  }

  public void increaseCardinality() {
    cardinality++;
  }

}
