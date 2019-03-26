package conexp.fx.core.dl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.relation.MatrixRelation;

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

  public final ELConceptDescription getMostSpecificConceptDescription(final Set<I> objects, final int roleDepth) {
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

  @Override
  public String toString() {
    return "Interpretation\r\n" + conceptNameExtensionMatrix + "\r\n" + roleNameExtensionMatrix;
  }

}
