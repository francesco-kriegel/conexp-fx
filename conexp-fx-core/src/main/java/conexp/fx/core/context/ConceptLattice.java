/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.context;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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
import java.util.concurrent.ConcurrentHashMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.relation.MatrixRelation;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;

public final class ConceptLattice<G, M> extends MatrixRelation<Concept<G, M>, Concept<G, M>>
{
  public final MatrixContext<G, M>                     context;
  public final ObservableMap<G, Concept<G, M>>         objectConcepts    =
                                                                             FXCollections
                                                                                 .observableMap(new ConcurrentHashMap<G, Concept<G, M>>());
  public final ObservableMap<M, Concept<G, M>>         attributeConcepts =
                                                                             FXCollections
                                                                                 .observableMap(new ConcurrentHashMap<M, Concept<G, M>>());
  private MatrixRelation<Concept<G, M>, Concept<G, M>> order;

  public ConceptLattice(final MatrixContext<G, M> context)
  {
    super(true);
    this.context = context;
    order = order();
    addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>()
    {
      @Override
      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event)
      {
        order = order();
      }
    }, RelationEvent.ALL_CHANGED);
  }

  public final Set<Concept<G, M>> lowerNeighbors(final Concept<G, M> concept)
  {
    return col(concept);
  }

  public final Set<Concept<G, M>> upperNeighbors(final Concept<G, M> concept)
  {
    return row(concept);
  }

  // lower
  public final Set<Concept<G, M>> ideal(final Concept<G, M> concept)
  {
    return order.col(concept);
  }

  // upper
  public final Set<Concept<G, M>> filter(final Concept<G, M> concept)
  {
    return order.row(concept);
  }

  public final Set<Concept<G, M>> interval(final Concept<G, M> lower, final Concept<G, M> upper)
  {
    return Sets.intersection(filter(lower), ideal(upper));
  }

  public final Set<Concept<G, M>> complement(final Set<Concept<G, M>> concepts)
  {
    return Sets.difference(rowHeads(), concepts);
  }

  @SafeVarargs
  public final Concept<G, M> supremum(final Concept<G, M>... concepts)
  {
    final Set<M> intent = new HashSet<M>(context.colHeads());
    for (Concept<G, M> concept : concepts)
      intent.retainAll(concept.intent());
    final Set<G> extent = new HashSet<G>(context.colAnd(intent));
    return new Concept<G, M>(extent, intent);
  }

  public final Concept<G, M> supremum(final Iterable<Concept<G, M>> concepts)
  {
    final Set<M> intent = new HashSet<M>(context.colHeads());
    for (Concept<G, M> concept : concepts)
      intent.retainAll(concept.intent());
    final Set<G> extent = new HashSet<G>(context.colAnd(intent));
    return new Concept<G, M>(extent, intent);
  }

  @SafeVarargs
  public final Concept<G, M> infimum(final Concept<G, M>... concepts)
  {
    final Set<G> extent = new HashSet<G>(context.rowHeads());
    for (Concept<G, M> concept : concepts)
      extent.retainAll(concept.extent());
    final Set<M> intent = new HashSet<M>(context.rowAnd(extent));
    return new Concept<G, M>(extent, intent);
  }

  public final Concept<G, M> infimum(final Iterable<Concept<G, M>> concepts)
  {
    final Set<G> extent = new HashSet<G>(context.rowHeads());
    for (Concept<G, M> concept : concepts)
      extent.retainAll(concept.extent());
    final Set<M> intent = new HashSet<M>(context.rowAnd(extent));
    return new Concept<G, M>(extent, intent);
  }

  public final Set<G> objectLabels(final Concept<G, M> concept)
  {
    return Maps.filterValues(objectConcepts, Predicates.equalTo(concept)).keySet();
  }

  public final Set<M> attributeLabels(final Concept<G, M> concept)
  {
    return Maps.filterValues(attributeConcepts, Predicates.equalTo(concept)).keySet();
  }
}
