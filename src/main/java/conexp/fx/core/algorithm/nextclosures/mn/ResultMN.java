package conexp.fx.core.algorithm.nextclosures.mn;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2023 Francesco Kriegel
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
