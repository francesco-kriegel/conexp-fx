package conexp.fx.core.algorithm.exploration;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
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

import com.google.common.collect.Lists;

import conexp.fx.core.collections.setlist.SetList.LecticOrder;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.math.SetClosureOperator;
import conexp.fx.gui.exploration.HumanExpert;

/**
 * The standard attribute exploration algorithm. This implementation is wrapped around the NextImplication class, that
 * computes the formal implications in the canonical base of a formal context by means of the NextClosure algorithm.
 *
 * @param <G>
 *          the type of objects.
 * @param <M>
 *          the type of attributes.
 */
public final class AttributeExploration<G, M> {

  /**
   * This method may only be called within an instance of Concept Explorer FX.
   * 
   * @param context
   * @return
   */
  public static final AttributeExploration<String, String>
      withHumanExpert(final MatrixContext<String, String> context) {
    return new AttributeExploration<String, String>(context, new HumanExpert(context));
  }

  public static final <G, M> Set<Implication<G, M>> getCanonicalBase(final MatrixContext<G, M> context) {
    final AttributeExploration<G, M> attributeExploration = new AttributeExploration<G, M>(context, __ -> null);
    try {
      attributeExploration.start();
    } catch (InterruptedException e) {}
    return attributeExploration.getImplicationalBase();
  }

  protected final MatrixContext<G, M>    context;
  protected final Expert<G, M>           expert;
  protected Set<M>                       pseudoClosure = new HashSet<M>();
  protected final Set<Implication<G, M>> implications  = new HashSet<Implication<G, M>>();
  private final SetClosureOperator<M>       clop          = SetClosureOperator.fromImplications(implications, true, true);
  private final LecticOrder<M>           lecticOrder;

  /**
   * Constructs a new attribute exploration process. It has to be started by a call to the method run.
   * 
   * @param context
   * @param expert
   */
  public AttributeExploration(final MatrixContext<G, M> context, final Expert<G, M> expert) {
    super();
    this.context = context;
    this.expert = expert;
    this.lecticOrder = context.colHeads().getLecticOrder();
  }

  /**
   * This method starts the attribute exploration. If the expert cancels the exploration process, then an
   * InterruptedException is thrown.
   * 
   * @throws InterruptedException
   *           if expert cancels the attribute exploration.
   */
  public final void start() throws InterruptedException {
    while (pseudoClosure.size() < context.colHeads().size()) {
//      final Set<M> closure = context.intent(pseudoClosure);
      final Set<M> closure = context.rowAnd(context.colAnd(pseudoClosure));
      if (closure.size() == pseudoClosure.size()) {
        nextPseudoClosure();
      } else {
        closure.removeAll(pseudoClosure);
        final Implication<G, M> implication = new Implication<G, M>(pseudoClosure, closure);
        final Set<CounterExample<G, M>> counterExample = expert.getCounterExamples(implication);
        if (counterExample.isEmpty()) {
          implications.add(implication);
          nextPseudoClosure();
        } else {
          counterExample.forEach(cex -> cex.insertIn(context));
        }
      }
    }
  }

  private final void nextPseudoClosure() {
    for (M m : Lists.reverse(context.colHeads())) {
      final Set<M> s = clop.closure(lecticOrder.oplus(pseudoClosure, m));
      if (lecticOrder.isSmaller(pseudoClosure, s, m)) {
        pseudoClosure = s;
        return;
      }
    }
    throw new RuntimeException();
  }

  /**
   * @return the implicational base, that was constructed during the attribute exploration.
   */
  public final Set<Implication<G, M>> getImplicationalBase() {
    return implications;
  }

  /**
   * @return the formal context, that is an extension of the input context and contains all counterexamples that have
   *         been given by the expert during the attribute exploration.
   */
  public final MatrixContext<G, M> getFormalContext() {
    return context;
  }

}
