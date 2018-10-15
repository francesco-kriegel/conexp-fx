package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.math.PartialComparable;
import conexp.fx.core.util.UnicodeSymbols;

public final class ELConceptDescription implements PartialComparable<ELConceptDescription>, Cloneable {

  private static final OWLDataFactory df = OWLManager.getOWLDataFactory();

  public static final ELConceptDescription of(final OWLClassExpression concept) {
    return new ELConceptDescription(concept);
  }

  public static final ELConceptDescription bot() {
    return new ELConceptDescription(Sets.newHashSet(df.getOWLNothing().getIRI()), HashMultimap.create());
  }

  public static final ELConceptDescription top() {
    return new ELConceptDescription();
  }

  public static final ELConceptDescription conceptName(final IRI conceptName) {
    return new ELConceptDescription(Sets.newHashSet(conceptName), HashMultimap.create());
  }

  public static final ELConceptDescription
      existentialRestriction(final IRI roleName, final ELConceptDescription filler) {
    final ELConceptDescription C = new ELConceptDescription();
    C.getExistentialRestrictions().put(roleName, filler);
    return C;
  }

  public static final ELConceptDescription existentialRestriction(List<IRI> rs, ELConceptDescription C) {
    ELConceptDescription rsC = C.clone();
    for (IRI r : rs)
      rsC = ELConceptDescription.existentialRestriction(r, rsC);
    return rsC;
  }

  public static final ELConceptDescription conjunction(final ELConceptDescription... conjuncts) {
    return conjunction(Arrays.asList(conjuncts));
  }

  public static final ELConceptDescription conjunction(final Collection<ELConceptDescription> conjuncts) {
    final ELConceptDescription conjunction = new ELConceptDescription();
    conjuncts.forEach(conjunct -> {
      conjunction.getConceptNames().addAll(conjunct.getConceptNames());
      conjunction.getExistentialRestrictions().putAll(conjunct.getExistentialRestrictions());
    });
    return conjunction;
  }

  public static final BiPredicate<ELConceptDescription, ELConceptDescription> quasiOrder() {
    return (x, y) -> x.isSubsumedBy(y);
  }

  public static final BiPredicate<ELConceptDescription, ELConceptDescription> dualQuasiOrder() {
    return (x, y) -> x.subsumes(y);
  }

  public static final BiPredicate<ELConceptDescription, ELConceptDescription> equivalence() {
    return (x, y) -> x.isEquivalentTo(y);
  }

  public static final BiPredicate<ELConceptDescription, ELConceptDescription> neighborhood() {
    return (x, y) -> x.isLowerNeighborOf(y);
  }

  public static final BiPredicate<ELConceptDescription, ELConceptDescription> dualNeighborhood() {
    return (x, y) -> x.isUpperNeighborOf(y);
  }

  public static final BiFunction<ELConceptDescription, ELConceptDescription, Integer> distance() {
    return (x, y) -> x.distanceTo(y);
  }

  private final Set<IRI>                            conceptNames;
  private final Multimap<IRI, ELConceptDescription> existentialRestrictions;
  // private final Set<Pair<IRI, ELConceptDescription>> valueRestrictions;
  // private final Set<Pair<Pair<Integer, IRI>, ELConceptDescription>>
  // qualifiedGreaterThanRestrictions;
  // private final Set<Pair<Pair<Integer, IRI>, ELConceptDescription>>
  // qualifiedSmallerThanRestrictions;
  // private final Set<IRI> negatedConceptNames;
  // private final Set<IRI> extistentialSelfRestrictions;

  /**
   * @param concept
   * 
   *          Creates a new EL normal form from an OWLClassExpression.
   */
  public ELConceptDescription(final OWLClassExpression concept) {
    super();
    this.conceptNames = new HashSet<>();
    this.existentialRestrictions = HashMultimap.create();
    if (concept.isOWLThing())
      return;
    if (concept.isOWLNothing()) {
      conceptNames.add(df.getOWLNothing().getIRI());
      return;
    }
    if (concept instanceof OWLClass) {
      this.conceptNames.add(((OWLClass) concept).getIRI());
      return;
    }
    if (concept instanceof OWLObjectSomeValuesFrom) {
      final OWLObjectSomeValuesFrom existentialRestriction = (OWLObjectSomeValuesFrom) concept;
      this.existentialRestrictions.put(
          ((OWLObjectProperty) existentialRestriction.getProperty()).getIRI(),
          new ELConceptDescription(existentialRestriction.getFiller()));
      return;
    }
    if (concept instanceof OWLObjectIntersectionOf) {
      final OWLObjectIntersectionOf conjunction = (OWLObjectIntersectionOf) concept;
      for (OWLClassExpression conjunct : conjunction.asConjunctSet())
        if (conjunct instanceof OWLClass)
          this.conceptNames.add(((OWLClass) conjunct).getIRI());
        else if (conjunct instanceof OWLObjectSomeValuesFrom)
          this.existentialRestrictions.put(
              ((OWLObjectProperty) ((OWLObjectSomeValuesFrom) conjunct).getProperty()).getIRI(),
              new ELConceptDescription(((OWLObjectSomeValuesFrom) conjunct).getFiller()));
        else
          throw new ELSyntaxException();
      return;
    }
    throw new ELSyntaxException();
  }

  /**
   * @param conceptNames
   * @param existentialRestrictions
   * 
   *          Creates a new EL normal form. If the sets conceptNames and existentitalRestrictions are both empty, then
   *          the constructed normal form represents the top concept.
   */
  public ELConceptDescription(
      final Set<IRI> conceptNames,
      final Multimap<IRI, ELConceptDescription> existentialRestrictions) {
    super();
    this.conceptNames = conceptNames;
    this.existentialRestrictions = existentialRestrictions;
  }

  public ELConceptDescription() {
    this.conceptNames = Sets.newHashSet();
    this.existentialRestrictions = HashMultimap.create();
  }

  public final boolean isBot() {
    return conceptNames.contains(df.getOWLNothing().getIRI());
  }

  public final boolean isTop() {
    return conceptNames.isEmpty() && existentialRestrictions.isEmpty();
  }

