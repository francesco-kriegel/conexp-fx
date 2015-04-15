package conexp.fx.core.algorithm.exploration;

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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Lists;

import conexp.fx.core.closureoperators.ImplicationClosureOperator;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.Implication;
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
  public static final AttributeExploration<String, String> withHumanExpert(final MatrixContext<String, String> context) {
    return new AttributeExploration<String, String>(context, new HumanExpert(context));
  }

  protected final MatrixContext<G, M>    context;
  protected final Expert<G, M>           expert;
  protected Set<M>                       pseudoClosure = new HashSet<M>();
  protected final Set<Implication<G, M>> implications  = new HashSet<Implication<G, M>>();

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
      final Set<M> closure = context.intent(pseudoClosure);
      if (closure.size() == pseudoClosure.size()) {
        nextPseudoClosure();
      } else {
        final Implication<G, M> implication = new Implication<G, M>(pseudoClosure, closure);
        final CounterExample<G, M> counterExample = expert.askForCounterexample(implication);
        if (counterExample == null) {
          addImplication(implication);
          nextPseudoClosure();
        } else {
          addCounterExample(counterExample);
        }
      }
    }
    System.out.println(context.rowHeads());
  }

  private final void addImplication(final Implication<G, M> implication) {
    implications.add(implication);
  }

  private final void addCounterExample(final CounterExample<G, M> counterExample) {
    context.rowHeads().add(
        counterExample.getObject());
    for (M m : counterExample.getAttributes())
      context.add(
          counterExample.getObject(),
          m);
  }

  private final void nextPseudoClosure() {
    pseudoClosure = nextClosure(pseudoClosure);
  }

  private final Set<M> nextClosure(final Set<M> set) {
    final ImplicationClosureOperator<G, M> clop = new ImplicationClosureOperator<>(implications, true);
    for (M m : Lists.reverse(context.colHeads())) {
      final Set<M> s = clop.closure(LexicalOrder.oplus(
          context.colHeads(),
          set,
          m));
      if (LexicalOrder.isSmaller(
          context.colHeads(),
          set,
          s,
          m))
        return s;
    }
    throw new RuntimeException();
  }

//  private final Set<M> nextClosure(final Set<M> set) {
//    final ImplicationClosureOperator<G, M> clop = new ImplicationClosureOperator<>(implications, true);
//    final Function<M, Function<Set<M>, Set<M>>> f = m -> (s -> clop.closure(_APlusG(
//        s,
//        m)));
//
//    final Optional<M> max = context.colHeads().parallelStream().filter(
//        m -> isLexicSmaller(
//            set,
//            f.apply(
//                m).apply(
//                set),
//            m)).findFirst();
//    if (max.isPresent())
//      return clop.closure(_APlusG(
//          set,
//          max.get()));
//    throw new RuntimeException();
//  }

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
