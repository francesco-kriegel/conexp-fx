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

import java.util.Set;

public abstract class AClosureOperator<T> implements ClosureOperator<T> {

  public AClosureOperator() {
    super();
  }

  @Override
  public boolean close(final Set<T> set) {
    return !set.addAll(closure(set));
  }

  @Override
  public boolean isClosed(final Set<T> set) {
    return set.containsAll(closure(set));
  }

}
