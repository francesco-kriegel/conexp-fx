package conexp.fx.core.algorithm.nextclosures.mn;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.context.MatrixContext;

public final class MatrixContextMN<G, M> {

  private final MatrixContext<G, M> cxt;
  private final Set<M>              premises;
  private final Set<M>              conclusions;
  private final MatrixContext<G, M> cxtM;
  private final MatrixContext<G, M> cxtN;

  public MatrixContextMN(final MatrixContext<G, M> cxt, final Set<M> premises, final Set<M> conclusions) {
    if (!Sets.intersection(
        premises,
        conclusions).isEmpty())
      throw new RuntimeException();
    this.cxt = cxt;
    this.premises = premises;
    this.conclusions = conclusions;
    this.cxtM = cxt.subRelation(
        cxt.rowHeads(),
        premises).clone();
    this.cxtN = cxt.subRelation(
        cxt.rowHeads(),
        conclusions).clone();
  }

  public final Set<M> closureMN(final Set<M> set) {
    if (!premises.containsAll(set))
      throw new RuntimeException();
    return new HashSet<M>(cxtN.rowAnd(new HashSet<G>(cxtM.colAnd(set))));
  }

  public final Set<M> intentM(final Set<M> set) {
    if (!premises.containsAll(set))
      throw new RuntimeException();
    return new HashSet<M>(cxtM.intent(set));
  }

}
