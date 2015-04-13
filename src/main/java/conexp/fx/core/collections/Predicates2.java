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


import java.util.Collection;

import com.google.common.base.Predicate;

public class Predicates2
{

  public static final <E> Predicate<Collection<E>> isEmpty()
  {
    return new Predicate<Collection<E>>()
    {

      @Override
      public final boolean apply(final Collection<E> c)
      {
        return c.isEmpty();
      }
    };
  }
}
