package conexp.fx.core.context;

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

import java.util.HashSet;
import java.util.Set;

import conexp.fx.core.collections.Pair;
import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;

public class SparseContext<G, M> extends AbstractContext<G, M> {

  private final Set<Pair<G, M>> incidences = new HashSet<Pair<G, M>>();

  public SparseContext(SetList<G> objects, SetList<M> attributes, boolean homogen) {
    super(objects, attributes, homogen);
  }

  @Override
  public boolean contains(Object o1, Object o2) {
    return incidences.contains(new Pair<Object, Object>(o1, o2));
  }

  @Override
  public boolean add(G row, M col) {
    return incidences.add(Pair.of(row, col));
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean addFast(Object o1, Object o2) {
    return incidences.add(Pair.of((G) o1, (M) o2));
  }

  public final SparseContext<Set<G>, M> cleanDomain() {
    final SetList<Set<G>> eqClasses = new HashSetArrayList<Set<G>>(this.objectQuasiOrder().equivalenceClasses());
    final SparseContext<Set<G>, M> cleanedContext = new SparseContext<Set<G>, M>(eqClasses, this.colHeads(), false);
    for (Set<G> g : cleanedContext.rowHeads())
      for (M m : cleanedContext.colHeads())
        if (this.contains(g.iterator().next(), m))
          cleanedContext.add(g, m);
    return cleanedContext;
  }

}
