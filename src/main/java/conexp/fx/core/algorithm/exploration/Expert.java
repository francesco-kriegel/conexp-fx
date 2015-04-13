package conexp.fx.core.algorithm.exploration;

import conexp.fx.core.implication.Implication;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

@FunctionalInterface
public interface Expert<G, M> {

  public CounterExample<G, M> askForCounterexample(final Implication<G, M> impl) throws InterruptedException;

}
