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

import java.util.Collection;
import java.util.Set;

import conexp.fx.core.collections.relation.AbstractRelation;
import conexp.fx.core.collections.relation.Relation;
import conexp.fx.core.collections.setlist.SetList;

public abstract class AbstractContext<G, M> extends AbstractRelation<G, M> implements Context<G, M> {

  protected AbstractContext(final SetList<G> objects, final SetList<M> attributes, final boolean homogen) {
    super(objects, attributes, homogen);
  }

  public Set<G> extent(final Collection<?> objects) {
    return colAnd(rowAnd(objects));
  }

  public Set<M> intent(final Collection<?> attributes) {
    return rowAnd(colAnd(attributes));
  }

  public Set<G> extent(final Object... objects) {
    if (objects.length == 1)
      return colAnd(row(objects[0]));
    return colAnd(rowAnd(objects));
  }

  public Set<M> intent(final Object... attributes) {
    if (attributes.length == 1)
      return rowAnd(col(attributes[0]));
    return rowAnd(colAnd(attributes));
  }

  public final Relation<G, G> objectQuasiOrder() {
    return new AbstractRelation<G, G>(rowHeads(), rowHeads(), true) {

      public final boolean contains(Object object1, Object object2) {
        return AbstractContext.this.row(object1).containsAll(AbstractContext.this.row(object2));
      }
    };
  }

  public final Relation<M, M> attributeQuasiOrder() {
    return new AbstractRelation<M, M>(colHeads(), colHeads(), true) {

      public final boolean contains(Object attribute1, Object attribute2) {
        return AbstractContext.this.col(attribute1).containsAll(AbstractContext.this.col(attribute2));
      }
    };
  }

  public MatrixContext<G, M> clone() {
    final MatrixContext<G, M> clone = new MatrixContext<G, M>(rowHeads().clone(), colHeads().clone(), false);
    clone.rowHeads().parallelStream().forEach(object -> clone.row(object).addAll(row(object)));
//    for (G object : clone.rowHeads())
//      for (M attribute : clone.colHeads())
//        if (contains(object, attribute))
//          clone.addFast(object, attribute);
    return clone;
  }

  @Override
  public final AbstractContext<G, M> getSelection() {
    return this;
  }

}
