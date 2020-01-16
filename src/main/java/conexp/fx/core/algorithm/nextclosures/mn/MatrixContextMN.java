package conexp.fx.core.algorithm.nextclosures.mn;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2020 Francesco Kriegel
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
