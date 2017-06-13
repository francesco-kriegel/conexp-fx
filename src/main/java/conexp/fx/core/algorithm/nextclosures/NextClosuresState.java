package conexp.fx.core.algorithm.nextclosures;

/*-
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
