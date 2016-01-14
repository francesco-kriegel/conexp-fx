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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import conexp.fx.core.context.Context;
import conexp.fx.core.implication.Implication;

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

  public void insertIn(final Context<G, M> cxt) {
    cxt.rowHeads().add(object);
    for (M m : attributes)
      cxt.add(object, m);
  }

  public void addTo(final Implication<G, M> implication) {
    implication.getSupport().add(object);
    implication.getConclusion().retainAll(attributes);
  }

  @Override
  public String toString() {
    return "counter-example (" + object + "): " + attributes;
  }

}
