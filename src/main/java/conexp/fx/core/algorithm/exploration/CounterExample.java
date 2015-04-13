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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CounterExample<G, M> {

  private final G      object;
  private final Set<M> attributes;

  @SafeVarargs
  public CounterExample(final G object, final M... attributes) {
    this(object, Arrays.asList(attributes));
  }

  public CounterExample(final G object, final Collection<M> attributes) {
    super();
    this.object = object;
    this.attributes = new HashSet<M>(attributes);
  }

  public G getObject() {
    return object;
  }

  public Set<M> getAttributes() {
    return attributes;
  }
}
