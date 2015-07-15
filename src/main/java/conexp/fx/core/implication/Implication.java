package conexp.fx.core.implication;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.util.UnicodeSymbols;
import de.tudresden.inf.tcs.fcaapi.FCAImplication;

public class Implication<G, M> extends de.tudresden.inf.tcs.fcalib.Implication<M> {

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

  public final Set<G> getSupport() {
    return support;
  }

  public final double getConfidence() {
    return confidence;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null)
      return false;
    if (obj instanceof FCAImplication)
      return super.equals(
          (FCAImplication<?>) obj);
    if (!(obj instanceof Implication))
      return false;
    final Implication<?, ?> other = (Implication<?, ?>) obj;
    return this.getPremise().equals(
        other.getPremise())
        && this.getConclusion().equals(
            other.getConclusion())
        && this.getSupport().equals(
            other.getSupport())
        && this.getConfidence() == other.getConfidence();
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
      s.append(
          pit.next());
    pit.forEachRemaining(
        m -> s.append(
            " " + UnicodeSymbols.WEDGE + " " + m));
    s.append(
        " " + UnicodeSymbols.TO + " ");
    final Iterator<M> cit = Sets.difference(
        getConclusion(),
        getPremise()).iterator();
    if (cit.hasNext())
      s.append(
          cit.next());
    cit.forEachRemaining(
        m -> s.append(
            " " + UnicodeSymbols.WEDGE + " " + m));
    if (confidence < 1d)
      s.append(
          " (" + ((int) (100d * confidence)) + "%)");
    return s.toString();
  }

}
