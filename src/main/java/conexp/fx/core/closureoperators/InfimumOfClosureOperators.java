package conexp.fx.core.closureoperators;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class InfimumOfClosureOperators<M> implements ClosureOperator<M> {

  private final List<ClosureOperator<M>> closureOperators;

  @SafeVarargs
  public InfimumOfClosureOperators(final ClosureOperator<M>... closureOperators) {
    super();
    if (closureOperators.length < 1)
      throw new IllegalArgumentException("Unable to create infimum of less than 1 closure operator.");
    this.closureOperators = Arrays.asList(closureOperators);
  }

  public final List<ClosureOperator<M>> getOperands() {
    return closureOperators;
  }

  @Override
  public final boolean isClosed(final Set<M> set) {
    return set.containsAll(closure(set));
  }

  @Override
  public final boolean close(final Set<M> set) {
    final Set<M> closure = closure(set);
    final boolean closed = set.containsAll(closure);
    if (!closed)
      set.addAll(closure);
    return closed;
  }

  @Override
  public final Set<M> closure(final Set<M> set) {
    final Set<M> closure = new HashSet<M>();
    final Iterator<ClosureOperator<M>> iterator = closureOperators.iterator();
    if (iterator.hasNext())
      set.addAll(iterator.next().closure(
          set));
    while (iterator.hasNext())
      set.retainAll(iterator.next().closure(
          set));
    return closure;
  }

}