  public final Set<IRI> getConceptNames() {
    return conceptNames;
  }

  public final Multimap<IRI, ELConceptDescription> getExistentialRestrictions() {
    return existentialRestrictions;
  }

  public final OWLClassExpression toOWLClassExpression() {
    if (isTop())
      return df.getOWLThing();
    if (isBot())
      return df.getOWLNothing();
    if (conceptNames.size() == 1 && existentialRestrictions.isEmpty())
      return df.getOWLClass(conceptNames.iterator().next());
    if (conceptNames.isEmpty() && existentialRestrictions.size() == 1) {
      final Entry<IRI, ELConceptDescription> existentialRestriction =
          existentialRestrictions.entries().iterator().next();
      return df.getOWLObjectSomeValuesFrom(
          df.getOWLObjectProperty(existentialRestriction.getKey()),
          existentialRestriction.getValue().toOWLClassExpression());
    }
    final Set<OWLClassExpression> conjuncts = new HashSet<>();
    for (IRI conceptName : conceptNames)
      conjuncts.add(df.getOWLClass(conceptName));
    for (Entry<IRI, ELConceptDescription> existentialRestriction : existentialRestrictions.entries())
      conjuncts.add(
          df.getOWLObjectSomeValuesFrom(
              df.getOWLObjectProperty(existentialRestriction.getKey()),
              existentialRestriction.getValue().toOWLClassExpression()));
    return df.getOWLObjectIntersectionOf(conjuncts);
  }

  public final boolean isSubsumedBy(final ELConceptDescription other) {
    return ELReasoner.isSubsumedBy(this, other);
  }

  public final boolean subsumes(final ELConceptDescription other) {
    return ELReasoner.subsumes(this, other);
  }

  public final boolean isEquivalentTo(final ELConceptDescription other) {
    return Stream.<Supplier<Boolean>> of(() -> this.subsumes(other), () -> other.subsumes(this)).parallel().allMatch(
        Supplier::get);
  }

  public final boolean isLowerNeighborOf(final ELConceptDescription other) {
    return this.upperNeighborsReduced().parallelStream().anyMatch(other::isEquivalentTo);
  }

  public final boolean isUpperNeighborOf(final ELConceptDescription other) {
    return other.isLowerNeighborOf(this);
  }

  public final ELConceptDescription reduce() {
    for (Entry<IRI, ELConceptDescription> er : existentialRestrictions.entries())
      er.getValue().reduce();
    final Function<Entry<IRI, ELConceptDescription>, Stream<Pair<Entry<IRI, ELConceptDescription>, Entry<IRI, ELConceptDescription>>>> f =
        er1 -> existentialRestrictions
            .entries()
            .parallelStream()
            .filter(er2 -> er1.getKey().equals(er2.getKey()))
            .filter(er2 -> !er1.equals(er2))
            .map(er2 -> Pair.of(er1, er2));
    final Set<Entry<IRI, ELConceptDescription>> superfluousERs = existentialRestrictions
        .entries()
        .parallelStream()
        .map(f)
        .reduce(Stream::concat)
        .orElseGet(Stream::empty)
        .filter(p -> p.first().getValue().isSubsumedBy(p.second().getValue()))
        .map(Pair::second)
        .collect(Collectors.toSet());
    superfluousERs.forEach(x -> existentialRestrictions.remove(x.getKey(), x.getValue()));
    return this;
  }

  public final int roleDepth() {
    if (existentialRestrictions.isEmpty())
      return 0;
    return 1 + existentialRestrictions
        .entries()
        .parallelStream()
        .map(Entry::getValue)
        .map(ELConceptDescription::roleDepth)
        .max(Integer::compare)
        .orElse(0);
  }

  public final int topLevelConjuncts() {
    return conceptNames.size() + existentialRestrictions.size();
  }

  public final int size() {
    return Math.max(
        1,
        2 * conceptNames.size() + existentialRestrictions.size() - 1
            + existentialRestrictions
                .entries()
                .parallelStream()
                .map(Entry::getValue)
                .map((ELConceptDescription c) -> 1 + c.size())
                .reduce(0, Integer::sum));
  }

  public final int rank() {
    return this.clone().reduce().unreducedRank();
  }

  public final int unreducedRank() {
    if (this.isTop())
      return 0;
    else
      return 1 + this.oneUpperNeighbor().unreducedRank();
  }

