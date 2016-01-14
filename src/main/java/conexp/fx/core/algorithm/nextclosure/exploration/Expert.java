package conexp.fx.core.algorithm.nextclosure.exploration;

import java.util.Set;
import java.util.concurrent.Future;

import conexp.fx.core.implication.Implication;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

@FunctionalInterface
public interface Expert<G, M> {

  /**
   * 
   * Returns an empty set if the provided implication is valid, otherwise returns a set containing at least one
   * counterexample.
   * 
   * @param impl
   * @return
   * @throws InterruptedException
   */
  public Set<CounterExample<G, M>> askForCounterExample(final Implication<G, M> impl) throws InterruptedException;

}
