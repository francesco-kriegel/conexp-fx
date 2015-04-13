package conexp.fx.core.collections;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.util.AbstractSet;

import com.google.common.collect.Iterators;

public abstract class IterableSet<E> extends AbstractSet<E> {

  @Override
  public int size() {
    return Iterators.size(iterator());
  }
}