  private final ELConceptDescription oneUpperNeighbor() {
    if (!this.conceptNames.isEmpty()) {
      final ELConceptDescription upperNeighbor = this.clone();
      final Iterator<IRI> it = upperNeighbor.conceptNames.iterator();
      it.next();
      it.remove();
      return upperNeighbor;
    } else if (!this.existentialRestrictions.isEmpty()) {
      final ELConceptDescription upperNeighbor = this.clone();
      final Iterator<Entry<IRI, ELConceptDescription>> it = upperNeighbor.existentialRestrictions.entries().iterator();
      final Entry<IRI, ELConceptDescription> ER = it.next();
      it.remove();
      ER
          .getValue()
          .upperNeighborsReduced()
          .parallelStream()
          .filter(
              uER -> upperNeighbor.existentialRestrictions
                  .entries()
                  .parallelStream()
                  .filter(otherER -> !otherER.equals(ER))
                  .filter(otherER -> ER.getKey().equals(otherER.getKey()))
                  .map(Entry::getValue)
                  .noneMatch(uER::subsumes))
          .map(uER -> Pair.of(ER.getKey(), uER))
          .sequential()
          .forEach(p -> upperNeighbor.existentialRestrictions.put(p.first(), p.second()));
      return upperNeighbor;
    } else
      return null;
  }

//  public final int rank2() {
//    final ELConceptDescription C = this.clone().reduce();
//    int r = C.conceptNames.size();
//    C.conceptNames.clear();
//    if (C.existentialRestrictions.isEmpty())
//      return r;
//    else if (C.existentialRestrictions.size() == 1)
//      return r + rank2existentialRestriction(
//          ELConceptDescription.existentialRestriction(C.existentialRestrictions.iterator().next()));
//    else
//      return r + rank2conjunction(C);
//  }
//
//  private static final int rank2conjunction(final ELConceptDescription conjunction) {
//    if (!conjunction.conceptNames.isEmpty())
//      throw new IllegalArgumentException();
//    final Set<Pair<IRI, ELConceptDescription>> conjuncts = conjunction.existentialRestrictions;
//    if (conjuncts.isEmpty())
//      return 0;
//    else if (conjuncts.size() == 1)
//      return rank2existentialRestriction(ELConceptDescription.existentialRestriction(conjuncts.iterator().next()));
//    else {
//      return IntStream.range(1, conjuncts.size() + 1).boxed().map(i -> {
//        return (int) (Math.pow(-1, i + 1) * Sets
//            .combinations(
//                conjuncts
//                    .parallelStream()
//                    .map(ELConceptDescription::existentialRestriction)
//                    // .map(ELConceptDescription::clone)
//                    .collect(Collectors.toSet()),
//                i)
//            .parallelStream()
//            .map(ELLeastCommonSubsumer::_of)
//            .map(ELConceptDescription::clone)
//            // .map(ELConceptDescription::getReducedForm)
//            .collect(Collectors.summingInt(ELConceptDescription::rank2conjunction)));
//      }).collect(Collectors.summingInt(Integer::intValue));
//    }
//  }
//
//  private static final int rank2existentialRestriction(final ELConceptDescription existentialRestriction) {
//    if (!existentialRestriction.conceptNames.isEmpty())
//      throw new IllegalArgumentException();
//    else if (existentialRestriction.existentialRestrictions.size() != 1)
//      throw new IllegalArgumentException();
//    else {
//      final Pair<IRI, ELConceptDescription> rC = existentialRestriction.existentialRestrictions.iterator().next();
//      final IRI r = rC.first();
//      final ELConceptDescription C = rC.second();
//      C.reduce();
//      return 1 + rank2conjunction(
//          new ELConceptDescription(
//              new HashSet<>(),
//              C.upperNeighborsReduced().parallelStream().map(D -> Pair.of(r, D)).collect(Collectors.toSet())));
//    }
//  }

  public final int rank2() {
    return this.clone().reduce().unreducedRank2();
  }

  public final int unreducedRank2() {
    return this.getConceptNames().size() + this.getExistentialRestrictions().keySet().parallelStream().map(r -> {
      final ELConceptDescription rDs = new ELConceptDescription();
      rDs.getExistentialRestrictions().putAll(r, this.getExistentialRestrictions().get(r));
      return rDs;
    }).mapToInt(ELConceptDescription::unreducedRank).sum();
  }

  public final int rank3() {
//    final Set<ELConceptDescription> Us = representatives(upperNeighborsReduced(), (X, Y) -> X.isEquivalentTo(Y));
    final Set<ELConceptDescription> Us = upperNeighborsReduced();
//    if (Us.parallelStream().anyMatch(X -> Us.parallelStream().filter(Y -> X != Y).anyMatch(X::isEquivalentTo)))
//      throw new RuntimeException();
    if (Us.isEmpty())
      return 0;
    else if (Us.size() == 1)
      return 1 + Us.iterator().next().clone().reduce().rank3();
    else
      return Us.size() + ELLeastCommonSubsumer.lcs(Us).clone().reduce().rank3();
  }

  public final int rank4() {
    return this.clone().reduce().unreducedRank4();
  }

  public final int unreducedRank4() {
    return this.getConceptNames().size() + this
        .getExistentialRestrictions()
        .keySet()
        .parallelStream()
        .map(r -> this.getExistentialRestrictions().get(r))
        .mapToInt(ELConceptDescription::recurseUnreducedRank4)
        .sum();
  }

  private static final int recurseUnreducedRank4(final Collection<ELConceptDescription> Ds) {
    if (Ds.isEmpty())
      return 0;
    else if (Ds.size() == 1)
      return 1 + recurseUnreducedRank4(Ds.iterator().next().upperNeighborsReduced());
    else
      return IntStream
          .range(1, Ds.size() + 1)
          .boxed()
          .map(
              i -> (int) (Math.pow(-1, i + 1) * Sets
                  .combinations(Sets.newHashSet(Ds), i)
                  .parallelStream()
                  .map(ELLeastCommonSubsumer::lcs)
                  .map(ELConceptDescription::reduce)
                  .collect(Collectors.toSet())
                  .parallelStream()
                  .map(Collections::singleton)
                  .collect(Collectors.summingInt(ELConceptDescription::recurseUnreducedRank4))))
          .collect(Collectors.summingInt(Integer::valueOf));
  }

  public final int distanceTo(final ELConceptDescription other) {
    final List<Integer> x = Stream
        .<Supplier<Integer>> of(
            () -> ELConceptDescription.conjunction(this, other).rank(),
            () -> ELLeastCommonSubsumer.lcs(this, other).rank())
        .parallel()
        .map(Supplier::get)
        .collect(Collectors.toList());
    return x.get(0) - x.get(1);
  }

  public final int distanceTo2(final ELConceptDescription other) {
    final ELConceptDescription lcs = ELLeastCommonSubsumer.lcs(this, other);
    int distance = -1;
    for (ELConceptDescription C = ELConceptDescription.conjunction(this, other).reduce(); C != null; C =
        C.oneUpperNeighborBelow(lcs))
      distance++;
    return distance;
  }

  private final ELConceptDescription oneUpperNeighborBelow(final ELConceptDescription other) {
    return Stream.concat(this.conceptNames.parallelStream().map(A -> {
      final ELConceptDescription upperNeighbor = this.clone();
      upperNeighbor.conceptNames.remove(A);
      return upperNeighbor;
    }), this.existentialRestrictions.entries().parallelStream().map(ER -> {
      final ELConceptDescription upperNeighbor = this.clone();
      upperNeighbor.existentialRestrictions.remove(ER.getKey(), ER.getValue());
      ER
          .getValue()
          .upperNeighborsReduced()
          .parallelStream()
          .filter(
              uER -> upperNeighbor.existentialRestrictions
                  .entries()
                  .parallelStream()
                  .filter(otherER -> !otherER.equals(ER))
                  .filter(otherER -> ER.getKey().equals(otherER.getKey()))
                  .map(Entry::getValue)
                  .noneMatch(uER::subsumes))
          .map(uER -> Pair.of(ER.getKey(), uER))
          .sequential()
          .forEach(p -> upperNeighbor.existentialRestrictions.put(p.first(), p.second()));
      return upperNeighbor;
    })).filter(other::subsumes).findAny().orElse(null);
  }

