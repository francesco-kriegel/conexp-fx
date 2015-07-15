package conexp.fx.core.algorithm.nextclosures.mn;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class ResultMN<G, M> {

  final Map<Set<M>, Set<M>> implications;

  public ResultMN() {
    this.implications = new HashMap<Set<M>, Set<M>>();
  }

  public final Set<M> closure(final Set<M> set) {
    final Set<M> cl = new HashSet<M>();
    for (Entry<Set<M>, Set<M>> e : implications.entrySet())
      if (set.containsAll(e.getKey()))
        cl.addAll(e.getValue());
    return cl;
  }

  public final Map<Set<M>, Set<M>> getImplications() {
    return implications;
  }

}
