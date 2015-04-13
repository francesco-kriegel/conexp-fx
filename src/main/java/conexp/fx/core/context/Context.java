/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.context;

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
import java.util.Set;

import conexp.fx.core.collections.relation.Relation;

public interface Context<G, M> extends Relation<G, M> {

  public Set<G> extent(Collection<?> objects);

  public Set<M> intent(Collection<?> attributes);

  public Set<G> extent(Object... objects);

  public Set<M> intent(Object... attributes);

  public Relation<G, G> objectQuasiOrder();

  public Relation<M, M> attributeQuasiOrder();

  public MatrixContext<G, M> clone();
}
