package conexp.fx.core.context.negation;

/*
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
