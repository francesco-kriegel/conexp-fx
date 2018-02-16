package conexp.fx.core.algorithm.exploration;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import conexp.fx.core.context.Implication;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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
  public Set<CounterExample<G, M>> getCounterExamples(Implication<G, M> implication) throws InterruptedException;

  public default Future<Set<CounterExample<G, M>>> requestCounterExamples(final Implication<G, M> implication) {
    return new Future<Set<CounterExample<G, M>>>() {

      private final CountDownLatch                             cdl         = new CountDownLatch(1);
      private final AtomicReference<Set<CounterExample<G, M>>> ref         =
          new AtomicReference<>(Collections.emptySet());
      private boolean                                          isCancelled = false;

      {
        new Thread(() -> {
          try {
            ref.set(getCounterExamples(implication));
            cdl.countDown();
          } catch (InterruptedException __) {
            cancel(true);
          }
        }).start();
      }

      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        if (isCancelled)
          return false;
        isCancelled = true;
        cdl.countDown();
        return true;
      }

      @Override
      public boolean isCancelled() {
        return isCancelled;
      }

      @Override
      public boolean isDone() {
        return cdl.getCount() == 0;
      }

      @Override
      public Set<CounterExample<G, M>> get() throws InterruptedException, ExecutionException {
        cdl.await();
        return ref.get();
      }

      @Override
      public Set<CounterExample<G, M>> get(long timeout, TimeUnit unit)
          throws InterruptedException, ExecutionException, TimeoutException {
        if (cdl.await(timeout, unit))
          return ref.get();
        throw new TimeoutException();
      }
    };
  }

}
