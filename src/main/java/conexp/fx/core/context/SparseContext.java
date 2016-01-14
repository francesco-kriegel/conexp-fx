package conexp.fx.core.context;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
