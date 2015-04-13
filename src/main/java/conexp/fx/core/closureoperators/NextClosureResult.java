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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import conexp.fx.core.context.Concept;

public class NextClosureResult<G, M> extends ImplicationClosureOperator<G, M> {

  private final Set<Concept<G, M>> formalConcepts;

  public NextClosureResult() {
    super(true);
    this.formalConcepts = Collections.newSetFromMap(new ConcurrentHashMap<Concept<G, M>, Boolean>());
  }

  public final Set<Concept<G, M>> getFormalConcepts() {
    return this.formalConcepts;
  }

}