  public final Set<ELConceptDescription> neighborhood(final int radius, final Signature sigma) {
    if (radius < 0)
      throw new IllegalArgumentException();
    final Set<ELConceptDescription> next = Sets.newHashSet(this);
    final Set<ELConceptDescription> neighborhood = Sets.newHashSet(this);
    for (int k = 0; k < radius; k++) {
      final Set<ELConceptDescription> news = Stream
          .concat(
              next.parallelStream().flatMap(C -> C.upperNeighborsReduced().parallelStream()),
              next.parallelStream().flatMap(C -> C.lowerNeighborsReduced(sigma).parallelStream()))
          .collect(Collectors.toSet());
      next.clear();
      next.addAll(news);
      next.removeAll(neighborhood);
      neighborhood.addAll(news);
    }
    return neighborhood;
  }

  public final Set<ELConceptDescription> upperNeighbors() {
    final ELConceptDescription reducedForm = this.clone().reduce();
    return Stream.concat(reducedForm.conceptNames.parallelStream().map(A -> {
      final ELConceptDescription upperNeighbor = reducedForm.clone();
      upperNeighbor.conceptNames.remove(A);
      return upperNeighbor;
    }), reducedForm.existentialRestrictions.entries().parallelStream().map(ER -> {
      final ELConceptDescription upperNeighbor = reducedForm.clone();
      upperNeighbor.existentialRestrictions.remove(ER.getKey(), ER.getValue());
      ER.getValue().upperNeighbors().forEach(
          uER -> upperNeighbor.existentialRestrictions.put(ER.getKey(), uER));
      return upperNeighbor;
    })
//        .filter(other -> !this.subsumes(other)))
    ).collect(Collectors.toSet());
  }

  public final Set<ELConceptDescription> upperNeighborsReduced() {
    final ELConceptDescription reducedForm = this.clone().reduce();
    return Collections3.representatives(Stream.concat(reducedForm.conceptNames.parallelStream().map(A -> {
      final ELConceptDescription upperNeighbor = reducedForm.clone();
      upperNeighbor.conceptNames.remove(A);
      return upperNeighbor;
    }), reducedForm.existentialRestrictions.entries().parallelStream().map(ER -> {
      final ELConceptDescription upperNeighbor = reducedForm.clone();
      upperNeighbor.existentialRestrictions.remove(ER.getKey(), ER.getValue());
      ER
          .getValue()
          .upperNeighborsReduced()
          .parallelStream()
          .filter(
              uER -> reducedForm.existentialRestrictions
                  .entries()
                  .parallelStream()
                  .filter(otherER -> !otherER.equals(ER))
                  .filter(otherER -> ER.getKey().equals(otherER.getKey()))
                  .map(Entry::getValue)
                  .noneMatch(uER::subsumes))
          .sequential()
          .forEach(uER -> upperNeighbor.existentialRestrictions.put(ER.getKey(), uER));
      return upperNeighbor;
    })
//        .filter(other -> !this.subsumes(other)))
    ).collect(Collectors.toSet()), (X, Y) -> X.isEquivalentTo(Y));
  }

  public final Set<ELConceptDescription> lowerNeighbors(final Signature sigma) {
    final ELConceptDescription C = ELConceptDescription.this.clone().reduce();
    final Set<ELConceptDescription> lowerNeighbors = Sets.newConcurrentHashSet();
    sigma.getConceptNames().parallelStream().filter(A -> !C.conceptNames.contains(A)).map(A -> {
      final ELConceptDescription lowerNeighbor = C.clone();
      lowerNeighbor.conceptNames.add(A);
      return lowerNeighbor;
    }).forEach(lowerNeighbors::add);
    sigma.getRoleNames().parallelStream().forEach(r -> {
      final Set<ELConceptDescription> filter = C.existentialRestrictions
          .entries()
          .parallelStream()
          .filter(existentialRestriction -> existentialRestriction.getKey().equals(r))
          .map(Entry::getValue)
          .collect(Collectors.toSet());
      if (filter.isEmpty()) {
        final ELConceptDescription lowerNeighbor = C.clone();
        lowerNeighbor.existentialRestrictions.put(r, ELConceptDescription.top());
        lowerNeighbors.add(lowerNeighbor);
      } else
        recurseLowerNeighbors(sigma, r, C, Collections.singleton(ELConceptDescription.top()), filter, lowerNeighbors);
    });
    return lowerNeighbors;
  }

  private final void recurseLowerNeighbors(
      final Signature sigma,
      final IRI r,
      final ELConceptDescription C,
      final Set<ELConceptDescription> currentCandidates,
      final Set<ELConceptDescription> filter,
      final Set<ELConceptDescription> lowerNeighbors) {
    final Set<ELConceptDescription> nextCandidates = Sets.newConcurrentHashSet();
    currentCandidates.parallelStream().forEach(D -> {
      if (!D.upperNeighborsReduced().parallelStream().allMatch(
          U -> C.isSubsumedBy(ELConceptDescription.existentialRestriction(r, U))))
        return;
      else if (filter.parallelStream().anyMatch(F -> F.isSubsumedBy(D)))
        D.lowerNeighbors(sigma).parallelStream().forEach(nextCandidates::add);
      else {
        final ELConceptDescription X = C.clone();
        X.existentialRestrictions.put(r, D);
        lowerNeighbors.add(X);
      }
    });
    if (!nextCandidates.isEmpty())
      recurseLowerNeighbors(sigma, r, C, nextCandidates, filter, lowerNeighbors);
  }

