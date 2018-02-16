/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.context;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.relation.MatrixRelation;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;

public final class ConceptLattice<G, M> extends MatrixRelation<Concept<G, M>, Concept<G, M>> {

  public final MatrixContext<G, M>                     context;
  public final ObservableMap<G, Concept<G, M>>         objectConcepts    =
                                                                             FXCollections
                                                                                 .observableMap(new ConcurrentHashMap<G, Concept<G, M>>());
  public final ObservableMap<M, Concept<G, M>>         attributeConcepts =
                                                                             FXCollections
                                                                                 .observableMap(new ConcurrentHashMap<M, Concept<G, M>>());
  private MatrixRelation<Concept<G, M>, Concept<G, M>> order;

  public ConceptLattice(final MatrixContext<G, M> context) {
    super(true);
    this.context = context;
    order = order();
    addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      @Override
      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
        order = order();
      }
    }, RelationEvent.ALL_CHANGED);
  }

  public final Set<Concept<G, M>> lowerNeighbors(final Concept<G, M> concept) {
    return col(concept);
  }

  public final Set<Concept<G, M>> upperNeighbors(final Concept<G, M> concept) {
    return row(concept);
  }

  // lower
  public final Set<Concept<G, M>> ideal(final Concept<G, M> concept) {
    return order.col(concept);
  }

  // upper
  public final Set<Concept<G, M>> filter(final Concept<G, M> concept) {
    return order.row(concept);
  }

  public final Set<Concept<G, M>> interval(final Concept<G, M> lower, final Concept<G, M> upper) {
    return Sets.intersection(filter(lower), ideal(upper));
  }

  public final Set<Concept<G, M>> complement(final Set<Concept<G, M>> concepts) {
    return Sets.difference(rowHeads(), concepts);
  }

  @SafeVarargs
  public final Concept<G, M> supremum(final Concept<G, M>... concepts) {
    final Set<M> intent = new HashSet<M>(context.colHeads());
    for (Concept<G, M> concept : concepts)
      intent.retainAll(concept.intent());
    final Set<G> extent = new HashSet<G>(context.colAnd(intent));
    return new Concept<G, M>(extent, intent);
  }

  public final Concept<G, M> supremum(final Iterable<Concept<G, M>> concepts) {
    final Set<M> intent = new HashSet<M>(context.colHeads());
    for (Concept<G, M> concept : concepts)
      intent.retainAll(concept.intent());
    final Set<G> extent = new HashSet<G>(context.colAnd(intent));
    return new Concept<G, M>(extent, intent);
  }

  @SafeVarargs
  public final Concept<G, M> infimum(final Concept<G, M>... concepts) {
    final Set<G> extent = new HashSet<G>(context.rowHeads());
    for (Concept<G, M> concept : concepts)
      extent.retainAll(concept.extent());
    final Set<M> intent = new HashSet<M>(context.rowAnd(extent));
    return new Concept<G, M>(extent, intent);
  }

  public final Concept<G, M> infimum(final Iterable<Concept<G, M>> concepts) {
    final Set<G> extent = new HashSet<G>(context.rowHeads());
    for (Concept<G, M> concept : concepts)
      extent.retainAll(concept.extent());
    final Set<M> intent = new HashSet<M>(context.rowAnd(extent));
    return new Concept<G, M>(extent, intent);
  }

  public final Set<G> objectLabels(final Concept<G, M> concept) {
    return Maps.filterValues(objectConcepts, Predicates.equalTo(concept)).keySet();
  }

  public final Set<M> attributeLabels(final Concept<G, M> concept) {
    return Maps.filterValues(attributeConcepts, Predicates.equalTo(concept)).keySet();
  }

  /**
   * The method computes the luxenburger base for partial implications, whose confidence is at least minConfidence. If
   * sorted is set to true, then the partial implications are sorted according to descending confidence, and a List is
   * returned. Otherwise an unsorted Set is returned.
   * 
   * @param minConfidence
   * @param sorted
   * @return a collection of implications
   */
  public final Collection<Implication<G, M>> luxenburgerBase(final double minConfidence, final boolean sorted) {
    if (minConfidence < 0d || minConfidence > 1d)
      throw new IllegalArgumentException("Confidence must be in range [0,1].");
    Stream<Implication<G, M>> s = new LuxenburgerBaseExtractor().get().parallelStream();
    if (minConfidence > 0d)
      s = s.filter(i -> i.getConfidence() >= minConfidence);
    if (sorted)
      return s.sorted((i, j) -> (int) Math.signum(j.getConfidence() - i.getConfidence())).collect(Collectors.toList());
    return s.collect(Collectors.toSet());
  }

//  public final List<Implication<G, M>> luxenburgerBase() {
//    return new LuxenburgerBaseExtractor()
//        .get()
//        .parallelStream()
//        .sorted((i, j) -> (int) Math.signum(j.getConfidence() - i.getConfidence()))
//        .collect(Collectors.toList());
//  }
//
//  public final List<Implication<G, M>> luxenburgerBase(final double confidence) {
//    if (confidence < 0d || confidence > 1d)
//      throw new IllegalArgumentException("Confidence must be in range [0,1].");
//    return new LuxenburgerBaseExtractor()
//        .get()
//        .parallelStream()
//        .filter(i -> i.getConfidence() >= confidence)
//        .sorted((i, j) -> (int) Math.signum(j.getConfidence() - i.getConfidence()))
//        .collect(Collectors.toList());
//  }

  private final class LuxenburgerBaseExtractor {

    private final Set<Implication<G, M>> set = Collections
                                                 .newSetFromMap(new ConcurrentHashMap<Implication<G, M>, Boolean>());

    private final Set<Implication<G, M>> get() {
      recursion(context.selection.topConcept());
      return set;
    }

    private final void recursion(final Concept<G, M> concept) {
      col(concept).parallelStream().forEach(
          lowerNeighbor -> {
            set.add(new Implication<G, M>(concept.getIntent(), Sets.newHashSet(Sets.difference(
                lowerNeighbor.getIntent(),
                concept.getIntent())), lowerNeighbor.getExtent(), ((double) lowerNeighbor.getExtent().size())
                / ((double) concept.getExtent().size())));
            recursion(lowerNeighbor);
          });
    }

  }

}
