package conexp.fx.core.context.negation;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collection;

import org.ujmp.core.calculation.Calculation.Ret;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.context.negation.Literal.Type;

public class NegationScaling {

  public static final <G, M> MatrixContext<G, Literal<M>> negationScaling(
      final MatrixContext<G, M> cxt,
      final Collection<M> attributes) {
    final MatrixContext<G, Literal<M>> ncxt = new MatrixContext<G, Literal<M>>(false);
    final ArrayList<Literal<M>> negations = new ArrayList<Literal<M>>();
    ncxt.rowHeads().addAll(cxt.rowHeads());
    for (M m : cxt.colHeads()) {
      ncxt.colHeads().add(new Literal<M>(m));
      if (attributes.contains(m))
        negations.add(new Literal<M>(Type.NEGATIVE, m));
    }
    ncxt.colHeads().addAll(negations);
    ncxt.setMatrix(cxt
        .matrix()
        .clone()
        .appendHorizontally(Ret.NEW,
            cxt.matrix().selectColumns(Ret.NEW, cxt.colHeads().indicesOf(attributes, false)).not(Ret.NEW))
        .toBooleanMatrix());
    return ncxt;
  }

}