  public final Set<ELConceptDescription> lowerNeighborsReduced(final Signature sigma) {
//    final Set<ELConceptDescription> lowerNeighbors = lowerNeighbors(sigma);
//    lowerNeighbors.parallelStream().forEach(ELConceptDescription::reduce);
    final Set<ELConceptDescription> lowerNeighbors = new HashSet<>();
    for (ELConceptDescription lowerNeighbor : lowerNeighbors(sigma))
      lowerNeighbors.add(lowerNeighbor.reduce());
    return lowerNeighbors;
  }

  protected final Set<ELConceptDescription> lowerNeighbors2(final Signature sigma) {
    final Set<ELConceptDescription> lowerNeighbors = Sets.newConcurrentHashSet();
    sigma.getConceptNames().parallelStream().filter(A -> !ELConceptDescription.this.conceptNames.contains(A)).map(A -> {
      final ELConceptDescription lowerNeighbor = ELConceptDescription.this.clone();
      lowerNeighbor.conceptNames.add(A);
      return lowerNeighbor;
    }).forEach(lowerNeighbors::add);
    sigma.getRoleNames().parallelStream().forEach(r -> {
      final ELConceptDescription rLCS = ELLeastCommonSubsumer.lcs(
          ELConceptDescription.this.clone().reduce().existentialRestrictions
              .entries()
              .parallelStream()
              .filter(sD -> sD.getKey().equals(r))
              .map(Entry::getValue)
              .collect(Collectors.toSet()));
      rLCS.reduce();
//      recurseLowerNeighbors(sigma, r, new ELConceptDescription(), lowerNeighbors);
      recurseLowerNeighbors2a(sigma, r, rLCS, lowerNeighbors);
      recurseLowerNeighbors2b(sigma, r, new ELConceptDescription(), rLCS, lowerNeighbors);
    });
    return lowerNeighbors;
  }

  private final void recurseLowerNeighbors2a(
      final Signature sigma,
      final IRI r,
      final ELConceptDescription D,
      final Set<ELConceptDescription> lowerNeighbors) {
    if (ELConceptDescription.this.isSubsumedBy(ELConceptDescription.existentialRestriction(r, D)))
      D.lowerNeighbors2(sigma).parallelStream().forEach(E -> recurseLowerNeighbors2a(sigma, r, E, lowerNeighbors));
    else if (D.upperNeighborsReduced().parallelStream().allMatch(
        E -> ELConceptDescription.this.isSubsumedBy(ELConceptDescription.existentialRestriction(r, E)))) {
      final ELConceptDescription lowerNeighbor = ELConceptDescription.this.clone();
      lowerNeighbor.existentialRestrictions.put(r, D);
      lowerNeighbors.add(lowerNeighbor);
    }
  }

  private final void recurseLowerNeighbors2b(
      final Signature sigma,
      final IRI r,
      final ELConceptDescription D,
      final ELConceptDescription rLCS,
      final Set<ELConceptDescription> lowerNeighbors) {
    if (rLCS.subsumes(D))
      return;
    else if (ELConceptDescription.this.isSubsumedBy(ELConceptDescription.existentialRestriction(r, D)))
      D.lowerNeighbors2(sigma).parallelStream().forEach(
          E -> recurseLowerNeighbors2b(sigma, r, E, rLCS, lowerNeighbors));
    else if (D.upperNeighborsReduced().parallelStream().allMatch(
        E -> ELConceptDescription.this.isSubsumedBy(ELConceptDescription.existentialRestriction(r, E)))) {
      final ELConceptDescription lowerNeighbor = ELConceptDescription.this.clone();
      lowerNeighbor.existentialRestrictions.put(r, D);
      lowerNeighbors.add(lowerNeighbor);
    }
  }

  protected final Set<ELConceptDescription> lowerNeighbors3(final Signature sigma) {
    final ELConceptDescription C = ELConceptDescription.this.clone().reduce();
    final Set<ELConceptDescription> lowerNeighbors = Sets.newConcurrentHashSet();
    sigma.getConceptNames().parallelStream().filter(A -> !ELConceptDescription.this.conceptNames.contains(A)).map(A -> {
      final ELConceptDescription lowerNeighbor = C.clone();
      lowerNeighbor.conceptNames.add(A);
      return lowerNeighbor;
    }).forEach(lowerNeighbors::add);
    sigma.getRoleNames().parallelStream().forEach(r -> {
      final Set<ELConceptDescription> Ds = C.existentialRestrictions
          .entries()
          .parallelStream()
          .filter(sD -> sD.getKey().equals(r))
          .map(Entry::getValue)
          .collect(Collectors.toSet());
      if (Ds.isEmpty()) {
        final ELConceptDescription X = C.clone();
        X.existentialRestrictions.put(r, ELConceptDescription.top());
        lowerNeighbors.add(X);
      } else
//        Stream
//            .concat(
        // Collections.<Pair<Set<ELConceptDescription>, ELConceptDescription>> emptySet().parallelStream(),
        Collections
            .singleton(Pair.of(Ds, ELConceptDescription.top()))
            .parallelStream()
            // ,
            // Sets.powerSet(Ds).parallelStream().filter(Es -> Es.size() > 0).map(Es -> {
            // final ELConceptDescription lcsEs = ELLeastCommonSubsumer._of(Es);
            // lcsEs.reduce();
            // return Pair.of(Es, lcsEs);
            // }))
            .forEach(p -> {
              recurseLowerNeighbors3(sigma, r, C, p.second(), p.first(), lowerNeighbors);
              // p.second().lowerNeighbors3(sigma).parallelStream().forEach(
              // E -> recurseLowerNeighbors3(sigma, r, C, E, p.first(), lowerNeighbors));
            });
    });
    return lowerNeighbors;
  }

