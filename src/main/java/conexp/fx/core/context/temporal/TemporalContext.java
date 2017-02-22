package conexp.fx.core.context.temporal;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.MatrixContext;

/**
 * @author Francesco Kriegel
 *
 * @param <G>
 * @param <M>
 * 
 *          a prototypical implementation of a temporal context
 * 
 *          the time dimension is assumed to be a finite prefix of the natural numbers, ie. the timepoints 0 to n for
 *          some natural number n
 * 
 *          Furthermore it is assumed that all timepoints have the same objects and attributes. If this is not the case,
 *          then it can be ensured by calling the normalize() methode. It simply adds all existing objects and
 *          attributes to all other timepoint contexts.
 */
public class TemporalContext<G, M> {

  private final List<MatrixContext<G, M>> cuts;

  public TemporalContext() {
    super();
    this.cuts = new LinkedList<MatrixContext<G, M>>();
  }

  public final MatrixContext<G, M> addTimepoint() {
    final MatrixContext<G, M> cxt = new MatrixContext<G, M>(false);
    cuts.add(cxt);
    return cxt;
  }

  public final MatrixContext<G, M> atTimepoint(final int t) {
    return cuts.get(t);
  }

  public final boolean hasTimepoint(final int t) {
    return t < cuts.size();
  }

  public final int lastTimepoint() {
    return cuts.size() - 1;
  }

  public final int getLength() {
    return cuts.size();
  }

  public final boolean normalize() {
    boolean changed = false;
    final Set<G> objects = new HashSet<G>();
    final Set<M> attributes = new HashSet<M>();
    for (MatrixContext<G, M> cxt : cuts) {
      objects.addAll(cxt.rowHeads());
      attributes.addAll(cxt.colHeads());
    }
    for (MatrixContext<G, M> cxt : cuts) {
      changed |= cxt.rowHeads().addAll(objects);
      changed |= cxt.colHeads().addAll(attributes);
    }
    return changed;
  }

  public final boolean contains(final G g, final M m, final Integer t) {
    return cuts.get(t).contains(g, m);
  }

  public final boolean contains(final G g, final LTL<M> m, final Integer t) {
    switch (m.getType()) {
    // checks whether g has m at timepoint t
    case NOW:
      return hasTimepoint(t) && atTimepoint(t).contains(g, m.getM());
      // checks whether g has m at next timepoint t+1
      // if the timepoint t exists but not t+1 then the method returns false;
    case NEXTW:
      return hasTimepoint(t + 1) && atTimepoint(t + 1).contains(g, m.getM());
      // checks whether g has m at next timepoint t+1
      // if the timepoint t exists but not t+1 then the method returns true;
    case NEXTS:
      return (hasTimepoint(t + 1) && atTimepoint(t + 1).contains(g, m.getM())) || (t == lastTimepoint());
      // checks whether g has m at some timepoint s after t
    case SOMETIMES:
      break;
    // checks whether g has m at all timepoints s after t
    case ALWAYS:
      // checks whether g has m at all timepoints s after t, until it has n
      // it may also happen that g always has m at all timepoints s after t
    case UNTILW:
      break;
    case UNTILS:
      break;
    default:
      break;
    }
    return false;
  }

  /**
   * @param timepoints
   * @param ltlAttributes
   * @return a formal context
   * 
   *         The method may throw an Exception, if the temporal context is not normalized. Thus the normalize() method
   *         should be called before.
   */
  public final MatrixContext<Pair<G, Integer>, LTL<M>> temporalScaling(
      final Set<Integer> timepoints,
      final Set<LTL<M>> ltlAttributes) {
    final MatrixContext<Pair<G, Integer>, LTL<M>> cxt = new MatrixContext<Pair<G, Integer>, LTL<M>>(false);
    cxt.colHeads().addAll(ltlAttributes);
    for (G g : cuts.get(0).rowHeads())
      if (timepoints == null)
        for (int t = 0; t <= lastTimepoint(); t++) {
          final Pair<G, Integer> p = new Pair<G, Integer>(g, t);
          cxt.rowHeads().add(p);
          for (LTL<M> m : ltlAttributes)
            if (this.contains(g, m, t))
              cxt.addFastSilent(p, m);
        }
      else
        for (int t : timepoints) {
          final Pair<G, Integer> p = new Pair<G, Integer>(g, t);
          cxt.rowHeads().add(p);
          for (LTL<M> m : ltlAttributes)
            if (this.contains(g, m, t))
              cxt.addFastSilent(p, m);
        }
    return cxt;
  }

  public final MatrixContext<Pair<G, Integer>, LTL<M>> temporalScaling(final Set<LTL<M>> ltlAttributes) {
    return temporalScaling(null, ltlAttributes);
  }

  public final MatrixContext<Pair<G, Integer>, LTL<M>> temporalScaling() {
    return temporalScaling(null, getAllLTLAttributes());
  }

  public Set<LTL<M>> getAllLTLAttributes() {
    return getLTLAttributes(LTL.Type.values());
  }

  public Set<LTL<M>> getLTLAttributes(final LTL.Type... types) {
    final Set<LTL<M>> ltlAttributes = new HashSet<LTL<M>>();
    for (LTL.Type type : types)
      switch (type) {
      case NOW:
      case NEXTW:
      case NEXTS:
      case SOMETIMES:
      case ALWAYS:
        for (M m : cuts.get(0).colHeads())
          ltlAttributes.add(new LTL<M>(type, m));
        break;
      case UNTILW:
      case UNTILS:
        for (M m : cuts.get(0).colHeads())
          for (M n : cuts.get(0).colHeads())
            if (!m.equals(n))
              ltlAttributes.add(new LTL<M>(type, m, n));
        break;
      }
    return ltlAttributes;
  }

}
