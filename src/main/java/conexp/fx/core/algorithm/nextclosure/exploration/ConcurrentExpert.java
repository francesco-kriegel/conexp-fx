package conexp.fx.core.algorithm.nextclosure.exploration;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import conexp.fx.core.implication.Implication;

@FunctionalInterface
public interface ConcurrentExpert<G, M> {

  public Future<Set<CounterExample<G, M>>> askForCounterExample(Implication<G, M> implication);

  public static <G, M> ConcurrentExpert<G, M> from(final Expert<G, M> expert) {
    return implication -> new Future<Set<CounterExample<G, M>>>() {

      private final CountDownLatch                             cdl         = new CountDownLatch(1);
      private final AtomicReference<Set<CounterExample<G, M>>> ref         =
          new AtomicReference<>(Collections.emptySet());
      private boolean                                          isCancelled = false;

      {
        new Thread(() -> {
          try {
            ref.set(expert.askForCounterExample(implication));
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