  private final void recurseLowerNeighbors3(
      final Signature sigma,
      final IRI r,
      final ELConceptDescription C,
      final ELConceptDescription D,
      final Set<ELConceptDescription> filter,
      final Set<ELConceptDescription> lowerNeighbors) {
    if (!D.upperNeighborsReduced().parallelStream().allMatch(
        U -> C.isSubsumedBy(ELConceptDescription.existentialRestriction(r, U))))
      return;
    else if (filter.parallelStream().anyMatch(F -> F.isSubsumedBy(D)))
      D.lowerNeighbors3(sigma).parallelStream().forEach(
          E -> recurseLowerNeighbors3(sigma, r, C, E, filter, lowerNeighbors));
//      return;
    else {
      final ELConceptDescription X = C.clone();
      X.existentialRestrictions.put(r, D);
      lowerNeighbors.add(X);
    }
//    D
//        .lowerNeighbors3(sigma)
//        .parallelStream()
//        .filter(L -> filter.parallelStream().allMatch(E -> !E.isSubsumedBy(L)))
//        .filter(
//            L -> L.upperNeighborsReduced().parallelStream().allMatch(
//                U -> C.isSubsumedBy(ELConceptDescription.existentialRestriction(Pair.of(r, U)))))
//        .map(L -> {
//          final ELConceptDescription X = C.clone();
//          X.existentialRestrictions.add(Pair.of(r, L));
//          return X;
//        })
//        .forEach(lowerNeighbors::add);
  }

//  public final Set<ELConceptDescription> lowerNeighbors2(final Signature sigma) {
//    this.reduce();
//    final Set<ELConceptDescription> lowerNeighbors = Sets.newConcurrentHashSet();
//    sigma.getConceptNames().parallelStream().filter(A -> !ELConceptDescription.this.conceptNames.contains(A)).map(A -> {
//      final ELConceptDescription lowerNeighbor = ELConceptDescription.this.clone();
//      lowerNeighbor.conceptNames.add(A);
//      return lowerNeighbor;
//    }).forEach(lowerNeighbors::add);
//    sigma.getRoleNames().parallelStream().forEach(
//        r -> this.existentialRestrictions.parallelStream().filter(sD -> sD.first().equals(r)).map(Pair::second).forEach(
//            D -> D.lowerNeighbors2(sigma).parallelStream().forEach(
//                E -> recurseLowerNeighbors2(sigma, r, E, lowerNeighbors))));
//    return lowerNeighbors;
//  }
//
//  private final void recurseLowerNeighbors2(
//      final Signature sigma,
//      final IRI r,
//      final ELConceptDescription D,
//      final Set<ELConceptDescription> lowerNeighbors) {
//    if (D.upperNeighborsReduced().parallelStream().allMatch(
//        E -> ELConceptDescription.this.isSubsumedBy(ELConceptDescription.existentialRestriction(Pair.of(r, E))))) {
//      final ELConceptDescription lowerNeighbor = ELConceptDescription.this.clone();
//      lowerNeighbor.existentialRestrictions.add(Pair.of(r, D));
//      lowerNeighbors.add(lowerNeighbor);
//    } else
//      D.lowerNeighbors(sigma).parallelStream().forEach(E -> recurseLowerNeighbors2(sigma, r, E, lowerNeighbors));
//  }

  public final Set<ELConceptDescription> lowerNeighbors4(final Signature sigma) {
    final ELConceptDescription C = ELConceptDescription.this.clone().reduce();
    final Set<ELConceptDescription> lowerNeighbors = Sets.newConcurrentHashSet();
    sigma.getConceptNames().parallelStream().filter(A -> !C.conceptNames.contains(A)).map(A -> {
      final ELConceptDescription lowerNeighbor = C.clone();
      lowerNeighbor.conceptNames.add(A);
      return lowerNeighbor;
    }).forEach(lowerNeighbors::add);
    sigma.getRoleNames().parallelStream().forEach(r -> {
//      recurseLowerNeighbors4a(sigma, Collections.singletonList(r), C, lowerNeighbors);
      final Set<ELConceptDescription> filter = C.existentialRestrictions
          .entries()
          .parallelStream()
          .filter(existentialRestriction -> existentialRestriction.getKey().equals(r))
          .map(Entry::getValue)
          .collect(Collectors.toSet());
      if (filter.isEmpty()) {
        final ELConceptDescription lowerNeighbor = C.clone();
        lowerNeighbor.existentialRestrictions.put(r, ELConceptDescription.top());
        lowerNeighbors.add(lowerNeighbor);
      } else {
        if (filter.size() == 1) {
//          final ELConceptDescription D = filter.iterator().next();
//          for (ELConceptDescription E : D.lowerNeighbors4(sigma))
//            if (E.reduce().topLevelConjuncts() == 1)
//              lowerNeighbors
//                  .add(ELConceptDescription.conjunction(C.clone(), ELConceptDescription.existentialRestriction(r, E)));
        } else
          recurseLowerNeighbors4(
              sigma,
              r,
              C,
              Collections.singleton(ELConceptDescription.conjunction(filter)),
              filter,
              lowerNeighbors);
        filter.parallelStream().forEach(F -> {
          F.lowerNeighbors4(sigma).parallelStream().forEach(L -> {
            L.conceptNames.removeIf(A -> F.isSubsumedBy(ELConceptDescription.conceptName(A)));
            L.existentialRestrictions.entries().removeIf(
                rE -> F.isSubsumedBy(ELConceptDescription.existentialRestriction(rE.getKey(), rE.getValue())));
            final ELConceptDescription rL = ELConceptDescription.existentialRestriction(r, L);
            if (!C.isSubsumedBy(rL))
              lowerNeighbors.add(ELConceptDescription.conjunction(C.clone(), rL));
          });
        });
      }
    });
    return lowerNeighbors;
  }

