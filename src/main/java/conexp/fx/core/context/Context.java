/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.context;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.collections.relation.Relation;

public interface Context<G, M> extends Relation<G, M> {

  public Set<G> extent(Collection<?> objects);

  public Set<M> intent(Collection<?> attributes);

  public Set<G> extent(Object... objects);

  public Set<M> intent(Object... attributes);

  public Relation<G, G> objectQuasiOrder();

  public Relation<M, M> attributeQuasiOrder();

  public MatrixContext<G, M> clone();

  public Context<G, M> getSelection();

  public default boolean models(Implication<G, M> implication, boolean... checkSupport) {
    final Set<G> support = colAnd(implication.getPremise());
    final double confidence = support.isEmpty() ? 1d
        : ((double) colAnd(Sets.union(implication.getPremise(), implication.getConclusion())).size())
            / ((double) support.size());
    return implication.getConfidence() == confidence
        && (checkSupport.length == 0 || checkSupport[0] == false || implication.getSupport().equals(support));
  }

  public default boolean models(Collection<Implication<G, M>> implications, boolean... checkSupport) {
    return implications.parallelStream().allMatch(implication -> this.models(implication, checkSupport));
  }

  public default boolean has(Concept<G, M> concept) {
    return concept.extent().equals(colAnd(concept.intent())) && concept.intent().equals(rowAnd(concept.extent()));
  }

  public default MatrixContext<G, M> toMatrixContext() {
    return this instanceof MatrixContext ? (MatrixContext<G, M>) this : clone();
  }
}
