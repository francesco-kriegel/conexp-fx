package conexp.fx.core.dl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.semanticweb.owlapi.model.IRI;

import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.relation.MatrixRelation;
import conexp.fx.core.math.BooleanMatrices;

/*-
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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

public class ELInterpretation2<I> {

  private final MatrixRelation<I, IRI>         conceptNameExtensionMatrix;
  private final Map<IRI, MatrixRelation<I, I>> roleNameExtensionMatrix;

  public ELInterpretation2() {
    super();
    this.conceptNameExtensionMatrix = new MatrixRelation<>(false);
    this.roleNameExtensionMatrix = new HashMap<>();
  }

  public ELInterpretation2(
      MatrixRelation<I, IRI> conceptNameExtensionMatrix,
      Map<IRI, MatrixRelation<I, I>> roleNameExtensionMatrix) {
    super();
    this.conceptNameExtensionMatrix = conceptNameExtensionMatrix;
    this.roleNameExtensionMatrix = roleNameExtensionMatrix;
  }

  public final ELInterpretation2<I> clone() {
    final MatrixRelation<I, IRI> _conceptNameExtensionMatrix = this.conceptNameExtensionMatrix.clone();
    final Map<IRI, MatrixRelation<I, I>> _roleNameExtensionMatrix = new HashMap<>();
    this.roleNameExtensionMatrix
        .entrySet()
        .forEach(entry -> _roleNameExtensionMatrix.put(entry.getKey(), entry.getValue().clone()));
    return new ELInterpretation2<>(_conceptNameExtensionMatrix, _roleNameExtensionMatrix);
  }

  public final Signature getSignature(final boolean onlyActiveSignature) {
    final Signature sigma = new Signature(IRI.generateDocumentIRI());
    if (!onlyActiveSignature) {
      sigma.getConceptNames().addAll(conceptNameExtensionMatrix.colHeads());
      sigma.getRoleNames().addAll(roleNameExtensionMatrix.keySet());
    } else {
      for (IRI A : conceptNameExtensionMatrix.colHeads())
        if (!conceptNameExtensionMatrix.col(A).isEmpty())
          sigma.getConceptNames().add(A);
      for (IRI r : roleNameExtensionMatrix.keySet())
        if (!roleNameExtensionMatrix.get(r).isEmpty())
          sigma.getRoleNames().add(r);
    }
    return sigma;
  }

  public final MatrixRelation<I, IRI> getConceptNameExtensionMatrix() {
    return this.conceptNameExtensionMatrix;
  }

  public final MatrixRelation<I, I> getRoleNameExtensionMatrix(final IRI roleName) {
    return this.roleNameExtensionMatrix.computeIfAbsent(roleName, __ -> new MatrixRelation<>(true));
  }

  public final Map<IRI, MatrixRelation<I, I>> getRoleNameExtensionMatrixMap() {
    return this.roleNameExtensionMatrix;
  }

  public final Set<I> getDomain() {
    return roleNameExtensionMatrix
        .values()
        .stream()
        .reduce((Set<I>) conceptNameExtensionMatrix.rowHeads(), (s, m) -> Sets.union(s, m.colHeads()), Sets::union);
  }

  public final boolean add(final I i, final IRI A) {
    return conceptNameExtensionMatrix.add(i, A);
  }

  public final boolean add(final I i, final String A) {
    return add(i, IRI.create(A));
  }

  public final boolean add(final I i, final IRI r, final I j) {
    return getRoleNameExtensionMatrix(r).add(i, j);
  }

  public final boolean add(final I i, final String r, final I j) {
    return add(i, IRI.create(r), j);
  }

  private final Predicate<I> satisfiesAllExistentialRestrictions(final ELConceptDescription conceptDescription) {
    return i -> conceptDescription
        .getExistentialRestrictions()
        .entries()
        .parallelStream()
        .allMatch(
            e -> roleNameExtensionMatrix.containsKey(e.getKey())
                && roleNameExtensionMatrix.get(e.getKey()).rowHeads().contains(i)
                    ? roleNameExtensionMatrix
                        .get(e.getKey())
                        .row(i)
                        .parallelStream()
                        .anyMatch(j -> isInExtensionOf(j, e.getValue()))
                    : false);
  }

  public final Set<I> getExtension(final ELConceptDescription conceptDescription) {
    if (conceptDescription.isBot())
      return Collections.emptySet();
    else if (conceptDescription.isTop())
      return new HashSet<>(this.getDomain());
    else
      return this.conceptNameExtensionMatrix
          .colAnd(conceptDescription.getConceptNames())
          .parallelStream()
          .filter(satisfiesAllExistentialRestrictions(conceptDescription))
          .collect(Collectors.toSet());
  }

  public final boolean isInExtensionOf(final I i, final ELConceptDescription conceptDescription) {
    if (conceptDescription.isBot())
      return false;
    else if (conceptDescription.isTop())
      return true;
    else
      return this.conceptNameExtensionMatrix.colAnd(conceptDescription.getConceptNames()).contains(i)
          && satisfiesAllExistentialRestrictions(conceptDescription).test(i);
  }

  public final boolean models(final ELConceptInclusion conceptInclusion) {
    return getDomain()
        .parallelStream()
        .allMatch(
            Predicates
                .<I> not(i -> isInExtensionOf(i, conceptInclusion.getSubsumee()))
                .or(i -> isInExtensionOf(i, conceptInclusion.getSubsumer())));
  }

  public final boolean models(final ELTBox tBox) {
    return tBox.getConceptInclusions().parallelStream().allMatch(this::models);
  }

  public final ELConceptDescription getMostSpecificConceptDescription(final I object, final int roleDepth) {
    if (roleDepth < 0)
      throw new IllegalArgumentException();
    else {
      final ELConceptDescription mmsc = new ELConceptDescription();
      mmsc.getConceptNames().addAll(conceptNameExtensionMatrix.row(object));
      if (roleDepth > 0) {
        for (Entry<IRI, MatrixRelation<I, I>> e : roleNameExtensionMatrix.entrySet())
          if (e.getValue().rowHeads().contains(object))
            for (I successor : e.getValue().row(object))
              mmsc
                  .getExistentialRestrictions()
                  .put(e.getKey(), getMostSpecificConceptDescription(successor, roleDepth - 1));
      }
      return mmsc.reduce();
    }
  }

  public final ELConceptDescription getMostSpecificConceptDescription(final Set<I> objects, final int roleDepth) {
    if (roleDepth < 0)
      throw new IllegalArgumentException();
    else if (objects.isEmpty())
      return ELConceptDescription.bot();
    else {
      final Iterator<I> it = objects.iterator();
      ELConceptDescription mmsc = getMostSpecificConceptDescription(it.next(), roleDepth);
      while (it.hasNext())
        mmsc = ELLeastCommonSubsumer.lcs(mmsc, getMostSpecificConceptDescription(it.next(), roleDepth));
      return mmsc;
    }
//      return objects
//          .parallelStream()
//          .reduce(
//              ELConceptDescription.bot(),
//              (mmsc, object) -> ELLeastCommonSubsumer.lcs(mmsc, getMostSpecificConceptDescription(object, roleDepth)),
//              ELLeastCommonSubsumer::lcs)
//          .reduce();
  }

  public final ELConceptDescription getMostSpecificConceptDescription2(final Set<I> objects, final int roleDepth) {
    if (roleDepth < 0)
      throw new IllegalArgumentException();
    else if (objects.isEmpty())
      return ELConceptDescription.bot();
    else {
      final ELConceptDescription mmsc = new ELConceptDescription();
      mmsc.getConceptNames().addAll(this.conceptNameExtensionMatrix.rowAnd(objects));
      if (roleDepth > 0) {
        for (Entry<IRI, MatrixRelation<I, I>> e : roleNameExtensionMatrix.entrySet()) {
          final IRI roleName = e.getKey();
          final Set<Set<I>> hypergraph = objects
              .parallelStream()
              .map(o -> e.getValue().rowHeads().contains(o) ? e.getValue().row(o) : Collections.<I> emptySet())
              .collect(Collectors.toSet());
          for (Set<I> mhs : getMinimalHittingSets(hypergraph)) {
            mmsc.getExistentialRestrictions().put(roleName, getMostSpecificConceptDescription(mhs, roleDepth - 1));
          }
        }
      }
      return mmsc.reduce();
    }
  }

  public static final <T> Set<Set<T>> getMinimalHittingSets(final Set<Set<T>> hypergraph) {
    if (hypergraph.isEmpty())
      return Collections.<Set<T>> emptySet();
    else {
      final Set<Set<T>> identity = Collections.singleton(Collections.emptySet());
      final BiFunction<Set<Set<T>>, Set<T>, Set<Set<T>>> accumulator = (hittingSets, factor) -> hittingSets
          .parallelStream()
          .flatMap(hittingSet -> factor.parallelStream().map(element -> {
            final Set<T> newHittingSet = new HashSet<>(hittingSet);
            newHittingSet.add(element);
            return newHittingSet;
          }))
          .collect(Collectors.toSet());
      final BinaryOperator<Set<Set<T>>> combiner = (hittingSets1, hittingSets2) -> hittingSets1
          .parallelStream()
          .flatMap(hittingSet1 -> hittingSets2.parallelStream().map(hittingSet2 -> {
            final Set<T> newHittingSet = new HashSet<>(hittingSet1);
            newHittingSet.addAll(hittingSet2);
            return newHittingSet;
          }))
          .collect(Collectors.toSet());
      final Set<Set<T>> hittingSets = hypergraph.parallelStream().reduce(identity, accumulator, combiner);
      final Set<Set<T>> nonMinimalHittingSets = Sets.newConcurrentHashSet();
      hittingSets.parallelStream().forEach(hittingSet1 -> {
        if (hittingSets
            .parallelStream()
            .anyMatch(hittingSet2 -> hittingSet1.containsAll(hittingSet2) && !hittingSet2.containsAll(hittingSet1)))
          nonMinimalHittingSets.add(hittingSet1);
      });
      hittingSets.removeAll(nonMinimalHittingSets);
      return hittingSets;
    }
  }

  public ELInterpretation2<Set<I>> reduce() {
    // Step 1: Domain Reduction
    final BiPredicate<I, I> bisimilar =
        (i, j) -> (new ELsiConceptDescription<I>(this, i)).isEquivalentTo(new ELsiConceptDescription<I>(this, j));
    final Set<Set<I>> quotientDomain = Collections3.quotient(getDomain(), bisimilar);
    final ELInterpretation2<Set<I>> reduction = new ELInterpretation2<Set<I>>();
    final Signature activeSignature = this.getSignature(true);
    reduction.getConceptNameExtensionMatrix().colHeads().addAll(activeSignature.getConceptNames());
    reduction.getConceptNameExtensionMatrix().rowHeads().addAll(quotientDomain);
    activeSignature.getConceptNames().forEach(conceptName -> {
      final Set<Set<I>> col = reduction.getConceptNameExtensionMatrix().col(conceptName);
      quotientDomain.forEach(equivalenceClass -> {
        if (getConceptNameExtensionMatrix().contains(equivalenceClass.iterator().next(), conceptName))
          col.add(equivalenceClass);
      });
    });
    activeSignature.getRoleNames().forEach(roleName -> {
      reduction.getRoleNameExtensionMatrix(roleName).rowHeads().addAll(quotientDomain);
      quotientDomain.forEach(equivalenceClass -> {
        quotientDomain.forEach(fquivalenceClass -> {
          if ((new ELsiConceptDescription<I>(this, equivalenceClass.iterator().next()))
              .isSubsumedBy(
                  ELsiConceptDescription
                      .exists(roleName, new ELsiConceptDescription<I>(this, fquivalenceClass.iterator().next()))))
            reduction.getRoleNameExtensionMatrix(roleName).add(equivalenceClass, fquivalenceClass);
        });
      });
    });

    // Step 2: Successor Reduction
    activeSignature.getRoleNames().forEach(roleName -> {
      final MatrixRelation<Set<I>, Set<I>> roleExtension = reduction.getRoleNameExtensionMatrix(roleName);
      roleExtension.rowHeads().forEach(x -> {
        final Set<Set<I>> successors = roleExtension.row(x);
        final Set<Set<I>> superfluousSuccessors = Sets.newConcurrentHashSet();
        successors.forEach(y -> {
          successors.forEach(z -> {
            if (!(y.equals(z)) && (new ELsiConceptDescription<Set<I>>(reduction, y))
                .isSubsumedBy(new ELsiConceptDescription<Set<I>>(reduction, z)))
              superfluousSuccessors.add(z);
          });
        });
        successors.removeAll(superfluousSuccessors);
      });
    });

    // Step 3: Connected Component
    // cannot be computed here, since we have no selected root element

    return reduction;
  }

  public ELInterpretation2<I> connectedComponent(I root) {
    final MatrixRelation<I, I> reachabilityMatrix = new MatrixRelation<I, I>(true);
    this.getRoleNameExtensionMatrixMap().keySet().forEach(roleName -> {
      reachabilityMatrix.addAll(this.getRoleNameExtensionMatrix(roleName));
    });
    reachabilityMatrix.setMatrix(BooleanMatrices.transitiveClosure(reachabilityMatrix.matrix()));
    final Set<I> reachableElements = new HashSet<I>(reachabilityMatrix.row(root));
    final ELInterpretation2<I> connectedComponent = this.clone();
    connectedComponent.getConceptNameExtensionMatrix().rowHeads().retainAll(reachableElements);
    connectedComponent.getRoleNameExtensionMatrixMap().keySet().forEach(roleName -> {
      connectedComponent.getRoleNameExtensionMatrix(roleName).rowHeads().retainAll(reachableElements);
    });
    return connectedComponent;
  }

  public static <J> ELInterpretation2<List<J>> productOf(List<ELInterpretation2<J>> factors) {
    final int n = factors.size();
    final HashSet<List<J>> productDomain = new HashSet<>(
        Lists
            .transform(
                Lists.cartesianProduct(Lists.transform(factors, factor -> new ArrayList<J>(factor.getDomain()))),
                tuple -> new ArrayList<J>(tuple)));
    final ELInterpretation2<List<J>> product = new ELInterpretation2<>();
    final Set<IRI> conceptNames = new HashSet<>(
        factors
            .stream()
            .map(factor -> (Set<IRI>) factor.getSignature(true).getConceptNames())
            .reduce(Sets::intersection)
            .get());
    final Set<IRI> roleNames = new HashSet<>(
        factors
            .stream()
            .map(factor -> (Set<IRI>) factor.getSignature(true).getRoleNames())
            .reduce(Sets::intersection)
            .get());
    productDomain.forEach(tuple -> {
      conceptNames.forEach(conceptName -> {
        if (IntStream
            .range(0, n)
            .allMatch(k -> factors.get(k).getConceptNameExtensionMatrix().contains(tuple.get(k), conceptName)))
          product.getConceptNameExtensionMatrix().add(tuple, conceptName);
      });
      roleNames.forEach(roleName -> {
        productDomain.forEach(tuple2 -> {
          if (IntStream
              .range(0, n)
              .allMatch(k -> factors.get(k).getRoleNameExtensionMatrix(roleName).contains(tuple.get(k), tuple2.get(k))))
            product.getRoleNameExtensionMatrix(roleName).add(tuple, tuple2);
        });
      });
    });
    return product;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof ELInterpretation2))
      return false;
    final ELInterpretation2<?> other = (ELInterpretation2<?>) obj;
    return this.conceptNameExtensionMatrix.equals(other.conceptNameExtensionMatrix)
        && this.roleNameExtensionMatrix.equals(other.roleNameExtensionMatrix);
  }

  @Override
  public int hashCode() {
    return 67 * this.conceptNameExtensionMatrix.hashCode() + 257 * this.roleNameExtensionMatrix.hashCode();
  }

  @Override
  public String toString() {
    return "Interpretation\r\n" + conceptNameExtensionMatrix + "\r\n" + roleNameExtensionMatrix;
  }

}