  private final void recurseLowerNeighbors4(
      final Signature sigma,
      final IRI r,
      final ELConceptDescription C,
      final Set<ELConceptDescription> currentCandidates,
      final Set<ELConceptDescription> filter,
      final Set<ELConceptDescription> lowerNeighbors) {
    final Set<ELConceptDescription> nextCandidates = Sets.newConcurrentHashSet();
    currentCandidates.parallelStream().forEach(D -> {
      D.reduce();
      if (filter.parallelStream().noneMatch(F -> F.isSubsumedBy(D))) {
//      if (!C.isSubsumedBy(ELConceptDescription.existentialRestriction(r, D))) {
        final Set<ELConceptDescription> Us = D.upperNeighborsReduced();
        if (D.topLevelConjuncts() <= filter.size()
            && Us.parallelStream().allMatch(U -> filter.parallelStream().anyMatch(F -> F.isSubsumedBy(U)))) {
//          if (D.topLevelConjuncts() <= filter.size() && Us.parallelStream().allMatch(U -> C.isSubsumedBy(ELConceptDescription.existentialRestriction(r, U)))) {
          final ELConceptDescription X = C.clone();
          X.existentialRestrictions.put(r, D);
          lowerNeighbors.add(X);
        } else
          Us.parallelStream().filter(U -> filter.parallelStream().noneMatch(F -> F.isSubsumedBy(U))).forEach(
              // Us.parallelStream().filter(U -> !C.isSubsumedBy(ELConceptDescription.existentialRestriction(r,
              // U))).forEach(
              nextCandidates::add);
      }
    });
    if (!nextCandidates.isEmpty())
      recurseLowerNeighbors4(sigma, r, C, nextCandidates, filter, lowerNeighbors);
  }

  private final void recurseLowerNeighbors4a(
      final Signature sigma,
      final List<IRI> rs,
      final ELConceptDescription C,
      final Set<ELConceptDescription> lowerNeighbors) {
    final ELConceptDescription rsTop = ELConceptDescription.existentialRestriction(rs, ELConceptDescription.top());
    if (C.isSubsumedBy(rsTop)) {
      for (IRI A : sigma.getConceptNames()) {
        final ELConceptDescription rsA =
            ELConceptDescription.existentialRestriction(rs, ELConceptDescription.conceptName(A));
        if (!C.isSubsumedBy(rsA))
          lowerNeighbors.add(ELConceptDescription.conjunction(C.clone(), rsA));
      }
      for (IRI r : sigma.getRoleNames()) {
        final ELConceptDescription rsrTop = ELConceptDescription
            .existentialRestriction(rs, ELConceptDescription.existentialRestriction(r, ELConceptDescription.top()));
        if (!C.isSubsumedBy(rsrTop))
          lowerNeighbors.add(ELConceptDescription.conjunction(C.clone(), rsrTop));
        final List<IRI> rsr = new LinkedList<>(rs);
        rsr.add(r);
        recurseLowerNeighbors4a(sigma, rsr, C, lowerNeighbors);
      }
    }
  }

  public final Set<ELConceptDescription> lowerNeighbors5(final Signature sigma) {
    final ELConceptDescription C = ELConceptDescription.this.clone().reduce();
    final Set<ELConceptDescription> lowerNeighbors = Sets.newConcurrentHashSet();
    sigma.getConceptNames().parallelStream().filter(A -> !C.conceptNames.contains(A)).map(A -> {
      final ELConceptDescription lowerNeighbor = C.clone();
      lowerNeighbor.conceptNames.add(A);
      return lowerNeighbor;
    }).forEach(lowerNeighbors::add);
    sigma.getRoleNames().parallelStream().forEach(r -> {
      final Set<ELConceptDescription> filter = C.existentialRestrictions
          .entries()
          .parallelStream()
          .filter(existentialRestriction -> existentialRestriction.getKey().equals(r))
          .map(Entry::getValue)
          .collect(Collectors.toSet());
      if (filter.isEmpty()) {
        final ELConceptDescription lowerNeighbor = C.clone();
        lowerNeighbor.existentialRestrictions.put(r, ELConceptDescription.top());
        lowerNeighbors.add(lowerNeighbor);
      } else {
        if (filter.size() > 1) {
          filter.parallelStream().forEach(F -> filter.parallelStream().filter(G -> !G.equals(F)).forEach(G -> {
            final ELConceptDescription D = ELConceptDescription.conjunction(F, G);
            recurseLowerNeighbors5(sigma, r, C, Collections.singleton(D), filter, lowerNeighbors);
          }));
        }
        filter.parallelStream().forEach(F -> {
          F.lowerNeighbors5(sigma).parallelStream().forEach(L -> {
            L.conceptNames.removeIf(A -> F.isSubsumedBy(ELConceptDescription.conceptName(A)));
            L.existentialRestrictions.entries().removeIf(
                rE -> F.isSubsumedBy(ELConceptDescription.existentialRestriction(rE.getKey(), rE.getValue())));
            final ELConceptDescription rL = ELConceptDescription.existentialRestriction(r, L);
            if (!C.isSubsumedBy(rL))
              lowerNeighbors.add(ELConceptDescription.conjunction(C.clone(), rL));
          });
        });
      }
    });
    return lowerNeighbors;
  }

  private final void recurseLowerNeighbors5(
      final Signature sigma,
      final IRI r,
      final ELConceptDescription C,
      final Set<ELConceptDescription> currentCandidates,
      final Set<ELConceptDescription> filter,
      final Set<ELConceptDescription> lowerNeighbors) {
    final Set<ELConceptDescription> nextCandidates = Sets.newConcurrentHashSet();
    currentCandidates.parallelStream().forEach(D -> {
      D.reduce();
      if (filter.parallelStream().noneMatch(F -> F.isSubsumedBy(D))) {
        final Set<ELConceptDescription> Us = D.upperNeighborsReduced();
        if (D.topLevelConjuncts() <= filter.size()
            && Us.parallelStream().allMatch(U -> filter.parallelStream().anyMatch(F -> F.isSubsumedBy(U)))) {
          final ELConceptDescription X = C.clone();
          X.existentialRestrictions.put(r, D);
          lowerNeighbors.add(X);
        } else
          Us.parallelStream().filter(U -> filter.parallelStream().noneMatch(F -> F.isSubsumedBy(U))).forEach(
              nextCandidates::add);
      }
    });
    if (!nextCandidates.isEmpty())
      recurseLowerNeighbors5(sigma, r, C, nextCandidates, filter, lowerNeighbors);
  }

