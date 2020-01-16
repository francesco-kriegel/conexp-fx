package conexp.fx.core.context;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2020 Francesco Kriegel
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

import java.util.Collection;
import java.util.Set;
import java.util.function.BiPredicate;

import conexp.fx.core.collections.relation.AbstractRelation;
import conexp.fx.core.collections.relation.Relation;
import conexp.fx.core.collections.setlist.SetList;

public abstract class AbstractContext<G, M> extends AbstractRelation<G, M> implements Context<G, M> {

  public static <G, M> AbstractContext<G, M>
      fromPredicate(final SetList<G> objects, final SetList<M> attributes, final BiPredicate<G, M> predicate) {
    return new AbstractContext<G, M>(objects, attributes, false) {

      @Override
      public final boolean contains(final Object o1, final Object o2) {
        try {
          return predicate.test((G) o1, (M) o2);
        } catch (ClassCastException e) {
          return false;
        }
      }

    };
  }

  public static <G> AbstractContext<G, G> fromPredicate(final SetList<G> objects, final BiPredicate<G, G> predicate) {
    return new AbstractContext<G, G>(objects, objects, true) {

      @Override
      public final boolean contains(final Object o1, final Object o2) {
        try {
          return predicate.test((G) o1, (G) o2);
        } catch (ClassCastException e) {
          return false;
        }
      }

    };
  }

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
