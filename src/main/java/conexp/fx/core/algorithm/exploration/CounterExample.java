package conexp.fx.core.algorithm.exploration;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import conexp.fx.core.context.Context;
import conexp.fx.core.context.Implication;

public final class CounterExample<G, M> {

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

  public final G getObject() {
    return object;
  }

  public final Set<M> getAttributes() {
    return attributes;
  }

  public final void insertIn(final Context<G, M> cxt) {
    cxt.rowHeads().add(object);
    cxt.row(object).addAll(attributes);
//    for (M m : attributes)
//      cxt.add(object, m);
  }

  public final void addTo(final Implication<G, M> implication) {
    implication.getSupport().add(object);
    implication.getConclusion().retainAll(attributes);
  }

  @Override
  public final boolean equals(final Object obj) {
    if (!(obj instanceof CounterExample))
      return false;
    final CounterExample<?, ?> that = (CounterExample<?, ?>) obj;
    return this.object.equals(that.object) && this.attributes.equals(that.attributes);
  }

  @Override
  public final int hashCode() {
    return object.hashCode() + attributes.hashCode();
  }

  @Override
  public final String toString() {
    return "counter-example (" + object + "): " + attributes;
  }

}