  @Override
  public final String toString() {
    if (isBot())
      return UnicodeSymbols.BOT;
    if (isTop())
      return UnicodeSymbols.TOP;
    final StringBuilder sb = new StringBuilder();
    final Iterator<IRI> it1 = conceptNames.iterator();
    if (it1.hasNext()) {
      sb.append(it1.next().toString());
    }
    while (it1.hasNext()) {
      // sb.append(" ");
      sb.append(UnicodeSymbols.SQCAP);
      // sb.append(" ");
      sb.append(it1.next().toString());
    }
    if (!conceptNames.isEmpty() && !existentialRestrictions.isEmpty()) {
      // sb.append(" ");
      sb.append(UnicodeSymbols.SQCAP);
      // sb.append(" ");
    }
    final Iterator<Entry<IRI, ELConceptDescription>> it2 = existentialRestrictions.entries().iterator();
    if (it2.hasNext()) {
      final Entry<IRI, ELConceptDescription> existentialRestriction = it2.next();
      sb.append(UnicodeSymbols.EXISTS);
      // sb.append(" ");
      sb.append(existentialRestriction.getKey().toString());
      // sb.append(" ");
      sb.append(".");
      if (existentialRestriction.getValue().conceptNames.size()
          + existentialRestriction.getValue().existentialRestrictions.size() <= 1)
        sb.append(existentialRestriction.getValue().toString());
      else {
        sb.append("(");
        sb.append(existentialRestriction.getValue().toString());
        sb.append(")");
      }
    }
    while (it2.hasNext()) {
      // sb.append(" ");
      sb.append(UnicodeSymbols.SQCAP);
      final Entry<IRI, ELConceptDescription> existentialRestriction = it2.next();
      // sb.append(" ");
      sb.append(UnicodeSymbols.EXISTS);
      // sb.append(" ");
      sb.append(existentialRestriction.getKey().toString());
      // sb.append(" ");
      sb.append(".");
      if (existentialRestriction.getValue().conceptNames.size()
          + existentialRestriction.getValue().existentialRestrictions.size() <= 1)
        sb.append(existentialRestriction.getValue().toString());
      else {
        sb.append("(");
        sb.append(existentialRestriction.getValue().toString());
        sb.append(")");
      }
    }
    return sb.toString();
  }

  public final String toLaTeXString() {
    if (this.isBot())
      return "\\bot";
    if (this.isTop())
      return "\\top";
    final StringBuilder sb = new StringBuilder();
    final Iterator<IRI> it1 = conceptNames.iterator();
    if (it1.hasNext()) {
      sb.append(it1.next().toString());
    }
    while (it1.hasNext()) {
      sb.append(" \\sqcap ");
      sb.append(it1.next().toString());
    }
    final Iterator<Entry<IRI, ELConceptDescription>> it2 = existentialRestrictions.entries().iterator();
    if (conceptNames.isEmpty())
      sb.append(" \\sqcap ");
    if (it2.hasNext()) {
      final Entry<IRI, ELConceptDescription> existentialRestriction = it2.next();
      sb.append(" \\exists ");
      sb.append(existentialRestriction.getKey().toString());
      sb.append(" . ");
      if (existentialRestriction.getValue().conceptNames.size()
          + existentialRestriction.getValue().existentialRestrictions.size() <= 1)
        sb.append(existentialRestriction.getValue().toLaTeXString());
      else {
        sb.append(" \\left( ");
        sb.append(existentialRestriction.getValue().toLaTeXString());
        sb.append(" \\right) ");
      }
    }
    while (it2.hasNext()) {
      final Entry<IRI, ELConceptDescription> existentialRestriction = it2.next();
      sb.append(" \\sqcap ");
      sb.append(" \\exists ");
      sb.append(existentialRestriction.getKey().toString());
      sb.append(" . ");
      if (existentialRestriction.getValue().conceptNames.size()
          + existentialRestriction.getValue().existentialRestrictions.size() <= 1)
        sb.append(existentialRestriction.getValue().toLaTeXString());
      else {
        sb.append(" \\left( ");
        sb.append(existentialRestriction.getValue().toLaTeXString());
        sb.append(" \\right) ");
      }

    }
    return sb.toString();
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof ELConceptDescription))
      return false;
    final ELConceptDescription other = (ELConceptDescription) obj;
    return this.conceptNames.equals(other.conceptNames)
        && this.existentialRestrictions.equals(other.existentialRestrictions);
  }

  @Override
  public final int hashCode() {
    return 2 * conceptNames.hashCode() + 3 * existentialRestrictions.hashCode();
  }

  @Override
  public final ELConceptDescription clone() {
    final ELConceptDescription clone = new ELConceptDescription();
    clone.getConceptNames().addAll(this.getConceptNames());
    this
        .getExistentialRestrictions()
        .entries()
        .parallelStream()
        .map(ER -> Pair.of(ER.getKey(), ER.getValue().clone()))
        .sequential()
        .forEach(ER -> clone.getExistentialRestrictions().put(ER.x(), ER.y()));
    return clone;
  }

  @Override
  public final int compareTo(final ELConceptDescription other) {
    final List<Boolean> x = Stream
        .<Supplier<Boolean>> of(() -> this.subsumes(other), () -> other.subsumes(this))
        .parallel()
        .map(Supplier::get)
        .collect(Collectors.toList());
    if (x.get(0) && x.get(1))
      return 0;
    else if (x.get(0))
      return 1;
    else if (x.get(1))
      return -1;
    else
      return Integer.MAX_VALUE;
  }

  @Override
  public final boolean smaller(final ELConceptDescription other) {
    return Stream.<Supplier<Boolean>> of(() -> !this.subsumes(other), () -> other.subsumes(this)).parallel().allMatch(
        Supplier::get);
  }

  @Override
  public final boolean greater(final ELConceptDescription other) {
    return Stream.<Supplier<Boolean>> of(() -> this.subsumes(other), () -> !other.subsumes(this)).parallel().allMatch(
        Supplier::get);
  }

  @Override
  public final boolean smallerEq(final ELConceptDescription other) {
    return this.isSubsumedBy(other);
  }

  @Override
  public final boolean greaterEq(final ELConceptDescription other) {
    return this.subsumes(other);
  }

  @Override
  public final boolean uncomparable(final ELConceptDescription other) {
    return Stream.<Supplier<Boolean>> of(() -> !this.subsumes(other), () -> !other.subsumes(this)).parallel().allMatch(
        Supplier::get);
  }

}
