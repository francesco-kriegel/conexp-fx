package conexp.fx.core.context;

import java.util.Collection;
import java.util.Collections;

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import conexp.fx.core.math.ClosureOperator;
import conexp.fx.core.util.UnicodeSymbols;
import de.tudresden.inf.tcs.fcaapi.FCAImplication;

public class Implication<G, M> extends de.tudresden.inf.tcs.fcalib.Implication<M> {

  public static final <G, M> boolean equivalent(final Set<Implication<G, M>> x, final Set<Implication<G, M>> y) {
    return entails(x, y) && entails(y, x);
  }

  public static final <G, M> boolean entails(final Set<Implication<G, M>> x, final Set<Implication<G, M>> y) {
    return y.parallelStream().allMatch(z -> entails(x, z));
  }

  public static final <G, M> Set<Implication<G, M>>
      diff(final Set<Implication<G, M>> x, final Set<Implication<G, M>> y) {
    return y.parallelStream().filter(z -> !entails(x, z)).collect(Collectors.toSet());
  }

  public static final <G, M> boolean entails(final Set<Implication<G, M>> x, final Implication<G, M> y) {
    return ClosureOperator.fromImplications(x, false, false).closure(y.getPremise()).containsAll(y.getConclusion());
  }

  private final Set<G> support;
  private final double confidence;

  public Implication() {
    this(new HashSet<M>(), new HashSet<M>(), new HashSet<G>());
  }

  public Implication(final Set<M> premise, final Set<M> conclusion) {
    this(premise, conclusion, new HashSet<G>());
  }

  public Implication(final Set<M> premise, final Set<M> conclusion, final Set<G> support) {
    this(premise, conclusion, support, 1d);
  }

  public Implication(final Set<M> premise, final Set<M> conclusion, final Set<G> support, final double confidence) {
    super(premise, conclusion);
    this.support = support;
    if (confidence < 0d || confidence > 1d)
      throw new IllegalArgumentException("Confidence must be in range [0,1]");
    this.confidence = confidence;
  }

  public Implication(final Collection<M> premise, final Collection<M> conclusion) {
    this(new HashSet<M>(premise), new HashSet<M>(conclusion));
  }

  public Implication(final Collection<M> premise, final Collection<M> conclusion, final Collection<G> support) {
    this(
        (Set<M>) (premise instanceof Set ? premise : new HashSet<M>(premise)),
        (Set<M>) (conclusion instanceof Set ? conclusion : new HashSet<M>(conclusion)),
        (Set<G>) (support instanceof Set ? support : new HashSet<G>(support)),
        1d);
  }

  public Implication(final M premise, final Set<M> conclusion) {
    this(Collections.singleton(premise), conclusion);
  }

  public Implication(final Set<M> premise, final M conclusion) {
    this(premise, Collections.singleton(conclusion));
  }

  public Implication(final M premise, final M conclusion) {
    this(Collections.singleton(premise), Collections.singleton(conclusion));
  }

  public final Set<G> getSupport() {
    return support;
  }

  public final double getConfidence() {
    return confidence;
  }

  public boolean isTrivial() {
    return getPremise().containsAll(getConclusion());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof Implication)) {
      if (obj instanceof FCAImplication)
        return super.equals((FCAImplication<?>) obj);
      return false;
    }
    final Implication<?, ?> other = (Implication<?, ?>) obj;
    return this.getPremise().equals(other.getPremise()) && this.getConclusion().equals(other.getConclusion());
//        && this.getSupport().equals(other.getSupport()) && this.getConfidence() == other.getConfidence();
  }

  @Override
  public int hashCode() {
    return getPremise().hashCode() + getConclusion().hashCode();
//    return 2 * getPremise().hashCode() + 3 * getConclusion().hashCode() + 5 * getSupport().hashCode()
//        + (int) (8191d * confidence);
  }

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    final Iterator<M> pit = getPremise().iterator();
    if (pit.hasNext())
      s.append(pit.next());
    pit.forEachRemaining(m -> s.append(" " + UnicodeSymbols.WEDGE + " " + m));
    s.append(" " + UnicodeSymbols.TO + " ");
    final Iterator<M> cit = Sets.difference(getConclusion(), getPremise()).iterator();
    if (cit.hasNext())
      s.append(cit.next());
    cit.forEachRemaining(m -> s.append(" " + UnicodeSymbols.WEDGE + " " + m));
    if (confidence < 1d)
      s.append(" (" + ((int) (100d * confidence)) + "%)");
    return s.toString();
  }

}
