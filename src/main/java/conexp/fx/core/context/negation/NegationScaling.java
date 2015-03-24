package conexp.fx.core.context.negation;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
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
        .appendHorizontally(
            cxt.matrix().selectColumns(Ret.NEW, cxt.colHeads().indicesOf(attributes, false)).not(Ret.NEW))
        .toBooleanMatrix());
    return ncxt;
  }

}
