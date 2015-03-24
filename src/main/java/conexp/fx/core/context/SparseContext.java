package conexp.fx.core.context;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.HashSet;
import java.util.Set;

import conexp.fx.core.collections.pair.Pair;
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
