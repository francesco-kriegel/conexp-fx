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
