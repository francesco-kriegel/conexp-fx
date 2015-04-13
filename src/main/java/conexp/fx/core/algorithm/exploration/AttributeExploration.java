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
import java.util.Iterator;
import java.util.Set;

import conexp.fx.core.algorithm.nextclosure.NextImplication;
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

  protected final MatrixContext<G, M>         context;
  protected final Expert<G, M>                expert;
  protected final Iterator<Implication<G, M>> iterator;
  protected final Set<Implication<G, M>>      implications = new HashSet<Implication<G, M>>();

  public AttributeExploration(final MatrixContext<G, M> context, final Expert<G, M> expert) {
    super();
    this.context = context;
    this.expert = expert;
    this.iterator = new NextImplication<G, M>(context).iterator();
    showNextImplication();
  }

  protected final void showNextImplication() {
    if (!iterator.hasNext())
      return;
    final Implication<G, M> next = iterator.next();
    if (next == null)
      return;
    try {
      final CounterExample<G, M> counterExample = expert.askForCounterexample(next);
      if (counterExample == null)
        implications.add(next);
      else
        addCounterExampleToContext(counterExample);
    } catch (InterruptedException e) {
      return;
    }
    showNextImplication();
  }

  protected final void addCounterExampleToContext(final CounterExample<G, M> counterExample) {
    context.rowHeads().add(
        counterExample.getObject());
    for (M m : counterExample.getAttributes())
      context.add(
          counterExample.getObject(),
          m);
  }

  public final Set<Implication<G, M>> getImplications() {
    return implications;
  }

  /**
   * This method may only be called within an instance of Concept Explorer FX.
   * 
   * @param context
   * @return
   */
  public static final AttributeExploration<String, String> withHumanExpert(final MatrixContext<String, String> context) {
    return new AttributeExploration<String, String>(context, new HumanExpert(context));
  }

}
